package net.nerfatg.handlers;

import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerInfo;
import net.nerfatg.proxy.packet.packets.SwitchTeam;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SwitchTeamHandler implements PacketHandle {

    Server server;

    public SwitchTeamHandler() {
        server = Server.Initalize();
    }

    @Override
    public List<PacketHandleResponse> handle(ByteBuffer buffer) {

        List<PacketHandleResponse> responses = new ArrayList<>();

        SwitchTeam st = new SwitchTeam(buffer);
        PacketHandleResponse res = new PacketHandleResponse();

        Team team;
        Game game = server.getGame(server.getPlayerAttendingGame(st.getPlayerId()));
        Player player = game.getPlayer(st.getPlayerId());

        for(Player p : game.getPlayerList().values()){
            res.addPlayerId(p.getId());
        }

        team = (player.getTeam() == Team.Red) ? Team.Blue : Team.Red;

        res.addResponsePacket(new PlayerInfo(player.getId(), player.getName(), (byte)team.ordinal(), PacketAction.Update));

        return responses;
    }
}
