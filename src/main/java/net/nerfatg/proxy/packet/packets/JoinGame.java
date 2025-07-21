package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class JoinGame extends Packet {
    private String playerId;
    private String gameName;

    public JoinGame(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public JoinGame(String playerId, String gameName, PacketAction action) {
        super(PacketType.JoinGame, action);
        this.playerId = playerId;
        this.gameName = gameName;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, StandardCharsets.UTF_8);
        
        byte[] gameNameBytes = new byte[12];
        buffer.get(gameNameBytes);
        this.gameName = new String(gameNameBytes, StandardCharsets.UTF_8).trim().replace("\0", "");
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
        
        // Write gameName (12 bytes fixed)
        byte[] originalGameNameBytes = gameName.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalGameNameBytes, 0, Math.min(originalGameNameBytes.length, 12));
        if (originalGameNameBytes.length < 12) {
            buffer.put(new byte[12 - originalGameNameBytes.length]); // pad with zeros
        }
    }

    public String getPlayerId() { return playerId; }
    public String getGameName() { return gameName; }

    @Override
    public String toString() {
        return String.format("JoinGame{playerId='%s', gameName='%s'}", playerId, gameName);
    }
}
