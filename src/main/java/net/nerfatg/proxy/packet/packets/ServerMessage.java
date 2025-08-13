package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ServerMessage extends Packet
{
    private String message;

    public ServerMessage(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }


    public ServerMessage(String message, PacketAction action) {
        super(PacketType.ServerMessage, action);

        this.message = message;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        byte[] playerIdBytes = new byte[32];
        buffer.get(playerIdBytes);
        this.message = new String(playerIdBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        
        // Write playerId (32 bytes fixed)
        byte[] originalPlayerIdBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalPlayerIdBytes, 0, Math.min(originalPlayerIdBytes.length, 32));
        if (originalPlayerIdBytes.length < 32) {
            buffer.put(new byte[32 - originalPlayerIdBytes.length]); // pad with zeros
        }
    }

    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("ServerMessage{message='%s'}", message);
    }
}
