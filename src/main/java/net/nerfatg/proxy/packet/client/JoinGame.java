package net.nerfatg.proxy.packet.client;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class JoinGame extends Packet<ClientPacketType> {
    private String playerId;
    private String playerName;
    private String gameId;

    public JoinGame(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.JoinGame);
    }

    public JoinGame(String playerId, String playerName, String gameId) {
        super(ClientPacketType.JoinGame);
        this.playerId = playerId;
        this.playerName = playerName;
        this.gameId = gameId;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[12];

        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);

        byte[] nameBytes = new byte[16];
        buffer.get(nameBytes);
        playerName = new String(nameBytes);

        byte[] gameNameBytes = new byte[5];
        buffer.get(nameBytes);
        gameId = new String(gameNameBytes);
    }

    @Override
    public void toBytes(ByteBuffer dbuf) throws BufferUnderflowException {
        dbuf.put(playerId.getBytes());
        dbuf.put(playerName.getBytes());
        dbuf.put(gameId.getBytes());
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return "JoinGame{" +
                "playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", gameName='" + gameId + '\'' +
                '}';
    }
}
