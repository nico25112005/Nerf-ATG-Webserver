package net.nerfatg.proxy;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface PacketHandle {

    Optional<byte[]> handle(ByteBuffer buffer);

}
