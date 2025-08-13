package net.nerfatg.handlers;

import net.nerfatg.Utils.GPS;
import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.packets.BaseLocation;

import java.nio.ByteBuffer;
import java.util.List;

public class BaseLocationHandler implements PacketHandle {

    Server server;

    public BaseLocationHandler(){
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        BaseLocation bl = new BaseLocation(buffer);

        Game game = server.getGame(server.getPlayerAttendingGame(bl.getPlayerId()));

        game.setBaseLocation(Team.values()[bl.getTeamIndex()], new GPS(bl.getLongitude(), bl.getLatitude()));

        PacketHandleResponse gameMemberRes = new PacketHandleResponse();

        for(String p : game.getPlayerList().keySet()){
            gameMemberRes.addPlayerId(p);
        }

        gameMemberRes.addResponsePacket(bl);

        return List.of(gameMemberRes);
    }
}
