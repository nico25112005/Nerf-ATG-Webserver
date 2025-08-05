import net.nerfatg.proxy.PacketHandle;
import net.nerfatg.proxy.PacketHandleResponse;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.proxy.packet.packets.CreateGame;
import org.junit.Test;
import net.nerfatg.Utils.GameType;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static net.nerfatg.NerfATGServer.loadLoggerConfiguration;

public class ProxyTest {

    private static class Handler implements PacketHandle {

        @Override
        public List<PacketHandleResponse> handle(ByteBuffer buffer) {
            // System.out.println(new String(bytes));

            CreateGame packet = new CreateGame(buffer);
            Logger.getLogger("Server").info("Server received client packet: " + packet);

            // GameInfo gameInfo = new GameInfo(packet.getGameType(), "D512A", packet.getGameName(), 0, 10, PacketAction.Add);


            return new ArrayList<>();
        }
    }

    @Test
    public void receiveTest() throws InterruptedException {
        loadLoggerConfiguration();

        /*
        Proxy proxy = new Proxy(25115);
        proxy.registerHandle(PacketType.CreateGame, new Handler());

        new Thread(proxy::launch).start();

        Thread.sleep(5000);
        */
        final int clientCount = 10;

        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < clientCount; i++) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 25115)) {
                    Logger.getLogger("Client").info("Connected to server");

                    CreateGame packet = new CreateGame("A90DA31AD316", GameType.TeamDeathMatch, "Testgame", (byte)0, PacketAction.Generic);

                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    buffer.putInt(PacketType.CreateGame.ordinal());
                    packet.toBytes(buffer);

                    socket.getOutputStream().write(buffer.array());

                    Thread.sleep(180000);
                    counter.incrementAndGet();
                } catch (IOException | InterruptedException e) {
                    Logger.getLogger("Client").warning(e.getMessage());
                }
            }).start();
        }

        while (counter.get() < clientCount) {
            Thread.sleep(100);
        }
    }

    @Test
    public void sendTest() throws InterruptedException {
        AtomicInteger finishedCount = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 25115)) {
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    byte[] bytes = new byte[64];
                    in.readFully(bytes); // wartet blockierend auf 64 Bytes

                    ByteBuffer dbuf = ByteBuffer.wrap(bytes);
                    PacketType packetType = PacketType.values()[dbuf.get()];

                    System.out.println("Received packet from Server!!!!!: " + packetType);
                    System.out.println(Arrays.toString(dbuf.array()));

                    finishedCount.incrementAndGet();
                } catch (IOException e) {
                    System.err.println("Fehler beim Verbinden oder Kommunizieren mit dem Server: " + e.getMessage());
                }
            }).start();
        }


        Thread.sleep(1000);

        while (finishedCount.get() < 5) {}
    }










}
