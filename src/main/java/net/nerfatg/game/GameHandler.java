package net.nerfatg.game;

import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.client.CreateGame;
import net.nerfatg.proxy.packet.server.ServerPacketType;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Optional;

public class GameHandler implements PacketHandle {

    private final HashMap<String, Game> games;

    public GameHandler() {
        this.games = new HashMap<>();
    }

    public void add(Game game) {
        games.put(game.getGameId(), game);
    }

    @Override
    public Optional<Packet<ServerPacketType>> handle(ByteBuffer buffer) {
        CreateGame packet = new CreateGame(buffer);
        Game game = new Game("9D6A3", packet.getPlayerId(), packet.getGameType(), packet.getGameName());
        add(game);

        return Optional.empty();
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }
}
