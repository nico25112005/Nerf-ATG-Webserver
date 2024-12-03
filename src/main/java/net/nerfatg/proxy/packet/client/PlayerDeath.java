package net.nerfatg.proxy.packet.client;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class PlayerDeath extends Packet<ClientPacketType> {

    private String playerId;

    public PlayerDeath(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.PlayerDeath);
    }

    public PlayerDeath(String playerId) {
        super(ClientPacketType.PlayerDeath);

        this.playerId = playerId;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[12];

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

    @Override
    public String toString() {
        return "PlayerDeath{" +
                "playerId='" + playerId + '\'' +
                '}';
    }
}
