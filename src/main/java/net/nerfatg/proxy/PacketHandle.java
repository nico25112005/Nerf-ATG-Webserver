package net.nerfatg.proxy;

import net.nerfatg.proxy.packet.Packet;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public interface PacketHandle {

    List<PacketHandleResponse> handle(ByteBuffer buffer);

}
