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

        fromBytes(buffer);
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
    public byte[] toBytes(int size) throws BufferOverflowException {
        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putInt(gameType.ordinal());
        buffer.put(gameId.getBytes());
        buffer.put(gameName.getBytes());
        buffer.putInt(playerCount);
        buffer.putInt(maxPlayer);

        return buffer.array();
    }
}
