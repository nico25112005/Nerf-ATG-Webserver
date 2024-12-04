package net.nerfatg.proxy.packet.server;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class PlayerInfo extends Packet<ServerPacketType> {

    private String playerId;
    private String playerName;
    private int teamIndex;

    public PlayerInfo(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ServerPacketType.PlayerInfo);
    }

    public PlayerInfo(String playerId, String playerName, int teamIndex) {
        super(ServerPacketType.PlayerInfo);
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamIndex = teamIndex;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferOverflowException {
        byte[] playerIdBytes = new byte[12];
        buffer.get(playerIdBytes);
        playerId = new String(playerIdBytes);

        byte[] playerNameBytes = new byte[16];
        buffer.get(playerNameBytes);
        playerName = new String(playerNameBytes);

        teamIndex = buffer.getInt();
    }

    @Override
    public void toBytes(ByteBuffer buffer) throws BufferOverflowException {
        buffer.put(String.format("%s-12", playerId).getBytes());
        buffer.put(String.format("%s-16", playerName).getBytes());
        buffer.putInt(teamIndex);
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", teamIndex=" + teamIndex +
                '}';
    }
}
