package net.nerfatg;

import net.nerfatg.game.GameHandler;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.client.ClientPacketType;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

public class NerfATGServer {

    /*
        create table players (
            id BIGINT NOT NULL auto_increment,
            name VARCHAR(20),
            longitude DOUBLE,
            latitude DOUBLE,
        PRIMARY KEY (id)
    );
     */

    public static void loadLoggerConfiguration() {
        InputStream stream = NerfATGServer.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Properties properties;
    private final SessionFactory factory;

    private final Proxy proxy;
    private final GameHandler gameHandler;

    public NerfATGServer() throws IOException {
        this.properties = new Properties();
        this.properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));

        try {
            factory = new Configuration()
                .addResource("Player.hbm.xml").configure().buildSessionFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        this.proxy = new Proxy(25565);
        this.gameHandler = new GameHandler();

        this.proxy.registerHandle(ClientPacketType.CreateGame, this.gameHandler);
    }

    private void launch(String[] args) {
        this.proxy.launch();
    }

    public Properties getProperties() {
        return properties;
    }

    public static void main(String[] args) {
        loadLoggerConfiguration();

        final NerfATGServer server;
        try {
            server = new NerfATGServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.launch(args);
    }
}
