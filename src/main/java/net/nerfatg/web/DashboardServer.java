package net.nerfatg.web;

import com.sun.net.httpserver.HttpServer;
import net.nerfatg.data.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class DashboardServer {

    private static final Logger LOGGER = Logger.getLogger(DashboardServer.class.getName());

    private final HttpServer httpServer;
    private final Server server;
    private final int port;
    private LogCaptureHandler logCaptureHandler;

    public DashboardServer(Server server, Properties properties) throws IOException {
        this.server = server;
        this.port = Integer.parseInt(properties.getProperty("dashboard.port", "8080"));

        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        ApiHandler apiHandler = new ApiHandler(server);

        httpServer.createContext("/api/games", apiHandler);
        httpServer.createContext("/api/debug", apiHandler);
        httpServer.createContext("/", new StaticFileHandler());

        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() {
        // Attach log capture to root logger so we catch everything
        logCaptureHandler = new LogCaptureHandler();
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(logCaptureHandler);

        httpServer.start();
        LOGGER.info("Dashboard web server started on http://localhost:" + port);
    }

    public void stop() {
        httpServer.stop(0);
        if (logCaptureHandler != null) {
            Logger rootLogger = Logger.getLogger("");
            rootLogger.removeHandler(logCaptureHandler);
        }
        LOGGER.info("Dashboard web server stopped.");
    }
}