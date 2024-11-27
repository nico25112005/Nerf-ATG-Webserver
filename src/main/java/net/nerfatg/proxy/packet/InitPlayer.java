package net.nerfatg.proxy.packet;

import net.nerfatg.game.TeamType;
import net.nerfatg.game.WeaponType;
import net.nerfatg.proxy.PacketType;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class InitPlayer extends Packet {
    private int maxHealth;
    private int maxAmmunition;
    private float damping;

    private TeamType teamType;
    private WeaponType weaponType;

    public InitPlayer(ByteBuffer buffer) {
        super(buffer, PacketType.InitPlayer);
    }

    @Override
    public void fromBytes(ByteBuffer buffer) throws BufferUnderflowException {
        maxHealth = buffer.getInt();
        maxAmmunition = buffer.getInt();
        damping = buffer.getFloat();
        teamType = TeamType.values()[buffer.getInt()];
        weaponType = WeaponType.values()[buffer.getInt()];
    }

    @Override
    public byte[] toBytes(int size) throws BufferUnderflowException {
        ByteBuffer dbuf = ByteBuffer.allocate(size);

        dbuf.putInt(maxHealth);
        dbuf.putInt(maxAmmunition);
        dbuf.putFloat(damping);
        dbuf.putInt(teamType.ordinal());
        dbuf.putInt(weaponType.ordinal());

        return dbuf.array();
    }
}
