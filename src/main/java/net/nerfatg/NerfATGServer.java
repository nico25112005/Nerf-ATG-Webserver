package net.nerfatg;

import jline.console.ConsoleReader;
import net.nerfatg.command.CommandHandler;
import net.nerfatg.command.CommandScanner;
import net.nerfatg.command.commands.BroadcastCommand;
import net.nerfatg.command.commands.VersionCommand;
import net.nerfatg.command.commands.ConnectionsCommand;
import net.nerfatg.command.commands.SendPlayerInfoCommand;
import net.nerfatg.command.commands.SendGameInfoCommand;
import net.nerfatg.command.commands.SendCreateGameCommand;
import net.nerfatg.command.commands.SendJoinGameCommand;
import net.nerfatg.command.commands.SendSwitchTeamCommand;
import net.nerfatg.command.commands.SendStartGameCommand;
import net.nerfatg.command.commands.SendPlayerReadyCommand;
import net.nerfatg.command.commands.SendActivateAbilityCommand;
import net.nerfatg.command.commands.SendMapPointCommand;
import net.nerfatg.command.commands.SendPlayerStatusCommand;
import net.nerfatg.command.commands.SendGameStartedCommand;
import net.nerfatg.command.commands.SendReadyPlayerCountCommand;
import net.nerfatg.command.commands.SendBaseLocationCommand;
import net.nerfatg.game.GameHandler;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.task.Task;
import net.nerfatg.task.TaskScheduler;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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

    private final ConsoleReader consoleReader;

    private final Proxy proxy;
    private final GameHandler gameHandler;

    public NerfATGServer() throws IOException {
        this.properties = new Properties();
        this.properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));

        this.proxy = new Proxy(25115);
        this.gameHandler = new GameHandler();

        this.proxy.registerHandle(PacketType.CreateGame, this.gameHandler);

        this.taskScheduler = new TaskScheduler();
        this.commandHandler = new CommandHandler(this.taskScheduler,
                ' ');

        this.consoleReader = new ConsoleReader(System.in, System.out);
        this.consoleReader.addCompleter(this.commandHandler);
        this.consoleReader.setPrompt(">" + " ");

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
                return this.consoleReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        this.commandHandler.scanLoop(commandScanner);
    }

    private void launch(String[] args) {
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
