package net.nerfatg.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {

    private static final String WEB_ROOT = "web";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        String requestedFile = WEB_ROOT + path;
        if (path.contains("..") || path.startsWith("/api/")) {
            send404(exchange);
            return;
        }

        URL resource = getClass().getClassLoader().getResource(requestedFile);
        if (resource == null) {
            send404(exchange);
            return;
        }

        String contentType = guessContentType(path);
        exchange.getResponseHeaders().set("Content-Type", contentType);

        try (InputStream in = resource.openStream(); OutputStream out = exchange.getResponseBody()) {
            byte[] data = in.readAllBytes();
            exchange.sendResponseHeaders(200, data.length);
            out.write(data);
        }
        exchange.close();
    }

    private void send404(HttpExchange exchange) throws IOException {
        byte[] message = "Not found".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, message.length);
        exchange.getResponseBody().write(message);
        exchange.close();
    }

    private String guessContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
