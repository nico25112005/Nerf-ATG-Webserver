import net.nerfatg.game.GameType;
import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.Packet;
import net.nerfatg.proxy.packet.client.ClientPacketType;
import net.nerfatg.proxy.packet.client.CreateGame;
import net.nerfatg.proxy.packet.server.GameInfo;
import net.nerfatg.proxy.packet.server.PlayerStatus;
import net.nerfatg.proxy.packet.server.ServerPacketType;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ProxyTest {

    private static class Handler implements PacketHandle {

        @Override
        public Optional<Packet<ServerPacketType>> handle(ByteBuffer buffer) {
            // System.out.println(new String(bytes));

            CreateGame packet = new CreateGame(buffer);
            Logger.getLogger("Server").info("Server received client packet: " + packet);
            System.out.println(packet);

            GameInfo gameInfo = new GameInfo(packet.getGameType(), "D512A", packet.getGameName(), 0, 10);

            return Optional.of(gameInfo);
        }
    }

    @Test
    public void receiveTest() throws InterruptedException {
        Proxy proxy = new Proxy(25565);
        proxy.registerHandle(ClientPacketType.CreateGame, new Handler());

        new Thread(proxy::launch).start();

        Thread.sleep(5000);

        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 25565)) {
                    Logger.getLogger("Client").info("Connected to server");

                    CreateGame packet = new CreateGame("A90DA31AD316", GameType.Team, "Testgame");

                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    buffer.putInt(ClientPacketType.CreateGame.ordinal());
                    packet.toBytes(buffer);

                    socket.getOutputStream().write(buffer.array());
                } catch (IOException e) {
                    Logger.getLogger("Client").warning(e.getMessage());
                }
            }).start();
        }
    }


    public void sendTest() throws InterruptedException {
        Proxy proxy = new Proxy(25564);
        proxy.registerHandle(ClientPacketType.CreateGame, new Handler());

        new Thread(proxy::launch).start();

        AtomicInteger finishedCount = new AtomicInteger(0);

        Thread.sleep(5000);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 25564)) {
                    while (socket.getInputStream().available() == 0);

                    byte[] bytes = new byte[64];
                    socket.getInputStream().read(bytes, 0, Math.min(socket.getInputStream().available(), bytes.length));

                    ByteBuffer dbuf = ByteBuffer.wrap(bytes);
                    ServerPacketType packetType = ServerPacketType.values()[dbuf.getInt()];
                    System.out.println("Received packet from Server!!!!!: " + packetType);

                    GameInfo gameInfo = new GameInfo(dbuf);
                    System.out.println("Packet: " + gameInfo);

                    finishedCount.incrementAndGet();
                } catch (IOException e) {
                    System.err.println("Fehler beim Verbinden oder Kommunizieren mit dem Server: " + e.getMessage());
                }
            }).start();
        }

        Thread.sleep(1000);

        proxy.broadcast(
                new GameInfo(GameType.Team, "DA21AC", "TestGame", 0, 10)
        );

        while (finishedCount.get() < 5) {}
    }










}
