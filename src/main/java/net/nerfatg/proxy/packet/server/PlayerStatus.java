package net.nerfatg.proxy.packet.server;

import net.nerfatg.game.GameType;
import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class PlayerStatus extends Packet<ServerPacketType> {

    private String playerId;
    private String playerName;
    private int teamIndex;
    private double longitude;
    private double latitude;
    private int health;

    public PlayerStatus(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ServerPacketType.PlayerStatus);
    }

    public PlayerStatus(String playerId, String playerName, int teamIndex, double longitude, double latitude, int health) {
        super(ServerPacketType.PlayerStatus);
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamIndex = teamIndex;
        this.longitude = longitude;
        this.latitude = latitude;
        this.health = health;
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
        longitude = buffer.getDouble();
        latitude = buffer.getDouble();
        health = buffer.getInt();
    }

    @Override
    public void toBytes(ByteBuffer buffer) throws BufferOverflowException {
        buffer.put(playerId.getBytes());
        buffer.put(playerName.getBytes());
        buffer.putInt(teamIndex);
        buffer.putDouble(longitude);
        buffer.putDouble(latitude);
        buffer.putInt(health);
    }

    @Override
    public String toString() {
        return "PlayerStatus{" +
                "playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", teamIndex=" + teamIndex +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", health=" + health +
                '}';
    }
}
