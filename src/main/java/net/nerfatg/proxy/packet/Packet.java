package net.nerfatg.proxy.packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public abstract class Packet<T> {

    private final T type;

    public Packet(ByteBuffer buffer, T type) throws BufferOverflowException {

        this.fromBytes(buffer);
        this.type = type;
    }

    public Packet(T type) {
        this.type = type;
    }

    public T getType() {
        return type;
    }

    public abstract void fromBytes(ByteBuffer buffer) throws BufferOverflowException;
    public abstract void toBytes(ByteBuffer buffer) throws BufferOverflowException;
}