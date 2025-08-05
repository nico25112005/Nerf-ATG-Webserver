package net.nerfatg.handlers;

import net.nerfatg.Utils.GameType;
import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.JoinGame;
import net.nerfatg.proxy.packet.packets.PlayerInfo;

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

        List<PacketHandleResponse> responses = new ArrayList<>();

        JoinGame jg = new JoinGame(buffer);
        PacketHandleResponse res = new PacketHandleResponse();
        Game game = server.getGame(jg.getGameId());

        for(Player player : game.getPlayerList().values()){
            res.addPlayerId(player.getId());
        }

        if(jg.getAction() == PacketAction.Add){
            Team team;

            server.addOrReplacePlayerInGame(jg.getGameId(), jg.getPlayerId());

            if(game.getGameType() == GameType.FreeForAll){
                team = Team.Violet;
            }
            else{
                team = Team.values()[(game.getPlayerCount() % 2)];
            }

            game.getPlayer(jg.getPlayerId()).setTeam(team);

            res.addResponsePacket(new PlayerInfo(jg.getPlayerId(), game.getPlayer(jg.getPlayerId()).getName(), (byte)team.ordinal(), PacketAction.Add));

        } else if (jg.getAction() == PacketAction.Remove) {
            Player player = game.getPlayer(jg.getPlayerId());

            res.addResponsePacket(new PlayerInfo(jg.getPlayerId(), player.getName(), (byte)player.getTeam().ordinal(), PacketAction.Remove));

            server.removePlayerFromGame(jg.getGameId(), jg.getPlayerId());
        }

        responses.add(res);


        return responses;
    }
}
