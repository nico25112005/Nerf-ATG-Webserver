package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlayerReady extends Packet {
    private String playerId;
    private byte health;
    private byte weapon;
    private byte damping;

    public PlayerReady(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public PlayerReady(String playerId, byte health, byte weapon, byte damping, PacketAction action) {
        super(PacketType.PlayerReady, action);
        this.playerId = playerId;
        this.health = health;
        this.weapon = weapon;
        this.damping = damping;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, StandardCharsets.UTF_8);
        this.health = buffer.get();
        this.weapon = buffer.get();
        this.damping = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        
        // Write playerId (8 bytes fixed)
        byte[] originalPlayerIdBytes = playerId.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalPlayerIdBytes, 0, Math.min(originalPlayerIdBytes.length, 8));
        if (originalPlayerIdBytes.length < 8) {
            buffer.put(new byte[8 - originalPlayerIdBytes.length]); // pad with zeros
        }
        
        buffer.put(health);
        buffer.put(weapon);
        buffer.put(damping);
    }

    public String getPlayerId() { return playerId; }
    public byte getHealth() { return health; }
    public byte getWeapon() { return weapon; }
    public byte getDamping() { return damping; }

    @Override
    public String toString() {
        return String.format("PlayerReady{playerId='%s', health=%d, weapon=%d, damping=%d}", playerId, health, weapon, damping);
    }
}