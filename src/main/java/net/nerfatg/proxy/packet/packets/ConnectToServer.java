package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConnectToServer extends Packet {
    private String playerId;
    private String playerName;

    public ConnectToServer(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public ConnectToServer(String playerId, String playerName, PacketAction action) {
        super(PacketType.PlayerInfo, action);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, StandardCharsets.UTF_8);
        
        byte[] nameBytes = new byte[12];
        buffer.get(nameBytes);
        this.playerName = new String(nameBytes, StandardCharsets.UTF_8).trim().replace("\0", "");
        
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
        byte[] originalNameBytes = playerName.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalNameBytes, 0, Math.min(originalNameBytes.length, 12));
        if (originalNameBytes.length < 12) {
            buffer.put(new byte[12 - originalNameBytes.length]); // pad with zeros
        }
        
    }

    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }

    @Override
    public String toString() {
        return String.format("PlayerInfo{playerId='%s', playerName='%s'}", playerId, playerName);
    }
}