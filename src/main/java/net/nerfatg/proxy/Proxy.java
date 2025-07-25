package net.nerfatg.proxy;

import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketType;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Proxy {

    private static final Set<SocketChannel> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private static final HashMap<String, SocketChannel> playerClients = new HashMap<>();
    private static Proxy instance;

    private final NerfLogger logger;

    private final int port;

    private boolean running;
    private final HashMap<PacketType, List<PacketHandle>> handles;

    public Proxy(int port, NerfLogger logger) {
        this.handles = new HashMap<>();
        for (PacketType type : PacketType.values()) {
            this.handles.put(type, new ArrayList<>());
        }

        this.logger = logger;
        this.port = port;
        instance = this;
        
        logger.debug("Proxy initialized on port " + port);
    }

    public Proxy(int port) {
        this(port, LoggerFactory.getLogger("Proxy"));
    }

    public void registerHandle(PacketType type, PacketHandle handle) {
        handles.get(type).add(handle);
    }

    public void unregister(PacketType type, PacketHandle handle) {
        handles.get(type).remove(handle);
    }

    public void launch(Runnable listening) {
        running = true;

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // Erstelle einen Selector
            Selector selector = Selector.open();

            // Erstelle einen ServerSocketChannel und konfiguriere ihn auf nicht-blockierend
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            // Registriere den ServerSocketChannel für neue Verbindungen
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.important("Proxy listening on port " + port);

            listening.run();

            // Server-Schleife
            while (running) {
                spin(serverSocketChannel, selector);
            }
        } catch (IOException e) {
            logger.error("Failed to launch proxy: " + e.getMessage());
        }
    }

    public void spin(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        // Warte auf Ereignisse
        selector.select();

        // Iteriere über die bereitgestellten Schlüssel (Events)
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove(); // Entferne den Schlüssel aus der Liste

            if (key.isAcceptable()) {
                handleAccept(serverSocketChannel, selector);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }
    }

    private void handleAccept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        // Akzeptiere die Verbindung und konfiguriere sie auf nicht-blockierend
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);

        // Registriere den neuen ClientChannel beim Selector für Leseoperationen
        clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(64));
        connectedClients.add(clientChannel);
        logger.info("Client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // Client hat die Verbindung geschlossen
            logger.info("Client closed connection: " + clientChannel.getRemoteAddress());
            connectedClients.remove(clientChannel);
            clientChannel.close();
            key.cancel();
            return;
        }

        if (buffer.position() == 64) {
            buffer.flip();
            PacketType clientPacketType = PacketType.values()[buffer.getInt()];
            logger.debug("Server received packet: " + clientPacketType);

            List<Packet> responses = new ArrayList<>();

            for (PacketHandle handle : handles.get(clientPacketType)) {
                handle.handle(buffer.duplicate()).ifPresent(responses::add);
            }

            for (Packet response : responses) {
                ByteBuffer dbuf = ByteBuffer.allocate(64);
                dbuf.putInt(response.getType().ordinal());
                response.toBytes(dbuf);
                dbuf.position(0);

                logger.debug("Send Response: " + response);

                clientChannel.write(dbuf);
            }

            buffer.clear();
        }
    }

    public void send(String playerId, Packet packet) {
        logger.debug("Sending packet to player " + playerId + ": " + packet.getType());
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(packet.getType().ordinal());
        packet.toBytes(buffer);

        try {
            SocketChannel playerChannel = playerClients.get(playerId);
            if (playerChannel != null) {
                playerChannel.write(buffer);
                logger.fine("Packet sent successfully to player " + playerId);
            } else {
                logger.warning("Player " + playerId + " not found in connected clients");
            }
        } catch (IOException e) {
            logger.error("Failed to send packet to player " + playerId + ": " + e.getMessage());
        }
    }

    public void broadcast(Packet packet) {
        logger.info("Broadcasting packet to " + connectedClients.size() + " clients: " + packet.getType());
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(packet.getType().ordinal());
        packet.toBytes(buffer);

        int successCount = 0;
        int failCount = 0;

        for (SocketChannel client : connectedClients) {
            try {
                logger.debug("Sending packet to client " + client.getRemoteAddress());
                client.write(buffer.duplicate());
                successCount++;
            } catch (IOException e) {
                logger.warning("Failed to send packet to client" + ": " + e.getMessage());
                failCount++;
            }
        }

        logger.info("Broadcast completed - Success: " + successCount + ", Failed: " + failCount);
    }

    public void shutdown() {
        running = false;
    }

    public static Set<SocketChannel> getConnectedClients() {
        return connectedClients;
    }

    public static Map<String, SocketChannel> getPlayerClients() {
        return playerClients;
    }

    public static Proxy getInstance() {
        return instance;
    }
}
