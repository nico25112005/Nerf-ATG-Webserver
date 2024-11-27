package net.nerfatg.proxy;

import net.nerfatg.NerfATGServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proxy extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private final byte[] buffer = new byte[257];
    private final HashMap<PacketType, List<PacketHandle>> handles;

    public Proxy(NerfATGServer server) {
        this.handles = new HashMap<>();
        for (PacketType type : PacketType.values()) {
            this.handles.put(type, new ArrayList<>());
        }

        try {
            SocketAddress addr = new InetSocketAddress("localhost", 25565);
            this.socket = new DatagramSocket(addr);
        } catch (SocketException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void registerHandle(PacketType type, PacketHandle handle) {
        handles.get(type).add(handle);
    }

    public void unregister(PacketType type, PacketHandle handle) {
        handles.get(type).remove(handle);
    }

    @Override
    public void run() {
        running = true;

        try {
            while (running) {
                spin();
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        } finally {
            running = false;
            socket.close();
        }
    }

    public void spin() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        // first byte is packet type
        int index = buffer[0];
        PacketType type = PacketType.values()[index];

        InetAddress address = packet.getAddress();
        int port = packet.getPort();

        List<byte[]> responses = new ArrayList<>();

        synchronized (handles) {
            for (PacketHandle handle : handles.get(type)) {
                Optional<byte[]> bytes = handle.handle(packet.getData());
                bytes.ifPresent(responses::add);
            }
        }

        for (byte[] response : responses) {
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, address, port);
            socket.send(responsePacket);
        }
    }

    public void shutdown() {
        running = false;
    }
}
