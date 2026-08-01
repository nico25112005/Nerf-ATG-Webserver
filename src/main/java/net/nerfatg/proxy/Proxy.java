package net.nerfatg.proxy;

import net.nerfatg.data.Server;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.proxy.packet.packets.ConnectToServer;
import net.nerfatg.proxy.packet.packets.QuitGame;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proxy {

    private static final Set<SocketChannel> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private static final HashMap<String, SocketChannel> playerClients = new HashMap<>();

    private static long packetsReceived = 0;
    private static long packetsSent = 0;
    private static long totalConnections = 0;
    private static long currentConnections = 0;
    private static long errorCount = 0;

    private static final Map<PacketType, Long> packetsByType = new ConcurrentHashMap<>();
    private static final Deque<String> connectionLog = new ConcurrentLinkedDeque<>();
    private static final int MAX_CONN_LOG = 50;

    private final int port;

    private boolean running;
    private final HashMap<PacketType, List<PacketHandle>> handles;

    public Proxy(int port) {
        this.handles = new HashMap<>();
        for (PacketType type : PacketType.values()) {
            this.handles.put(type, new ArrayList<>());
        }

        this.port = port;
    }

    public void registerHandle(PacketType type, PacketHandle handle) {
        handles.get(type).add(handle);
    }

    public void unregister(PacketType type, PacketHandle handle) {
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

            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.INFO, "Listening on port " + port);

            // Server-Schleife
            while (running) {
                try {
                    spin(serverSocketChannel, selector);
                } catch (IOException e) {
                    errorCount++;
                    Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, e.getMessage());
                }
            }
        } catch (IOException e) {
            errorCount++;
            Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, e.getMessage());
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
        totalConnections++;
        currentConnections++;
        connectionLog.addLast("[" + new Date() + "] CONNECT " + clientChannel.getRemoteAddress());
        while (connectionLog.size() > MAX_CONN_LOG) connectionLog.pollFirst();
        Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "Client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // Client hat die Verbindung geschlossen
            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "Client closed connection!: " + clientChannel.getRemoteAddress());
            connectedClients.remove(clientChannel);
            currentConnections--;
            connectionLog.addLast("[" + new Date() + "] DISCONNECT " + clientChannel.getRemoteAddress());
            while (connectionLog.size() > MAX_CONN_LOG) connectionLog.pollFirst();
            String playerId = playerClients.entrySet()
                    .stream()
                    .filter(e -> Objects.equals(e.getValue(), clientChannel))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);


            Server server = Server.Initalize();

            if(server.getPlayerInGame().containsKey(playerId)){
                ByteBuffer temp = ByteBuffer.allocate(64);
                new QuitGame(playerId, PacketAction.Generic).toBytes(temp);
                handlePacket(temp, clientChannel);
            }
            server.playerDisconectedFromServer(playerId);

            if(playerId != null)
                playerClients.remove(playerId);

            key.cancel();
        }

        if (buffer.position() == 64) {
            handlePacket(buffer, clientChannel);
            buffer.clear();
        }
    }

    public void handlePacket(ByteBuffer buffer, SocketChannel clientChannel) throws IOException {
        packetsReceived++;
        buffer.flip();
        PacketType clientPacketType = PacketType.values()[buffer.get()];
        packetsByType.merge(clientPacketType, 1L, Long::sum);

        if(clientPacketType == PacketType.ConnectToServer){
            playerClients.put(new ConnectToServer(buffer).getPlayerId(), clientChannel);
        }
        if(clientPacketType != PacketType.Ping && clientPacketType != PacketType.PlayerStatus)
            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.INFO, "Server received packet: " + clientPacketType);

        List<PacketHandleResponse> responses = new ArrayList<>();

        for (PacketHandle handle : handles.get(clientPacketType)) {
            responses.addAll(handle.handle(buffer.duplicate()));
        }

        for (PacketHandleResponse response : responses) {
            for (Packet packet : response.getResponsePackets()){

                ByteBuffer dbuf = ByteBuffer.allocate(64);
                packet.toBytes(dbuf);
                dbuf.position(0);

                if(response.getServerBroadcast()){

                    for(SocketChannel socket : playerClients.values()){
                        socket.write(dbuf.duplicate());

                        if(clientPacketType != PacketType.Ping && clientPacketType != PacketType.PlayerStatus)
                            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "Send Packet: " + packet + " To: " + socket);
                    }
                }
                else{

                    for(String playerId : response.getPlayerIds()){
                        if(clientPacketType != PacketType.Ping && clientPacketType != PacketType.PlayerStatus)
                            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "Send Packet: " + packet + " To: " + playerId);
                        //Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, Arrays.toString(dbuf.duplicate().array()));

                        try {
                            SocketChannel channel = playerClients.get(playerId);
                            if (channel != null) {
                                channel.write(dbuf.duplicate());
                                packetsSent++;
                            }
                        } catch (IOException e) {
                            errorCount++;
                            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.SEVERE, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void send(String playerId, Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(packet.getType().ordinal());
        packet.toBytes(buffer);
        SocketChannel channel = playerClients.get(playerId);
        if (channel == null) {
            errorCount++;
            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.WARNING, "No client found for playerId: " + playerId);
            return;
        }
        try {
            channel.write(buffer);
        } catch (IOException e) {
            errorCount++;
            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void broadcast(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        packet.toBytes(buffer);
        buffer.position(0);

        try {
            for (SocketChannel client : connectedClients) {
                Logger.getLogger(Proxy.class.getSimpleName()).log(Level.INFO, "Packet sent to client " + client.getRemoteAddress());
                Logger.getLogger(Proxy.class.getSimpleName()).log(Level.INFO, Arrays.toString(buffer.array()));

                client.write(buffer);
                packetsSent++;
            }
        } catch (IOException e) {
            errorCount++;
            Logger.getLogger(Proxy.class.getSimpleName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
    }

    public HashMap<String, SocketChannel> getPlayerClients(){
        return playerClients;
    }

    public static long getPacketsReceived() {
        return packetsReceived;
    }

    public static long getPacketsSent() {
        return packetsSent;
    }

    public static long getTotalConnections() {
        return totalConnections;
    }

    public static long getCurrentConnections() {
        return currentConnections;
    }

    public static long getErrorCount() {
        return errorCount;
    }

    public static Map<PacketType, Long> getPacketsByType() {
        return Collections.unmodifiableMap(packetsByType);
    }

    public static Deque<String> getConnectionLog() {
        return connectionLog;
    }
}
