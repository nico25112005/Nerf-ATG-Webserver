package net.nerfatg.game;

import java.util.UUID;

public class Game {

    private final String gameId;
    private final String hostId;
    private final GameType gameType;
    private final String gameName;

    public Game(String gameId, String hostId, GameType gameType, String gameName) {
        this.gameId = gameId;
        this.hostId = hostId;
        this.gameType = gameType;
        this.gameName = gameName;
    }

    public String getGameId() {
        return gameId;
    }

    public String getHostId() {
        return hostId;
    }

    public String getGameName() {
        return gameName;
    }

    public GameType getGameType() {
        return gameType;
    }
}
