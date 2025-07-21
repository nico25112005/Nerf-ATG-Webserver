package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StartGame extends Packet {
    private String playerId;

    public StartGame(ByteBuffer buffer) throws BufferOverflowException {
       super(buffer);
    }

    public StartGame(String playerId, PacketAction action) {
        super(PacketType.StartGame, action);
        this.playerId = playerId;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        byte[] playerIdBytes = new byte[8];
        buffer.position(offset);
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, StandardCharsets.UTF_8);
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
    }

    public String getPlayerId() {
        return playerId;
    }
}
