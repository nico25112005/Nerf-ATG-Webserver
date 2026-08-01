package net.nerfatg.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.nerfatg.Utils.GPS;
import net.nerfatg.Utils.Team;
import net.nerfatg.data.Game;
import net.nerfatg.data.Player;
import net.nerfatg.data.Server;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.NerfATGServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiHandler implements HttpHandler {

    private final Server server;

    public ApiHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String response;
        int status = 200;

        try {
            if ("/api/games".equals(path)) {
                response = listGames();
            } else if (path.startsWith("/api/games/") && path.endsWith("/players")) {
                String gameId = extractGameId(path, "/players");
                response = listPlayers(gameId);
            } else if (path.startsWith("/api/games/") && path.endsWith("/bases")) {
                String gameId = extractGameId(path, "/bases");
                response = listBases(gameId);
            } else if (path.startsWith("/api/games/")) {
                String gameId = path.substring("/api/games/".length());
                response = gameDetail(trimId(gameId));
            } else if ("/api/debug/logs".equals(path)) {
                response = LogCaptureHandler.getAllLogsAsJson();
            } else if ("/api/debug/status".equals(path)) {
                response = debugStatus();
            } else if ("/api/debug/packet-types".equals(path)) {
                response = debugPacketTypes();
            } else if ("/api/debug/connections".equals(path)) {
                response = debugConnections();
            } else {
                response = "{\"error\":\"Not found\"}";
                status = 404;
            }
        } catch (Exception e) {
            response = "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}";
            status = 500;
        }

        sendResponse(exchange, status, response);
    }

    private String extractGameId(String path, String suffix) {
        String base = "/api/games/";
        int end = path.length() - suffix.length();
        return path.substring(base.length(), end);
    }

    private String listGames() {
        Map<String, Game> games = server.getGameList();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Game game : games.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(gameSummaryJson(game));
        }
        sb.append("]");
        return sb.toString();
    }

    private String gameDetail(String gameId) {
        Game game = server.getGame(trimId(gameId));
        if (game == null) {
            return "{\"error\":\"Game not found\"}";
        }
        return gameDetailJson(game);
    }

    private String listPlayers(String gameId) {
        Game game = server.getGame(trimId(gameId));
        if (game == null) {
            return "{\"error\":\"Game not found\"}";
        }
        return playersJson(game);
    }

    private String listBases(String gameId) {
        Game game = server.getGame(trimId(gameId));
        if (game == null) {
            return "{\"error\":\"Game not found\"}";
        }
        return basesJson(game);
    }

    private String gameSummaryJson(Game game) {
        return "{"
                + "\"gameId\":\"" + escapeJson(trimId(game.getGameId())) + "\""
                + ",\"gameName\":\"" + escapeJson(game.getGameName()) + "\""
                + ",\"gameType\":\"" + game.getGameType().name() + "\""
                + ",\"maxPlayers\":" + game.getMaxPlayers()
                + ",\"playerCount\":" + game.getPlayerCount()
                + ",\"playersReady\":" + game.getPlayersReady()
                + "}";
    }

    private String gameDetailJson(Game game) {
        return "{"
                + "\"gameId\":\"" + escapeJson(trimId(game.getGameId())) + "\""
                + ",\"gameName\":\"" + escapeJson(game.getGameName()) + "\""
                + ",\"gameType\":\"" + game.getGameType().name() + "\""
                + ",\"maxPlayers\":" + game.getMaxPlayers()
                + ",\"playerCount\":" + game.getPlayerCount()
                + ",\"playersReady\":" + game.getPlayersReady()
                + ",\"players\":" + playersJson(game)
                + ",\"bases\":" + basesJson(game)
                + "}";
    }

    private String playersJson(Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Player player : game.getPlayerList().values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(playerJson(player));
        }
        sb.append("]");
        return sb.toString();
    }

    private String trimId(String id) {
        if (id == null) return "";
        return id.replace("\u0000", "").trim();
    }

    private String playerJson(Player player) {
        GPS gps = player.getGps();
        String team = player.getTeam() != null ? "\"" + player.getTeam().name() + "\"" : "null";
        String weapon = player.getWeaponType() != null ? "\"" + player.getWeaponType().name() + "\"" : "null";
        String lat = gps != null ? String.valueOf(gps.getLatitude()) : "null";
        String lon = gps != null ? String.valueOf(gps.getLongitude()) : "null";

        return "{"
                + "\"id\":\"" + escapeJson(trimId(player.getId())) + "\""
                + ",\"name\":\"" + escapeJson(player.getName()) + "\""
                + ",\"health\":" + player.getHealth()
                + ",\"team\":" + team
                + ",\"weaponType\":" + weapon
                + ",\"lat\":" + lat
                + ",\"lon\":" + lon
                + "}";
    }

    private String basesJson(Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map.Entry<Team, GPS> entry : game.getBaseLocationList().entrySet()) {
            if (!first) sb.append(",");
            first = false;
            GPS gps = entry.getValue();
            sb.append("{"
                    + "\"team\":\"" + entry.getKey().name() + "\""
                    + ",\"lat\":" + gps.getLatitude()
                    + ",\"lon\":" + gps.getLongitude()
                    + "}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String debugPacketTypes() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        Map<PacketType, Long> counts = Proxy.getPacketsByType();
        for (PacketType type : PacketType.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(type.name())).append("\":");
            sb.append(counts.getOrDefault(type, 0L));
        }
        sb.append("}");
        return sb.toString();
    }

    private String debugConnections() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (String entry : Proxy.getConnectionLog()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String debugStatus() {
        Runtime rt = Runtime.getRuntime();
        return "{"
                + "\"usedMemory\":" + (rt.totalMemory() - rt.freeMemory())
                + ",\"freeMemory\":" + rt.freeMemory()
                + ",\"maxMemory\":" + rt.maxMemory()
                + ",\"processors\":" + rt.availableProcessors()
                + ",\"threadCount\":" + Thread.activeCount()
                + ",\"gameCount\":" + server.getGameList().size()
                + ",\"playersInGame\":" + server.getPlayerInGame().size()
                + ",\"playersNotInGame\":" + server.getNotInGame().size()
                + ",\"packetsReceived\":" + Proxy.getPacketsReceived()
                + ",\"packetsSent\":" + Proxy.getPacketsSent()
                + ",\"totalConnections\":" + Proxy.getTotalConnections()
                + ",\"currentConnections\":" + Proxy.getCurrentConnections()
                + ",\"uptimeMs\":" + (System.currentTimeMillis() - NerfATGServer.START_TIME)
                + ",\"timestamp\":" + System.currentTimeMillis()
                + "}";
    }

    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
