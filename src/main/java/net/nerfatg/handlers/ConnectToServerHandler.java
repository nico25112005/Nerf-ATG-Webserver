package net.nerfatg.handlers;

import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.ConnectToServer;
import net.nerfatg.proxy.packet.packets.GameInfo;

import java.nio.ByteBuffer;
import java.util.List;

public class ConnectToServerHandler implements PacketHandle {

    Server server;

    public ConnectToServerHandler(){
        server = Server.Initalize();
    }


    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        ConnectToServer connToServer = new ConnectToServer(buffer);

        server.playerConnectedToServer(new Player(connToServer.getPlayerId(), connToServer.getPlayerName()));

        PacketHandleResponse res = new PacketHandleResponse();
        res.addPlayerId(connToServer.getPlayerId());

        for(Game game : server.getGameList().values()){
            res.addResponsePacket(new GameInfo(game.getGameType(), game.getGameId(), game.getGameName(), game.getPlayerCount(), game.getMaxPlayers(), PacketAction.Add));
        }

        return List.of(res);
    }
}
