package net.nerfatg.proxy.packet.client;

import net.nerfatg.game.GameType;
import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class CreateGame extends Packet<ClientPacketType> {
    private String playerId;
    private GameType gameType;
    private String gameName;

    public CreateGame(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.CreateGame);
    }

    public CreateGame(String playerId, GameType gameType, String gameName) {
        super(ClientPacketType.CreateGame);
        this.playerId = playerId;
        this.gameType = gameType;
        this.gameName = gameName;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[12];
        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);

        gameType = GameType.values()[buffer.getInt()];

        byte[] nameBytes = new byte[16];
        buffer.get(nameBytes);
        gameName = new String(nameBytes).trim();
    }

    @Override
    public void toBytes(ByteBuffer dbuf) throws BufferUnderflowException {
        dbuf.put(playerId.getBytes());
        dbuf.putInt(gameType.ordinal());
        dbuf.put(String.format("%-10s", gameName).getBytes());
    }

    public String getPlayerId() {
        return playerId;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public String toString() {
        return "CreateGame{" +
                "playerId='" + playerId + '\'' +
                ", gameType=" + gameType +
                ", gameName='" + gameName + '\'' +
                '}';
    }
}
