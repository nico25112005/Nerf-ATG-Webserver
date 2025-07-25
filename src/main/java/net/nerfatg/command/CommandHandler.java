/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

import jline.console.completer.Completer;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;
import net.nerfatg.task.AsyncTaskScheduler;
import net.nerfatg.task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CommandHandler implements Completer {

    private final Properties properties = new Properties();
    private final Map<String, Command> commandMap = new LinkedHashMap<>();
    private final char commandChar;
    private final AsyncTaskScheduler<Task> taskScheduler;
    private NerfLogger logger;

    public CommandHandler(AsyncTaskScheduler<Task> taskScheduler, char commandChar) {
        this.commandChar = commandChar;
        this.taskScheduler = taskScheduler;
        this.logger = LoggerFactory.getLogger("CommandHandler");

        InputStream in = CommandHandler.class.getClassLoader().getResourceAsStream("command-handler.properties");

        try {
            properties.load(in);
            logger.info("Command handler properties loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load command-handler.properties: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
        
        logger.info("CommandHandler initialized with command character: '" + commandChar + "'");
        logger.info("Registered commands will be: " + commandMap.keySet());
    }

    public void scanLoop(CommandScanner commandScanner) {
        String scan;

        try {
            scan = commandScanner.scan();
            if (scan != null && !scan.isEmpty()) {
                scan = scan.trim();

                if (isCommand(scan)) {
                    startCommandExecuteTask(scan);
                }
            }
        } catch (Exception e) {
            logger.error("Error in command scan loop: " + e.getMessage());
        }
    }

    private void startCommandExecuteTask(String line) {
        Task task = new Task("command-execute", () -> executeCommand(line));
        taskScheduler.runTask(task);
    }

    /**
     * It takes a GCommand object and adds it to the commandMap HashMap
     *
     * @param command The command you want to register.
     */
    public synchronized void registerCommand(Command command) {
        if (logger != null) logger.fine("Registering command: " + command.getLabel());
        command.setLogger(this.logger);
        commandMap.put(command.getLabel(), command);
    }

    private boolean isCommand(String line) {
        if (line.charAt(0) == commandChar || commandChar == ' ') {
            if (commandChar != ' ') line = line.substring(1);
            String[] cmdSplit = line.split(" ", 2);

            if (!commandMap.containsKey(cmdSplit[0])) {
                logger.warning("Command not found: " + cmdSplit[0]);
                logger.info("Available commands: " + String.join(", ", commandMap.keySet()));
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * It takes a string, splits it into a command and arguments, and then executes the command with the arguments
     *
     * @param line The line of text that was sent to the bot.
     */
    public synchronized void executeCommand(String line) {
        if (logger != null) logger.fine("Executing command line: " + line);
        if (commandChar != ' ') line = line.substring(1);
        String[] cmdSplit = line.split(" ", 2);
        if (cmdSplit.length < 2) executeCommand(cmdSplit[0], new String[]{});
        else executeCommand(cmdSplit[0], cmdSplit[1].split(" "));
    }

    /**
     * It executes a command
     *
     * @param cmd The command name
     * @param args The arguments of the command.
     */
    public void executeCommand(String cmd, String[] args) {
        logger.fine("Executing command: " + cmd + " with args: " + Arrays.toString(args));
        
        try {
            Command command = commandMap.get(cmd);
            if (command != null) {
                command.execute(new CommandContext(cmd, args, properties));
            }
        } catch (Exception e) {
            logger.error("Error executing command '" + cmd + "': " + e.getMessage());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setLogger(NerfLogger logger) {
        this.logger = logger;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // Force output to original System.out to ensure we see debug messages
        System.out.println("\n[DEBUG] Completion called! Buffer: '" + buffer + "', cursor: " + cursor);
        logger.error("COMPLETION METHOD CALLED - buffer: '" + buffer + "', cursor: " + cursor);
        
        final SortedSet<String> commands = new TreeSet<>(commandMap.keySet());
        System.out.println("[DEBUG] Available commands: " + commands);
        
        // Handle empty buffer
        if (buffer == null || buffer.isEmpty()) {
            System.out.println("[DEBUG] Empty buffer, adding all commands");
            candidates.addAll(commands);
            System.out.println("[DEBUG] Added " + candidates.size() + " candidates");
            return 0;
        }
        
        // Get the text up to the cursor position
        String textToCursor = buffer.length() > cursor ? buffer.substring(0, cursor) : buffer;
        System.out.println("[DEBUG] Text to cursor: '" + textToCursor + "'");
        
        // Since commandChar is ' ' (space), we don't need to check for command prefix
        // Just work with the buffer directly
        String[] parts = textToCursor.split(" ");
        System.out.println("[DEBUG] Split parts: " + Arrays.toString(parts));
        
        if (parts.length == 0) {
            // Empty input, show all commands
            candidates.addAll(commands);
            System.out.println("[DEBUG] Empty parts, added " + candidates.size() + " candidates");
            return 0;
        } else if (parts.length == 1) {
            // Completing command name
            String commandPrefix = parts[0];
            System.out.println("[DEBUG] Completing command with prefix: '" + commandPrefix + "'");
            
            for (String command : commands) {
                if (command.startsWith(commandPrefix)) {
                    candidates.add(command);
                    System.out.println("[DEBUG] Added command candidate: " + command);
                }
            }
            
            System.out.println("[DEBUG] Final candidates: " + candidates);
            // Return the start position for replacement
            return textToCursor.lastIndexOf(commandPrefix);
        } else {
            // Completing command arguments
            String commandName = parts[0];
            System.out.println("[DEBUG] Completing arguments for command: " + commandName);
            
            Command command = commandMap.get(commandName);
            if (command == null) {
                System.out.println("[DEBUG] Command not found: " + commandName);
                return -1;
            }
            
            // Get the argument parts (excluding the command name)
            String[] argParts = Arrays.copyOfRange(parts, 1, parts.length);
            String currentArg = argParts.length > 0 ? argParts[argParts.length - 1] : "";
            String[] prefixArgs = argParts.length > 1 ? Arrays.copyOfRange(argParts, 0, argParts.length - 1) : new String[0];
            
            System.out.println("[DEBUG] Prefix args: " + Arrays.toString(prefixArgs) + ", current arg: '" + currentArg + "'");
            
            List<CharSequence> argCandidates = command.completeArgument(prefixArgs, currentArg);
            System.out.println("[DEBUG] Got " + argCandidates.size() + " argument candidates: " + argCandidates);
            
            candidates.addAll(argCandidates);
            
            // Return the start position of the current argument
            if (!currentArg.isEmpty()) {
                return textToCursor.lastIndexOf(currentArg);
            } else {
                return textToCursor.length();
            }
        }
    }
}
