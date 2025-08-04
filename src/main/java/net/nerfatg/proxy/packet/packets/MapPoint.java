package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class MapPoint extends Packet {
    private String name;
    private byte index;
    private double longitude;
    private double latitude;

    public MapPoint(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public MapPoint(String name, byte index, double longitude, double latitude, PacketAction action) {
        super(PacketType.MapPoint, action);
        this.name = name;
        this.index = index;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] nameBytes = new byte[12];
        buffer.get(nameBytes);
        this.name = new String(nameBytes, StandardCharsets.UTF_8).trim().replace("\0", "");
        this.index = buffer.get();
        this.longitude = buffer.getDouble();
        this.latitude = buffer.getDouble();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write name (12 bytes fixed)
        byte[] originalNameBytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.put(originalNameBytes, 0, Math.min(originalNameBytes.length, 12));
        if (originalNameBytes.length < 12) {
            buffer.put(new byte[12 - originalNameBytes.length]); // pad with zeros
        }
        
        buffer.put(index);
        buffer.putDouble(longitude);
        buffer.putDouble(latitude);
    }

    public String getName() { return name; }
    public byte getIndex() { return index; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    @Override
    public String toString() {
        return String.format("MapPoint{ Name='%s', Index=%d, longitude=%f, latitude=%f }", name, index, longitude, latitude);
    }
}