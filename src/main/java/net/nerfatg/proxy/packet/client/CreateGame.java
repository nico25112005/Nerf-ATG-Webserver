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

        fromBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[6];

        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);

        gameType = GameType.values()[buffer.getInt()];

        byte[] nameBytes = new byte[16];
        buffer.get(nameBytes);

        gameName = new String(nameBytes);
    }

    @Override
    public byte[] toBytes(int size) throws BufferUnderflowException {
        ByteBuffer dbuf = ByteBuffer.allocate(size);

        dbuf.put(playerId.getBytes());
        dbuf.putInt(gameType.ordinal());
        dbuf.put(gameName.getBytes());

        return dbuf.array();
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
}
