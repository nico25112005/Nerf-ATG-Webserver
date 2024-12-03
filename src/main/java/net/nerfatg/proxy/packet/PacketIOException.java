package net.nerfatg.proxy.packet;

public class PacketIOException extends Exception {

    public PacketIOException(Packet<?> packet) {
        super(String.format("Error while processing packet of type %s", packet.getClass()));
    }


}
