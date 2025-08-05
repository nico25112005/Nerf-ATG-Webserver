package net.nerfatg.handlers;

import net.nerfatg.data.Game;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.CreateGame;
import net.nerfatg.proxy.packet.packets.GameInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateGameHandler implements PacketHandle {

    private final Server server;


    public CreateGameHandler(){
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        List<PacketHandleResponse> responses = new ArrayList<>();

        CreateGame cg = new CreateGame(buffer);

        server.addNewGame(new Game(cg.getGameName(), cg.getPlayerId(), cg.getMaxPlayer(),cg.getGameType()));

        PacketHandleResponse res = new PacketHandleResponse();
        res.setServerBroadcast();
        res.addResponsePacket(new GameInfo(cg.getGameType(), cg.getPlayerId(), cg.getGameName(), (byte) 0, cg.getMaxPlayer(), PacketAction.Add));

        responses.add(res);


        return responses;
    }
}
