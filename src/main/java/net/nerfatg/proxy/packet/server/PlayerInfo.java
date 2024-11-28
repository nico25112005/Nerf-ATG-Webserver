package net.nerfatg.proxy.packet.server;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class PlayerInfo extends Packet<ServerPacketType> {

    private String playerId;
    private String playerName;
    private int teamIndex;

    public PlayerInfo(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ServerPacketType.GameInfo);

        fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferOverflowException {
        byte[] playerIdBytes = new byte[6];
        buffer.get(playerIdBytes);
        playerId = new String(playerIdBytes);

        byte[] playerNameBytes = new byte[16];
        buffer.get(playerNameBytes);
        playerName = new String(playerNameBytes);

        teamIndex = buffer.getInt();
    }

    @Override
    public void toBytes(ByteBuffer buffer) throws BufferOverflowException {
        buffer.put(playerId.getBytes());
        buffer.put(playerName.getBytes());
        buffer.putInt(teamIndex);
    }

}
