package net.nerfatg.handlers;

import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.packets.Ping;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PingHandler implements PacketHandle {


    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {
        Ping ping = new Ping(buffer);

        PacketHandleResponse senderRes = new PacketHandleResponse();
        senderRes.addPlayerId(ping.getPlayerId());
        senderRes.addResponsePacket(ping);


        return List.of(senderRes);
    }
}
