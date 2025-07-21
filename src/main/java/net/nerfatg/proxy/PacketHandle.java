package net.nerfatg.proxy;

import net.nerfatg.proxy.packet.Packet;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface PacketHandle {

    Optional<Packet> handle(ByteBuffer buffer);

}
