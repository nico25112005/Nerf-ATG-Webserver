package net.nerfatg.proxy.packet.packets;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class BaseLocation extends Packet {

    private byte teamIndex;
    private double longitude;
    private double latitude;

    public BaseLocation(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public BaseLocation(byte teamIndex, double longitude, double latitude, PacketAction action) {
        super(PacketType.BaseLocation, action);

        this.teamIndex = teamIndex;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        this.teamIndex = buffer.get();
        this.longitude = buffer.getDouble();
        this.latitude = buffer.getDouble();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.put(teamIndex);
        buffer.putDouble(longitude);
        buffer.putDouble(latitude);
    }

    public byte getTeamIndex() { return teamIndex; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    @Override
    public String toString() {
        return String.format("BaseLocation{teamIndex=%d, longitude=%f, latitude=%f}", 
                           teamIndex, longitude, latitude);
    }
}
