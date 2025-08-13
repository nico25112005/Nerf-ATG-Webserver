package net.nerfatg.handlers;

import com.sun.xml.bind.v2.runtime.reflect.Lister;
import net.nerfatg.Utils.GameType;
import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameInfo;
import net.nerfatg.proxy.packet.packets.JoinGame;
import net.nerfatg.proxy.packet.packets.PlayerInfo;
import net.nerfatg.proxy.packet.packets.ServerMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class JoinGameHandler implements PacketHandle {

    Server server;

    public JoinGameHandler(){
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        JoinGame jg = new JoinGame(buffer);

        Game game = server.getGame(jg.getGameId());

        if(game.getMaxPlayers() > game.getPlayerCount()) {
            //Sender Response

            PacketHandleResponse senderRes = new PacketHandleResponse();
            senderRes.addPlayerId(jg.getPlayerId());

            for (Player mates : game.getPlayerList().values()) {
                senderRes.addResponsePacket(new PlayerInfo(mates.getId(), mates.getName(), (byte) mates.getTeam().ordinal(), PacketAction.Add));
            }

            //GameMember Response
            PacketHandleResponse gameMemberRes = new PacketHandleResponse();

            for (Player player : game.getPlayerList().values()) {
                gameMemberRes.addPlayerId(player.getId());
            }

            gameMemberRes.addPlayerId(jg.getPlayerId()); //adding own player id

            Team team;

            server.addOrReplacePlayerInGame(jg.getGameId(), jg.getPlayerId());

            if (game.getGameType() == GameType.FreeForAll) {
                team = Team.Violet;
            } else {
                team = Team.values()[(game.getPlayerCount() % 2)];
            }

            game.getPlayer(jg.getPlayerId()).setTeam(team);

            gameMemberRes.addResponsePacket(new PlayerInfo(jg.getPlayerId(), game.getPlayer(jg.getPlayerId()).getName(), (byte) team.ordinal(), PacketAction.Add));



            //Broadcast Response
            PacketHandleResponse broadcastRes = new PacketHandleResponse();
            broadcastRes.setServerBroadcast();

            broadcastRes.addResponsePacket(new GameInfo(game.getGameType(), game.getGameId(), game.getGameName(), game.getPlayerCount(), game.getMaxPlayers(), PacketAction.Update));

            return List.of(senderRes, gameMemberRes, broadcastRes);
        }
        else{
            PacketHandleResponse res = new PacketHandleResponse();
            res.addPlayerId(jg.getPlayerId());
            res.addResponsePacket(new ServerMessage("Something is wrong: Already Full", PacketAction.Generic));
            return List.of();
        }
    }
}
