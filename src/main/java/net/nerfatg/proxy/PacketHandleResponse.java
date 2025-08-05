package net.nerfatg.proxy;

import net.nerfatg.proxy.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketHandleResponse {

    private List<String> playerIds = new ArrayList<String>(){};
    private List<Packet> responsePackets = new ArrayList<Packet>(){};

    private boolean serverBroadcast = false;

    public PacketHandleResponse(){}

    public PacketHandleResponse(ArrayList<String> playerIds, ArrayList<Packet> responsePackets, boolean serverBroadcast){
        this.responsePackets = responsePackets;
        this.playerIds = playerIds;
        this.serverBroadcast = serverBroadcast;
    }

    public void addResponsePacket(Packet packet){
        responsePackets.add(packet);
    }

    public void addPlayerId(String playerId){
        playerIds.add(playerId);
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public List<Packet> getResponsePackets() {
        return responsePackets;
    }

    public boolean getServerBroadcast() {
        return serverBroadcast;
    }

    public void setServerBroadcast() {
        this.serverBroadcast = true;
    }
}
