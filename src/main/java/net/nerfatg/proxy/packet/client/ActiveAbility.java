package net.nerfatg.proxy.packet.client;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ActiveAbility extends Packet<ClientPacketType> {

    private String playerId;

    public ActiveAbility(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.AppStarted);

        fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[6];

        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);
    }

    @Override
    public void toBytes(ByteBuffer dbuf) throws BufferUnderflowException {
        dbuf.put(playerId.getBytes());
    }

    public String getPlayerId() {
        return playerId;
    }
}
