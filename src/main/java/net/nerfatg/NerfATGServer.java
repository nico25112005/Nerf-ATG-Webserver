package net.nerfatg;

import jline.console.ConsoleReader;
import net.nerfatg.command.CommandHandler;
import net.nerfatg.command.CommandScanner;
import net.nerfatg.command.commands.BroadcastCommand;
import net.nerfatg.command.commands.CompletionTestCommand;
import net.nerfatg.command.commands.HelpCommand;
import net.nerfatg.command.commands.LogTestCommand;
import net.nerfatg.command.commands.VersionCommand;
import net.nerfatg.game.GameHandler;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketType;
import net.nerfatg.task.Task;
import net.nerfatg.task.TaskScheduler;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class NerfATGServer {

    private static final NerfLogger logger = LoggerFactory.getLogger("Server");

    public static void initializeLoggingWithConsole(ConsoleReader consoleReader) {
        LoggerFactory.setDefaultLogDirectory(new File("logs"));
        LoggerFactory.setDefaultUseColors(true);
        LoggerFactory.setDefaultConsoleReader(consoleReader);
    }

    public static final String VERSION = "0.1.0";

    private final Properties properties;

    private final TaskScheduler taskScheduler;
    private final CommandHandler commandHandler;

    private final ConsoleReader consoleReader;

    private final Proxy proxy;
    private final GameHandler gameHandler;

    public NerfATGServer() throws IOException {
        logger.info("Initializing NerfATG Server...");
        AnsiConsole.systemInstall();

        logger.debug("Loading server properties...");
        this.properties = new Properties();
        this.properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));
        logger.info("Server properties loaded successfully");

        logger.debug("Setting up console reader...");
        this.consoleReader = new ConsoleReader(System.in, System.out);
        
        // Debug jline configuration
        System.out.println("[DEBUG] ConsoleReader created: " + this.consoleReader.getClass().getName());
        System.out.println("[DEBUG] Terminal: " + this.consoleReader.getTerminal().getClass().getName());
        System.out.println("[DEBUG] Terminal supported: " + this.consoleReader.getTerminal().isSupported());
        System.out.println("[DEBUG] Terminal ANSI supported: " + this.consoleReader.getTerminal().isAnsiSupported());
        
        logger.info("Console reader created");

        logger.debug("Starting proxy on port 36676...");
        this.proxy = new Proxy(36676);
        this.gameHandler = new GameHandler();

        this.proxy.registerHandle(PacketType.CreateGame, this.gameHandler);
        logger.info("Proxy initialized and packet handlers registered");

        logger.debug("Initializing task scheduler...");
        this.taskScheduler = new TaskScheduler();

        this.commandHandler = new CommandHandler(this.taskScheduler, ' ');
        logger.info("Task scheduler and command handler initialized");

        logger.debug("Starting command handler task...");
        Task task = new Task("command-handler", this::startCommandHandler);
        this.taskScheduler.runRepeatingTask(task);
        logger.info("Command handler task started");

        logger.debug("Registering commands...");
        this.commandHandler.registerCommand(new HelpCommand("help"));
        this.commandHandler.registerCommand(new VersionCommand("version"));
        this.commandHandler.registerCommand(new BroadcastCommand("broadcast", this));
        this.commandHandler.registerCommand(new LogTestCommand("logtest"));
        this.commandHandler.registerCommand(new CompletionTestCommand("completion"));
        logger.info("Commands registered successfully");

        logger.debug("Configuring console reader...");
        
        // Add some debugging to verify the completer is being added
        logger.important("Adding CommandHandler as completer to ConsoleReader");
        System.out.println("[DEBUG] About to add completer: " + this.commandHandler.getClass().getName());
        
        // TEMPORARILY add a simple completer to test if jline completion works at all
        this.consoleReader.addCompleter(new net.nerfatg.command.SimpleCompleter());
        this.consoleReader.addCompleter(this.commandHandler);
        
        System.out.println("[DEBUG] Completers added successfully");
        
        this.consoleReader.setPrompt("> ");
        logger.info("Console reader configured with command completion");

        // TEMPORARILY DISABLE ConsoleManager to test if it's interfering with completion
        initializeLoggingWithConsole(this.consoleReader);
        logger.warning("ConsoleManager temporarily disabled for completion testing");
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

    private void displayInitialPrompt() {
        try {
            // Give a small delay to ensure all log messages are processed
            Thread.sleep(100);
            
            // Clear any remaining output and display the prompt
            this.consoleReader.print("\r\u001B[K");
            this.consoleReader.flush();
            
            // Display a welcome message and prompt
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🚀 NerfATG Server v" + VERSION + " is ready!");
            System.out.println("Type 'help' for available commands");
            System.out.println("=".repeat(50));
            
            // Ensure the prompt is displayed
            this.consoleReader.drawLine();
            this.consoleReader.flush();
        } catch (IOException | InterruptedException e) {
            logger.warning("Failed to display initial prompt: " + e.getMessage());
            // Fallback: just print the prompt
            System.out.print("> ");
            System.out.flush();
        }
    }

    private void launch(String[] args) {
        this.proxy.launch(this::displayInitialPrompt);
    }

    public Properties getProperties() {
        return properties;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public static void main(String[] args) {
        final NerfATGServer server;
        try {
            server = new NerfATGServer();
        } catch (IOException e) {
            logger.error("Failed to initialize server: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Add shutdown hook to cleanup logging
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.important("Server shutting down...");
            LoggerFactory.shutdownAll();
        }));

        server.launch(args);
    }
}
