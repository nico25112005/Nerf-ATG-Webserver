package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlayerStatus extends Packet {
    private String playerId;
    private String name;
    private byte index;
    private double longitude;
    private double latitude;
    private byte health;

    public PlayerStatus(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public PlayerStatus(String playerId, String name, byte index, double longitude, double latitude, byte health, PacketAction action) {
        super(PacketType.PlayerStatus, action);
        this.playerId = playerId;
        this.name = name;
        this.index = index;
        this.longitude = longitude;
        this.latitude = latitude;
        this.health = health;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, StandardCharsets.UTF_8);
        
        byte[] nameBytes = new byte[12];
        buffer.get(nameBytes);
        this.name = new String(nameBytes, StandardCharsets.UTF_8).trim().replace("\0", "");
        
        this.index = buffer.get();
        this.longitude = buffer.getDouble();
        this.latitude = buffer.getDouble();
        this.health = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        
        // Write playerId (8 bytes fixed)
        byte[] originalPlayerIdBytes = playerId.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalPlayerIdBytes, 0, Math.min(originalPlayerIdBytes.length, 8));
        if (originalPlayerIdBytes.length < 8) {
            buffer.put(new byte[8 - originalPlayerIdBytes.length]); // pad with zeros
        }
        
        // Write name (12 bytes fixed)
        byte[] originalNameBytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalNameBytes, 0, Math.min(originalNameBytes.length, 12));
        if (originalNameBytes.length < 12) {
            buffer.put(new byte[12 - originalNameBytes.length]); // pad with zeros
        }
        
        buffer.put(index);
        buffer.putDouble(longitude);
        buffer.putDouble(latitude);
        buffer.put(health);
    }

    public String getPlayerId() { return playerId; }
    public String getName() { return name; }
    public byte getIndex() { return index; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
    public byte getHealth() { return health; }

    @Override
    public String toString() {
        return String.format("PlayerStatus{playerId='%s', playerName='%s', teamIndex=%d, longitude=%f, latitude=%f, health=%d}", 
                           playerId, name, index, longitude, latitude, health);
    }
}