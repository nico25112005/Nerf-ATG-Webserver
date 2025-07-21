package net.nerfatg.proxy.packet.packets;

import net.nerfatg.game.GameType;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class CreateGame extends Packet {

    private String playerId;
    private GameType gameType;
    private String gameName;
    private byte maxPlayer;

    public CreateGame(ByteBuffer buffer) {
        super(buffer);
    }

    public CreateGame(String playerId, GameType gameType, String gameName, byte maxPlayer, PacketAction action) {
        super(PacketType.CreateGame, action);

        this.playerId = playerId;
        this.gameType = gameType;
        this.gameName = gameName;
        this.maxPlayer = maxPlayer;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, java.nio.charset.StandardCharsets.UTF_8);
        
        this.gameType = GameType.values()[buffer.getInt()];
        
        byte[] gameNameBytes = new byte[12];
        buffer.get(gameNameBytes);
        this.gameName = new String(gameNameBytes, java.nio.charset.StandardCharsets.UTF_8).trim().replace("\0", "");
        
        this.maxPlayer = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        
        // Write playerId (8 bytes fixed)
        byte[] originalPlayerIdBytes = playerId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalPlayerIdBytes, 0, Math.min(originalPlayerIdBytes.length, 8));
        if (originalPlayerIdBytes.length < 8) {
            buffer.put(new byte[8 - originalPlayerIdBytes.length]); // pad with zeros
        }
        
        buffer.putInt(gameType.ordinal());
        
        // Write gameName (12 bytes fixed)
        byte[] originalGameNameBytes = gameName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalGameNameBytes, 0, Math.min(originalGameNameBytes.length, 12));
        if (originalGameNameBytes.length < 12) {
            buffer.put(new byte[12 - originalGameNameBytes.length]); // pad with zeros
        }
        
        buffer.put(maxPlayer);
    }

    public String getPlayerId() { return playerId; }
    public GameType getGameType() { return gameType; }
    public String getGameName() { return gameName; }
    public byte getMaxPlayer() { return maxPlayer; }

    @Override
    public String toString() {
        return String.format("CreateGame{playerId='%s', gameType=%s, gameName='%s', maxPlayer=%d}", 
                           playerId, gameType, gameName, maxPlayer);
    }
}
