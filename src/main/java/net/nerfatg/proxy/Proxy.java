package net.nerfatg.proxy;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.client.ClientPacketType;
import net.nerfatg.proxy.packet.server.ServerPacketType;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proxy {

    private static final Set<SocketChannel> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private static final HashMap<String, SocketChannel> playerClients = new HashMap<>();

    private final int port;

    private boolean running;
    private final HashMap<ClientPacketType, List<PacketHandle>> handles;

    public Proxy(int port) {
        this.handles = new HashMap<>();
        for (ClientPacketType type : ClientPacketType.values()) {
            this.handles.put(type, new ArrayList<>());
        }

        this.port = port;
    }

    public void registerHandle(ClientPacketType type, PacketHandle handle) {
        handles.get(type).add(handle);
    }

    public void unregister(ClientPacketType type, PacketHandle handle) {
        handles.get(type).remove(handle);
    }

    public void launch() {
        running = true;

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // Erstelle einen Selector
            Selector selector = Selector.open();

            // Erstelle einen ServerSocketChannel und konfiguriere ihn auf nicht-blockierend
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            // Registriere den ServerSocketChannel für neue Verbindungen
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Listening on port " + port);

            // Server-Schleife
            while (running) {
                spin(serverSocketChannel, selector);
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
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
        System.out.println("New Client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // Client hat die Verbindung geschlossen
            System.out.println("Client closed connection!: " + clientChannel.getRemoteAddress());
            connectedClients.remove(clientChannel);
            clientChannel.close();
            key.cancel();
            return;
        }

        System.out.println("Buffer position: " + buffer.position());

        if (buffer.position() == 64) {
            buffer.flip();
            ClientPacketType clientPacketType = ClientPacketType.values()[buffer.getInt()];

            List<Packet<ServerPacketType>> responses = new ArrayList<>();

            for (PacketHandle handle : handles.get(clientPacketType)) {
                handle.handle(buffer.duplicate()).ifPresent(responses::add);
            }

            for (Packet<ServerPacketType> response : responses) {
                ByteBuffer dbuf = ByteBuffer.allocate(64);
                dbuf.putInt(response.getType().ordinal());
                response.toBytes(dbuf);

                clientChannel.write(dbuf);
            }

            buffer.clear();
        }
    }

    public void send(String playerId, Packet<ServerPacketType> packet) {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(packet.getType().ordinal());
        packet.toBytes(buffer);

        try {
            playerClients.get(playerId).write(buffer);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void broadcast(Packet<ServerPacketType> packet) {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(packet.getType().ordinal());
        packet.toBytes(buffer);

        try {
            for (SocketChannel client : connectedClients) {
                System.out.println("Packet send to client " + client.getRemoteAddress());
                client.write(buffer.duplicate());
                System.out.println(client.isOpen());
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
    }
}
