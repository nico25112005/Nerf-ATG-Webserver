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

    public Map<String, Game> getGameList(){
        return Collections.unmodifiableMap(gameList);
    }

    public Game getGame(String gameId){
        return getGameList().get(gameId);
    }

    public Map<String, String> getPlayerInGame() {return Collections.unmodifiableMap(playerInGame); }

    public String getPlayerAttendingGame(String playerId){
        return playerInGame.get(playerId);
    }

    public Map<String, Player> getNotInGame() {return Collections.unmodifiableMap(notInGame); }


    public void addNewGame(Game game){
        gameList.put(game.getGameId(), game);
    }

    public void addOrReplacePlayerInGame(String gameId, String playerId){
        gameList.get(gameId).addOrReplacePlayer(notInGame.get(playerId));
        playerInGame.put(playerId, gameId);
        notInGame.remove(playerId);
    }

    public void removePlayerFromGame(String gameId, String playerId){
        Player player = gameList.get(gameId).getPlayerList().get(playerId);
        notInGame.put(playerId, new Player(player.getId(), player.getName())); //reseting the player so it only holds the id and name
        gameList.get(gameId).removePlayer(playerId);
        playerInGame.remove(playerId);
    }

    public void playerConnectedToServer(Player player){
        notInGame.put(player.getId(), player);
    }

    public void playerDisconectedFromServer(String playerid){
        if(notInGame.containsKey(playerid)){
            notInGame.remove(playerid);
        }
        else{
            removePlayerFromGame(getPlayerAttendingGame(playerid), playerid);
        }
    }
}
