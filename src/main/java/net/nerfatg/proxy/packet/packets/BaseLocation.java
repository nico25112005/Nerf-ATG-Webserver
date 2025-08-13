package net.nerfatg.proxy.packet.packets;

import net.nerfatg.Utils.GPS;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.Utils.GPS;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BaseLocation extends Packet {

    private String playerId;
    private byte teamIndex;
    private double longitude;
    private double latitude;

    public BaseLocation(ByteBuffer buffer) throws BufferOverflowException {
        super(buffer);
    }

    public BaseLocation(String playerId, byte teamIndex, GPS gps, PacketAction action) {
        super(PacketType.BaseLocation, action);

        this.playerId = playerId;
        this.teamIndex = teamIndex;
        this.longitude = gps.getLongitude();
        this.latitude = gps.getLatitude();
    }

    @Override
    public void readPayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] playerIdBytes = new byte[8];
        buffer.get(playerIdBytes);
        this.playerId = new String(playerIdBytes, java.nio.charset.StandardCharsets.UTF_8);

        this.teamIndex = buffer.get();
        this.longitude = buffer.getDouble();
        this.latitude = buffer.getDouble();
    }

    @Override
    public void writePayload(ByteBuffer buffer, int offset) throws BufferOverflowException {
        buffer.position(offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write playerId (8 bytes fixed)
        byte[] originalPlayerIdBytes = playerId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buffer.put(originalPlayerIdBytes, 0, Math.min(originalPlayerIdBytes.length, 8));
        if (originalPlayerIdBytes.length < 8) {
            buffer.put(new byte[8 - originalPlayerIdBytes.length]); // pad with zeros
        }

        buffer.put(teamIndex);
        buffer.putDouble(longitude);
        buffer.putDouble(latitude);
    }

    public String getPlayerId() {return playerId; }
    public byte getTeamIndex() { return teamIndex; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    @Override
    public String toString() {
        return String.format("BaseLocation{teamIndex=%d, longitude=%f, latitude=%f}", 
                           teamIndex, longitude, latitude);
    }
}
