package net.nerfatg.proxy.packet;

import net.nerfatg.proxy.PacketType;

public class PacketIOException extends Exception {

    public PacketIOException(PacketType packetType) {
        super(String.format("Error while processing packet of type %s", packetType));
    }


}
