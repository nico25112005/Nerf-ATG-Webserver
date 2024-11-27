package net.nerfatg.proxy;

import java.util.Optional;

public interface PacketHandle {

    Optional<byte[]> handle(byte[] bytes);

}
