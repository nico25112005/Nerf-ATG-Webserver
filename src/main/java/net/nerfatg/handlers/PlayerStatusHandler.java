package net.nerfatg.handlers;

import net.nerfatg.Utils.GPS;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.packets.PlayerStatus;

import java.nio.ByteBuffer;
import java.util.List;

public class PlayerStatusHandler implements PacketHandle {

    Server server;

    public PlayerStatusHandler(){
        server = Server.Initalize();
    }


    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {
        PlayerStatus ps = new PlayerStatus(buffer);

        Game game = server.getGame(server.getPlayerAttendingGame(ps.getPlayerId()));
        if(game != null){
            Player player = game.getPlayer(ps.getPlayerId());

            player.setGps(new GPS(ps.getLongitude(), ps.getLatitude()));
            player.setHealth(ps.getHealth());

            PacketHandleResponse gameMemberRes = new PacketHandleResponse();

            for(String p : game.getPlayerList().keySet()){
                gameMemberRes.addPlayerId(p);
            }

            gameMemberRes.addResponsePacket(ps);

            return List.of(gameMemberRes);
        }
        else{
            PacketHandleResponse senderRes = new PacketHandleResponse();

            return List.of();
        }
    }
}
