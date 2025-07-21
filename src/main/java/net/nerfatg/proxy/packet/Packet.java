package net.nerfatg.proxy.packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public abstract class Packet {

    private PacketType type;
    private PacketAction action;

    public Packet(ByteBuffer buffer) throws BufferOverflowException {
        this.fromBytes(buffer);
    }

    public Packet(PacketType type, PacketAction action) {
        this.type = type;
        this.action = action;
    }

    public PacketType getType() {
        return type;
    }

    public PacketAction getAction() {
        return action;
    }

    public void fromBytes(ByteBuffer buffer) {
        this.type = PacketType.values()[buffer.get(0)];
        this.action = PacketAction.values()[buffer.get(1)];
        this.readPayload(buffer, 4); // Payload begins after 4 bytes
    }

    public void toBytes(ByteBuffer buffer) {
        buffer.put(0, (byte) type.ordinal());
        buffer.put(1, (byte) action.ordinal());
        this.writePayload(buffer, 4); // Payload begins after 4 bytes
    }

    public abstract void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException;
    public abstract void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException;
}