package net.nerfatg;

import net.nerfatg.entity.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.util.Properties;

public class NerfATGServer {

    private final Properties properties;
    private final SessionFactory factory;

    public NerfATGServer() throws IOException {
        this.properties = new Properties();
        this.properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));

        try {
            factory = new Configuration()
                .addResource("Player.hbm.xml").configure().buildSessionFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private void launch(String[] args) {
        addPlayer(new Player("GodGotzi", 42.1123123, 11.123123));
        addPlayer(new Player("Nico", 13.123123, 42.123123));
    }

    public long addPlayer(Player player) {
        Session session = factory.openSession();
        Transaction tx = null;
        long id = 0;

        try {
            tx = session.beginTransaction();
            id = (long)session.save(player);
            tx.commit();
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
        } finally {
            session.close();
        }

        return id;
    }

    public Properties getProperties() {
        return properties;
    }

    public static void main(String[] args) {
        final NerfATGServer server;
        try {
            server = new NerfATGServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.launch(args);
    }
}
