package net.nerfatg.handlers;

import net.nerfatg.Utils.WeaponType;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerReady;
import net.nerfatg.proxy.packet.packets.ReadyPlayerCount;

import java.nio.ByteBuffer;
import java.util.List;

public class PlayerReadyHandler implements PacketHandle {

    Server server;

    public  PlayerReadyHandler(){
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        PlayerReady pr = new PlayerReady(buffer);

        Game game = server.getGame(server.getPlayerAttendingGame(pr.getPlayerId()));
        Player player = game.getPlayer(pr.getPlayerId());

        game.addPlayersReady();
        player.setHealth(pr.getHealth());
        player.setWeaponType(WeaponType.values()[pr.getWeapon()]);
        //didn´t set dampining. forgot why I put it in the packet

        PacketHandleResponse gameMemberRes = new PacketHandleResponse();

        for(String p : game.getPlayerList().keySet()){
            gameMemberRes.addPlayerId(p);
        }

        gameMemberRes.addResponsePacket(new ReadyPlayerCount(game.getPlayersReady(), PacketAction.Generic));

        return List.of(gameMemberRes);
    }
}
