package net.nerfatg.proxy.packet.packets;

import net.nerfatg.game.GameType;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class GameInfo extends Packet {

    private GameType gameType;
    private String gameId;
    private String gameName;

    private byte playerCount;
    private byte maxPlayer;

    public GameInfo(ByteBuffer buffer) {
        super(buffer);
    }

    public GameInfo(GameType gameType, String gameId, String gameName, byte playerCount, byte maxPlayer, PacketAction action) {
        super(PacketType.GameInfo, action);

        this.gameType = gameType;
        this.gameId = gameId;
        this.gameName = gameName;
        this.playerCount = playerCount;
        this.maxPlayer = maxPlayer;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        this.gameType = GameType.values()[buffer.getInt()];
        
        byte[] gameIdBytes = new byte[8];
        buffer.get(gameIdBytes);
        this.gameId = new String(gameIdBytes, java.nio.charset.StandardCharsets.UTF_8);
        
        byte[] gameNameBytes = new byte[12];
        buffer.get(gameNameBytes);
        this.gameName = new String(gameNameBytes, java.nio.charset.StandardCharsets.UTF_8).trim().replace("\0", "");
        
        this.playerCount = buffer.get();
        this.maxPlayer = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.put((byte)gameType.ordinal());
        
        // Write gameId (8 bytes fixed)
        byte[] originalGameIdBytes = gameId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalGameIdBytes, 0, Math.min(originalGameIdBytes.length, 8));
        if (originalGameIdBytes.length < 8) {
            buffer.put(new byte[8 - originalGameIdBytes.length]); // pad with zeros
        }
        
        // Write gameName (12 bytes fixed)
        byte[] originalGameNameBytes = gameName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalGameNameBytes, 0, Math.min(originalGameNameBytes.length, 12));
        if (originalGameNameBytes.length < 12) {
            buffer.put(new byte[12 - originalGameNameBytes.length]); // pad with zeros
        }
        
        buffer.put(playerCount);
        buffer.put(maxPlayer);
    }

    public GameType getGameType() { return gameType; }
    public String getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public byte getPlayerCount() { return playerCount; }
    public byte getMaxPlayer() { return maxPlayer; }

    @Override
    public String toString() {
        return String.format("GameInfo{gameType=%s, gameId='%s', gameName='%s', playerCount=%d, maxPlayer=%d}", 
                           gameType, gameId, gameName, playerCount, maxPlayer);
    }
}
