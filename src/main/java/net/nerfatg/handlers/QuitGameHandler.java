package net.nerfatg.handlers;

import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameInfo;
import net.nerfatg.proxy.packet.packets.PlayerInfo;
import net.nerfatg.proxy.packet.packets.QuitGame;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public class QuitGameHandler implements PacketHandle {

    Server server;

    public QuitGameHandler(){
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {
        QuitGame qg = new QuitGame(buffer);

        Game game = server.getGame(server.getPlayerAttendingGame(qg.getPlayerId()));
        Player player = game.getPlayer(qg.getPlayerId());

        //Sender Res
        PacketHandleResponse senderRes = new PacketHandleResponse();

        senderRes.addPlayerId(qg.getPlayerId());
        senderRes.addResponsePacket(new QuitGame(qg.getPlayerId(), PacketAction.Generic));

        //Gamemember Res
        PacketHandleResponse gameMemberRes = new PacketHandleResponse();

        for (String mate : game.getPlayerList().keySet()) {
            gameMemberRes.addPlayerId(mate);
        }
        gameMemberRes.removePlayerId(qg.getPlayerId());

        gameMemberRes.addResponsePacket(new PlayerInfo(player.getId(), player.getName(), (byte)player.getTeam().ordinal(), PacketAction.Remove));

        PacketHandleResponse broadcastRes = new PacketHandleResponse();
        broadcastRes.setServerBroadcast();


        if(Objects.equals(game.getGameId(), player.getId())){

            gameMemberRes.addResponsePacket(new QuitGame(qg.getPlayerId(), PacketAction.Generic));

            //Broadcast Res
            broadcastRes.addResponsePacket(new GameInfo(game.getGameType(),game.getGameId(), game.getGameName(), game.getPlayerCount(), game.getMaxPlayers(), PacketAction.Remove));
        }
        else{
            broadcastRes.addResponsePacket(new GameInfo(game.getGameType(), game.getGameId(), game.getGameName(), (byte)(game.getPlayerCount() - 1), game.getMaxPlayers(), PacketAction.Update));
        }

        server.removePlayerFromGame(game.getGameId(), qg.getPlayerId());

        if(Objects.equals(game.getGameId(), qg.getPlayerId())){
            server.removeGame(game.getGameId());
        }

        return List.of(senderRes, gameMemberRes,broadcastRes);
    }
}
