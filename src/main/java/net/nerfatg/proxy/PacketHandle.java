package net.nerfatg.proxy;

import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.server.ServerPacketType;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface PacketHandle {

    Optional<Packet<ServerPacketType>> handle(ByteBuffer buffer);

}
