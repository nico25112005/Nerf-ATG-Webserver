package net.nerfatg.proxy.packet.client;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class RefreshGames extends Packet<ClientPacketType> {
    private String playerId;

    public RefreshGames(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.RefreshGames);

        fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[6];

        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);
    }

    @Override
    public byte[] toBytes(int size) throws BufferUnderflowException {
        ByteBuffer dbuf = ByteBuffer.allocate(size);

        dbuf.put(playerId.getBytes());
        return dbuf.array();
    }

    public String getPlayerId() {
        return playerId;
    }
}
