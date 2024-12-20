package net.nerfatg.proxy.packet.client;

import net.nerfatg.game.WeaponType;
import net.nerfatg.proxy.packet.Packet;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class PlayerReady extends Packet<ClientPacketType> {

    private String playerId;
    private int health;
    private WeaponType weaponType;
    private float damping;

    public PlayerReady(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer, ClientPacketType.PlayerReady);
    }

    public PlayerReady(String playerId, int health, WeaponType weaponType, float damping) {
        super(ClientPacketType.PlayerReady);

        this.playerId = playerId;
        this.health = health;
        this.weaponType = weaponType;
        this.damping = damping;
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        byte[] macIdBytes = new byte[12];
        buffer.get(macIdBytes);
        playerId = new String(macIdBytes);

        health = buffer.getInt();
        weaponType = WeaponType.values()[buffer.getInt()];
        damping = buffer.getFloat();
    }

    @Override
    public void toBytes(ByteBuffer dbuf) throws BufferUnderflowException {
        dbuf.put(playerId.getBytes());
        dbuf.putInt(health);
        dbuf.putInt(weaponType.ordinal());
        dbuf.putFloat(damping);
    }

    public String getPlayerId() {
        return playerId;
    }

    public float getDamping() {
        return damping;
    }

    public int getHealth() {
        return health;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    @Override
    public String toString() {
        return "PlayerReady{" +
                "playerId='" + playerId + '\'' +
                ", health=" + health +
                ", weaponType=" + weaponType +
                ", damping=" + damping +
                '}';
    }
}
