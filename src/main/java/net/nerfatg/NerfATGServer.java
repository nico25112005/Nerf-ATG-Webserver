package net.nerfatg;

import com.sun.xml.bind.v2.runtime.reflect.Lister;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import net.nerfatg.command.CommandHandler;
import net.nerfatg.command.CommandScanner;
import net.nerfatg.command.commands.*;
import net.nerfatg.data.Server;
import net.nerfatg.handlers.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.web.DashboardServer;
import net.nerfatg.task.Task;
import net.nerfatg.task.TaskScheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

public class NerfATGServer {

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

    private final TaskScheduler taskScheduler;
    private final CommandHandler commandHandler;

    private final BufferedReader consoleReader;

    private final Proxy proxy;
    private final DashboardServer dashboardServer;

    public NerfATGServer() throws IOException {
        this.properties = new Properties();
        this.properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));

        this.proxy = new Proxy(25115);

        this.proxy.registerHandle(PacketType.ConnectToServer, new ConnectToServerHandler());
        this.proxy.registerHandle(PacketType.CreateGame, new CreateGameHandler());
        this.proxy.registerHandle(PacketType.JoinGame, new JoinGameHandler());
        this.proxy.registerHandle(PacketType.SwitchTeam, new SwitchTeamHandler());
        this.proxy.registerHandle(PacketType.StartGame, new StartGameHandler());
        this.proxy.registerHandle(PacketType.PlayerReady, new PlayerReadyHandler());
        this.proxy.registerHandle(PacketType.BaseLocation, new BaseLocationHandler());
        this.proxy.registerHandle(PacketType.PlayerStatus, new PlayerStatusHandler());
        this.proxy.registerHandle(PacketType.QuitGame, new QuitGameHandler());
        this.proxy.registerHandle(PacketType.Ping, new PingHandler());

        this.dashboardServer = new DashboardServer(Server.Initalize(), properties);

        this.taskScheduler = new TaskScheduler();
        this.commandHandler = new CommandHandler(this.taskScheduler,
                ' ');

        this.consoleReader = new BufferedReader(new InputStreamReader(System.in));

        Task task = new Task("command-handler", this::startCommandHandler);
        this.taskScheduler.runRepeatingTask(task);


        //this.commandHandler.registerCommand(new BroadcastCommand("broadcast", this));
        this.commandHandler.registerCommand(new ConnectionsCommand("connections", proxy.getPlayerClients()));
        this.commandHandler.registerCommand(new SendPlayerInfoCommand("sendplayerinfo", proxy));
        this.commandHandler.registerCommand(new SendGameInfoCommand("sendgameinfo", proxy));
        this.commandHandler.registerCommand(new SendCreateGameCommand("sendcreategame", proxy));
        this.commandHandler.registerCommand(new SendJoinGameCommand("sendjoingame", proxy));
        this.commandHandler.registerCommand(new SendSwitchTeamCommand("sendswitchteam", proxy));
        this.commandHandler.registerCommand(new SendStartGameCommand("sendstartgame", proxy));
        this.commandHandler.registerCommand(new SendPlayerReadyCommand("sendplayerready", proxy));
        this.commandHandler.registerCommand(new SendActivateAbilityCommand("sendactivateability", proxy));
        this.commandHandler.registerCommand(new SendMapPointCommand("sendmappoint", proxy));
        this.commandHandler.registerCommand(new SendPlayerStatusCommand("sendplayerstatus", proxy));
        this.commandHandler.registerCommand(new SendGameStartedCommand("sendgamestarted", proxy));
        this.commandHandler.registerCommand(new SendReadyPlayerCountCommand("sendreadyplayercount", proxy));
        this.commandHandler.registerCommand(new SendBaseLocationCommand("sendbaselocation", proxy));
    }

    private void startCommandHandler() {
        CommandScanner commandScanner = () -> {
            try {
                String line = this.consoleReader.readLine();
                return line != null ? line : "";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        this.commandHandler.scanLoop(commandScanner);
    }

    private void launch(String[] args) {
        this.dashboardServer.start();
        this.proxy.launch();
    }

    public Properties getProperties() {
        return properties;
    }

    public Proxy getProxy() {
        return proxy;
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
