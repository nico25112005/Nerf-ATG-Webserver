package net.nerfatg.entity;

import net.nerfatg.Utils.GPS;
import net.nerfatg.Utils.GameType;
import net.nerfatg.Utils.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Game {


    private final String gameName;
    private final String gameId;
    private final byte maxPlayers;

    private byte playerCount = 0;
    private byte playersReady = 0;

    private final GameType gameType;

    private final Map<String, Player> playerList = new HashMap<>();
    private final Map<Team, GPS> baseLocationList = new HashMap<>();
    private final Map<Team, String> teamleaders = new HashMap<>();

    public Game(String gameName, String gameId, byte maxPlayers, GameType gameType) {
        this.gameName = gameName;
        this.gameId = gameId;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
    }

    public String getGameId() {
        return gameId;
    }

    public byte getMaxPlayers() {
        return maxPlayers;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getGameName() {
        return gameName;
    }


    public byte getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(byte playerCount) {
        this.playerCount = playerCount;
    }


    public byte getPlayersReady() {
        return playersReady;
    }

    public void setPlayersReady(byte playersReady) {
        this.playersReady = playersReady;
    }


    public Map<String, Player> getPlayerList() {
        return Collections.unmodifiableMap(playerList);
    }

    public void addOrReplacePlayer(Player player) {
        playerList.put(player.getId(), player);
    }

    public void removePlayer(String playerId) {
        playerList.remove(playerId);
    }


    public Map<Team, GPS> getBaseLocationList() {
        return Collections.unmodifiableMap(baseLocationList);
    }

    public void setBaseLocation(Team team, GPS gps) {
        baseLocationList.put(team, gps);
    }

    public void removeBaseLocation(Team team) {
        baseLocationList.remove(team);
    }


    public Map<Team, String> getTeamleader() {
        return Collections.unmodifiableMap(teamleaders);
    }

    public void setTeamLeader(Team team, String playerId) {
        teamleaders.put(team, playerId);
    }

}
