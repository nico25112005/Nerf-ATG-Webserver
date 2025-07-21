package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class GameStarted extends Packet {

    private String leaderId;
    private String leaderName;
    private byte teamIndex;
    private byte playerCount;

    public GameStarted(ByteBuffer buffer) {
        super(buffer);
    }

    public GameStarted(String playerId, String leaderId, String leaderName, byte teamIndex, byte playerCount, PacketAction action) {
        super(PacketType.GameStarted, action);

        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.teamIndex = teamIndex;
        this.playerCount = playerCount;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] leaderIdBytes = new byte[8];
        buffer.get(leaderIdBytes);
        this.leaderId = new String(leaderIdBytes, java.nio.charset.StandardCharsets.UTF_8);
        
        byte[] leaderNameBytes = new byte[12];
        buffer.get(leaderNameBytes);
        this.leaderName = new String(leaderNameBytes, java.nio.charset.StandardCharsets.UTF_8).trim().replace("\0", "");
        
        this.playerCount = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        
        // Write leaderId (8 bytes fixed)
        byte[] originalLeaderIdBytes = leaderId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalLeaderIdBytes, 0, Math.min(originalLeaderIdBytes.length, 8));
        if (originalLeaderIdBytes.length < 8) {
            buffer.put(new byte[8 - originalLeaderIdBytes.length]); // pad with zeros
        }
        
        // Write leaderName (12 bytes fixed)
        byte[] originalLeaderNameBytes = leaderName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalLeaderNameBytes, 0, Math.min(originalLeaderNameBytes.length, 12));
        if (originalLeaderNameBytes.length < 12) {
            buffer.put(new byte[12 - originalLeaderNameBytes.length]); // pad with zeros
        }
        
        buffer.put(playerCount);
    }

    public String getLeaderId() { return leaderId; }
    public String getLeaderName() { return leaderName; }
    public byte getTeamIndex() { return teamIndex; }
    public byte getPlayerCount() { return playerCount; }

    @Override
    public String toString() {
        return String.format("GameStarted{leaderId='%s', leaderName='%s', teamIndex=%d, playerCount=%d}", 
                           leaderId, leaderName, teamIndex, playerCount);
    }
}
