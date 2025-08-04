package net.nerfatg.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Map<String, Game> gameList = new HashMap<>();
    private final Map<String, String> playerInGame = new HashMap<>();

    public Map<String, Game> getGameList(){
        return Collections.unmodifiableMap(gameList);
    }

    public void addNewGame(Game game){
        gameList.put(game.getGameId(), game);
    }

    public void addOrReplacePlayerInGame(String gameId, Player player){
        gameList.get(gameId).addOrReplacePlayer(player);
        playerInGame.put(player.getId(), gameId);
    }

    public void removePlayerFromGame(String gameId, String playerId){
        gameList.get(gameId).removePlayer(playerId);
        playerInGame.remove(playerId);
    }

}
