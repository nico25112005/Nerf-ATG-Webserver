package net.nerfatg.proxy.packet.client;

import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class JoinGame extends Packet<ClientPacketType> {
    private String playerId;
    private String playerName;
    private String gameName;

    public JoinGame(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.CreateGame);

        fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[6];

        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);

        byte[] nameBytes = new byte[16];
        buffer.get(nameBytes);
        playerName = new String(nameBytes);

        byte[] gameNameBytes = new byte[5];
        buffer.get(nameBytes);
        gameName = new String(gameNameBytes);
    }

    @Override
    public void toBytes(ByteBuffer dbuf) throws BufferUnderflowException {
        dbuf.put(playerId.getBytes());
        dbuf.put(playerName.getBytes());
        dbuf.put(gameName.getBytes());
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
