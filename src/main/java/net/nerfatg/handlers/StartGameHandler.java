package net.nerfatg.handlers;

import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameInfo;
import net.nerfatg.proxy.packet.packets.GameStarted;
import net.nerfatg.proxy.packet.packets.ServerMessage;
import net.nerfatg.proxy.packet.packets.StartGame;

import java.nio.ByteBuffer;
import java.util.*;

public class StartGameHandler implements PacketHandle {

    Server server;

    public StartGameHandler(){
        server = Server.Initalize();
    }


    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        StartGame sg = new StartGame(buffer);

        Game game = server.getGame(server.getPlayerAttendingGame(sg.getPlayerId()));

        boolean differentTeams = game.getPlayerList().values()
                .stream()
                .map(Player::getTeam)
                .distinct()
                .count() > 1;


        if (!differentTeams) {

            //Gamemember Response
            PacketHandleResponse gameMemberRes = new PacketHandleResponse();

            for (String p : game.getPlayerList().keySet()) {
                gameMemberRes.addPlayerId(p);
            }

            Random random = new Random();

            Player player1 = game.getPlayer(new ArrayList<>(game.getPlayerList().keySet()).get(random.nextInt(game.getPlayerCount())));
            Player player2 = game.getPlayer(new ArrayList<>(game.getPlayerList().keySet()).get(random.nextInt(game.getPlayerCount())));

            gameMemberRes.addResponsePacket(new GameStarted(player1.getId(), player1.getName(), (byte) player1.getTeam().ordinal(), game.getPlayerCount(), PacketAction.Generic));

            while (player2.getTeam() == player1.getTeam()) {
                player2 = game.getPlayer(new ArrayList<>(game.getPlayerList().keySet()).get(random.nextInt(game.getPlayerCount())));
            }

            gameMemberRes.addResponsePacket(new GameStarted(player2.getId(), player2.getName(), (byte) player2.getTeam().ordinal(), game.getPlayerCount(), PacketAction.Generic));

            //Boradcast Response

            PacketHandleResponse broadcastRes = new PacketHandleResponse();
            broadcastRes.setServerBroadcast();

            game.setGameRunning();
            broadcastRes.addResponsePacket(new GameInfo(game.getGameType(), game.getGameId(), game.getGameName(), game.getPlayerCount(), (byte) 0, PacketAction.Update));

            return List.of(gameMemberRes, broadcastRes);
        }
        else{
            PacketHandleResponse senderRes = new PacketHandleResponse();
            senderRes.addPlayerId(sg.getPlayerId());
            senderRes.addResponsePacket(new ServerMessage("Error: Only one Team!", PacketAction.Generic));

            return List.of(senderRes);
        }
    }
}
