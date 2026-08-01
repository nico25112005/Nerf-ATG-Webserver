package net.nerfatg.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Map<String, Game> gameList = new HashMap<>();
    private final Map<String, String> playerInGame = new HashMap<>();
    private final Map<String, Player> notInGame = new HashMap<>();
    private static Server instance;

    private Server(){}

    public static Server Initalize(){
        if(instance == null){
            instance = new Server();
        }
        return instance;
    }

    private String trim(String s) {
        if (s == null) return null;
        return s.replace("\u0000", "").trim();
    }

    public Map<String, Game> getGameList(){
        return Collections.unmodifiableMap(gameList);
    }

    public Game getGame(String gameId){
        if (gameId == null) return null;
        Game g = gameList.get(gameId);
        if (g != null) return g;
        String trimmed = trim(gameId);
        for (Map.Entry<String, Game> entry : gameList.entrySet()) {
            if (trim(entry.getKey()).equals(trimmed)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<String, String> getPlayerInGame() {return Collections.unmodifiableMap(playerInGame); }

    public String getPlayerAttendingGame(String playerId){
        if (playerId == null) return null;
        String gid = playerInGame.get(playerId);
        if (gid != null) return gid;
        String trimmed = trim(playerId);
        for (Map.Entry<String, String> entry : playerInGame.entrySet()) {
            if (trim(entry.getKey()).equals(trimmed)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<String, Player> getNotInGame() {return Collections.unmodifiableMap(notInGame); }


    public void addNewGame(Game game){
        gameList.put(game.getGameId(), game);
    }

    public void removeGame(String gameId){
        Game g = getGame(gameId);
        if (g == null) return;
        for(String playerId : g.getPlayerList().keySet()){
            removePlayerFromGame(g.getGameId(), playerId);
        }
        gameList.remove(g.getGameId());
    }

    public void addOrReplacePlayerInGame(String gameId, String playerId){
        Game game = getGame(gameId);
        if (game == null) return;
        // Find the player in notInGame (with null-byte tolerant lookup)
        Player player = notInGame.get(playerId);
        if (player == null) {
            String trimmed = trim(playerId);
            for (Map.Entry<String, Player> entry : notInGame.entrySet()) {
                if (trim(entry.getKey()).equals(trimmed)) {
                    player = entry.getValue();
                    notInGame.remove(entry.getKey());
                    break;
                }
            }
        } else {
            notInGame.remove(playerId);
        }
        if (player == null) return;

        game.addOrReplacePlayer(player);
        playerInGame.put(playerId, gameId);
    }

    public void removePlayerFromGame(String gameId, String playerId){
        Game game = getGame(gameId);
        if (game == null) return;
        Player player = game.getPlayer(playerId);
        if (player == null) {
            // Try with trimmed id
            String trimmed = trim(playerId);
            for (Map.Entry<String, Player> entry : game.getPlayerList().entrySet()) {
                if (trim(entry.getKey()).equals(trimmed)) {
                    player = entry.getValue();
                    playerId = entry.getKey();
                    break;
                }
            }
        }
        if (player == null) return;

        notInGame.put(playerId, new Player(player.getId(), player.getName()));
        game.removePlayer(playerId);
        playerInGame.remove(playerId);
    }

    public void playerConnectedToServer(Player player){
        notInGame.put(player.getId(), player);
    }

    public void playerDisconectedFromServer(String playerid){
        if(playerid == null) return;
        if(notInGame.containsKey(playerid)){
            notInGame.remove(playerid);
        }
        else {
            // Try trimmed lookup
            String trimmed = trim(playerid);
            boolean found = false;
            for (String key : notInGame.keySet()) {
                if (trim(key).equals(trimmed)) {
                    notInGame.remove(key);
                    found = true;
                    break;
                }
            }
            if (!found) {
                String gameId = getPlayerAttendingGame(playerid);
                if (gameId != null) {
                    removePlayerFromGame(gameId, playerid);
                }
            }
        }
    }
}