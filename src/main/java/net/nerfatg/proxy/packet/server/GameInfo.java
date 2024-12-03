package net.nerfatg.proxy.packet.server;

import net.nerfatg.game.GameType;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.client.ClientPacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class GameInfo extends Packet<ServerPacketType> {

    private GameType gameType;
    private String gameId;
    private String gameName;
    private int playerCount;
    private int maxPlayer;

    public GameInfo(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ServerPacketType.GameInfo);
    }

    public GameInfo(GameType gameType, String gameId, String gameName, int playerCount, int maxPlayer) {
        super(ServerPacketType.GameInfo);
        this.gameType = gameType;
        this.gameId = gameId;
        this.gameName = gameName;
        this.playerCount = playerCount;
        this.maxPlayer = maxPlayer;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferOverflowException {
        gameType = GameType.values()[buffer.getInt()];

        byte[] gameIdBytes = new byte[5];
        buffer.get(gameIdBytes);
        gameId = new String(gameIdBytes);

        byte[] gameNameBytes = new byte[16];
        buffer.get(gameNameBytes);
        gameName = new String(gameNameBytes);

        playerCount = buffer.getInt();
        maxPlayer = buffer.getInt();
    }

    @Override
    public void toBytes(ByteBuffer buffer) throws BufferOverflowException {
        buffer.putInt(gameType.ordinal());
        buffer.put(gameId.getBytes());
        buffer.put(gameName.getBytes());
        buffer.putInt(playerCount);
        buffer.putInt(maxPlayer);
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "gameType=" + gameType +
                ", gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", playerCount=" + playerCount +
                ", maxPlayer=" + maxPlayer +
                '}';
    }
}
