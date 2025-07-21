package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ReadyPlayerCount extends Packet {
    private byte readyPlayers;

    public ReadyPlayerCount(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public ReadyPlayerCount(byte readyPlayers, PacketAction action) {
        super(PacketType.ReadyPlayerCount, action);
        this.readyPlayers = readyPlayers;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        this.readyPlayers = buffer.get();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.put(readyPlayers);
    }

    public byte getReadyPlayers() { return readyPlayers; }

    @Override
    public String toString() {
        return String.format("ReadyPlayerCount{ readyPlayerCount=%d }", readyPlayers);
    }
}