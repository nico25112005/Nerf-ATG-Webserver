/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;

import java.util.*;

public class Command {

    private final SortedSet<CommandArgument> arguments = new TreeSet<>();
    private final String label;
    private final String description;
    private CommandAction nativeAction;
    private NerfLogger logger;
    private String helpText;

    public Command(String label) {
        this.label = label;
        this.description = null;
    }

    public Command(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public Command(String label, CommandAction nativeAction) {
        this.label = label;
        this.description = null;
        this.nativeAction = nativeAction;
    }

    public Command(String label, String description, CommandAction nativeAction) {
        this.label = label;
        this.description = description;
        this.nativeAction = nativeAction;
    }

    /**
     * If the command has no arguments, run the nullAction. If the command has arguments, check if the first argument is
     * valid. If it is, check if the next argument is valid. If it is, check if the next argument is valid. If it is, run
     * the command action
     *
     * @param commandContext The context of the command.
     */
    public void execute(CommandContext commandContext) {
        ensureLogger(); // Ensure logger is available
        
        if (commandContext.args().length > 0 && "-help".equalsIgnoreCase(commandContext.args()[0])) {
            if (helpText != null) {
                System.out.println(helpText);
            } else {
                System.out.println(generateHelpText());
            }
            return;
        }
        
        logger.fine("Executing command: " + label + " with args: " + Arrays.toString(commandContext.args()));
        if (commandContext.args().length == 0 && nativeAction != null) {
            logger.fine("No arguments, running native action for command: " + label);
            nativeAction.run(commandContext);
            return;
        }

        CommandArgument argument =
                arguments.stream().filter(arg -> filterArguments(arg, commandContext))
                        .findFirst().orElse(null);

        if (argument == null) {
            if (logger != null) logger.warning("Command not found for input: " + Arrays.toString(commandContext.args()));
            printSyntaxError(commandContext, 0);
            return;
        } else if (commandContext.args().length == 1) {
            if (logger != null) logger.fine("Running command action for argument: " + argument.getLabel());
            argument.getCommandAction().run(commandContext);
        }

        for (int i = 0; i < commandContext.args().length-1; i++) {
            argument = getNextArgument(argument, commandContext);
            if (argument == null) {
                if (logger != null) logger.warning("Command not found in argument chain for input: " + Arrays.toString(commandContext.args()));
                printSyntaxError(commandContext, i + 1);
                return;
            }

            if (argument.getIndex() == (commandContext.args().length-1) && argument.getCommandAction() != null) {
                if (logger != null) logger.fine("Running command action for argument: " + argument.getLabel());
                argument.getCommandAction().run(commandContext);
            }
        }
    }

    public List<CharSequence> completeArgument(String[] prefixArgs, String prefix) {
        List<CharSequence> candidates = new LinkedList<>();
        SortedSet<CommandArgument> nextArguments = arguments;

        for (String argPrefix : prefixArgs) {
            CommandArgument commandArgument = nextArguments.stream()
                    .filter(arg ->
                            arg.getLabel().equals(argPrefix) || arg instanceof CommandArgumentValue
                    ).findFirst().orElse(null);
            if (commandArgument == null) return candidates;

            nextArguments = commandArgument.getSubCommands();
        }

        for (CommandArgument argument : nextArguments) {
            if (argument.getLabel().startsWith(prefix)) {
                StringBuilder stringBuilder = new StringBuilder();

                for (String prefixArg : prefixArgs)
                    stringBuilder.append(prefixArg).append(" ");

                stringBuilder.append(argument.getLabel());

                candidates.add(stringBuilder.toString().trim());
            }
        }

        return candidates;
    }

    /**
     * It returns the next argument in the command chain
     *
     * @param gArgument The current argument that is being processed.
     * @param gotziCommandContext The context of the command.
     * @return The next argument in the command.
     */
    private CommandArgument getNextArgument(CommandArgument gArgument, CommandContext gotziCommandContext) {
        if (gArgument.getSubCommands() == null) return null;
        return gArgument.getSubCommands().stream().filter(arg ->
                filterArguments(arg, gotziCommandContext)
        ).findFirst().orElse(null);
    }

    /**
     * "If the argument's index is less than the number of arguments in the command context, and the argument's label is
     * equal to the argument at the index in the command context, or the argument is a value argument, return true."
     *
     * The first part of the function checks if the argument's index is less than the number of arguments in the command
     * context. This is to make sure that the argument is not out of bounds
     *
     * @param gArgument The argument that is being checked.
     * @param gotziCommandContext The context of the command.
     * @return A boolean value.
     */
    private boolean filterArguments(CommandArgument gArgument, CommandContext gotziCommandContext) {
        return gArgument.getIndex() < gotziCommandContext.args().length && (gArgument.getLabel().equals(gotziCommandContext.args()[gArgument.getIndex()]) || gArgument instanceof CommandArgumentValue);
    }

    /**
     * If the argument doesn't already exist, add it to the list
     *
     * @param argument The argument to add to the list of arguments.
     */
    public void addArgument(CommandArgument argument) {
        if (arguments.stream().noneMatch(subArgument -> subArgument.getLabel().equals(subArgument.getLabel())))
            arguments.add(argument);
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public SortedSet<CommandArgument> getArguments() {
        return Collections.unmodifiableSortedSet(arguments);
    }

    public void setNativeAction(CommandAction nativeAction) {
        this.nativeAction = nativeAction;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public NerfLogger getCommandLogger() {
        return this.logger;
    }

    protected void setLogger(NerfLogger logger) {
        this.logger = logger;
    }

    /**
     * Automatically creates and sets a logger for this command if none exists
     */
    private void ensureLogger() {
        if (this.logger == null) {
            this.logger = LoggerFactory.getLogger("Command-" + label);
        }
    }

    /**
     * Generates automatic help text based on command description and arguments
     *
     * @return Generated help text string
     */
    private String generateHelpText() {
        StringBuilder helpBuilder = new StringBuilder();
        
        // Command header
        helpBuilder.append("Command: ").append(label).append("\n");
        
        // Command description
        if (description != null && !description.trim().isEmpty()) {
            helpBuilder.append("Description: ").append(description).append("\n");
        }
        
        // Usage section
        helpBuilder.append("\nUsage:\n");
        helpBuilder.append("  ").append(label);
        
        if (nativeAction != null) {
            helpBuilder.append(" (no arguments)\n");
        }
        
        if (!arguments.isEmpty()) {
            helpBuilder.append("\n\nArguments:\n");
            generateArgumentHelp(arguments, helpBuilder, "  ");
        }
        
        helpBuilder.append("\nUse '").append(label).append(" -help' to show this help message.");
        
        return helpBuilder.toString();
    }

    /**
     * Recursively generates help text for command arguments
     *
     * @param args The set of arguments to generate help for
     * @param helpBuilder The StringBuilder to append help text to
     * @param indent The current indentation level
     */
    private void generateArgumentHelp(SortedSet<CommandArgument> args, StringBuilder helpBuilder, String indent) {
        for (CommandArgument arg : args) {
            if (arg instanceof CommandArgumentValue) {
                helpBuilder.append(indent).append("<value>");
                if (arg.getDescription() != null && !arg.getDescription().trim().isEmpty()) {
                    helpBuilder.append(" - ").append(arg.getDescription());
                }
                helpBuilder.append("\n");
            } else {
                helpBuilder.append(indent).append(arg.getLabel());
                if (arg.getDescription() != null && !arg.getDescription().trim().isEmpty()) {
                    helpBuilder.append(" - ").append(arg.getDescription());
                }
                helpBuilder.append("\n");
            }
            
            // Recursively add sub-arguments
            if (arg.getSubCommands() != null && !arg.getSubCommands().isEmpty()) {
                generateArgumentHelp(arg.getSubCommands(), helpBuilder, indent + "  ");
            }
        }
    }

    /**
     * Prints detailed syntax error information with suggestions
     *
     * @param commandContext The command context that caused the error
     * @param errorIndex The index where the error occurred
     */
    private void printSyntaxError(CommandContext commandContext, int errorIndex) {
        System.out.println("❌ Syntax Error: Invalid command syntax");
        System.out.println();
        
        // Show what was entered
        System.out.print("You entered: " + label);
        for (int i = 0; i < commandContext.args().length; i++) {
            if (i == errorIndex) {
                System.out.print(" [❌" + commandContext.args()[i] + "]");
            } else {
                System.out.print(" " + commandContext.args()[i]);
            }
        }
        System.out.println();
        System.out.println();
        
        // Show available options at the error point
        SortedSet<CommandArgument> availableArgs = getAvailableArgumentsAtIndex(commandContext, errorIndex);
        
        if (!availableArgs.isEmpty()) {
            System.out.println("💡 Available options at this position:");
            for (CommandArgument arg : availableArgs) {
                if (arg instanceof CommandArgumentValue) {
                    System.out.print("   <value>");
                    if (arg.getDescription() != null && !arg.getDescription().trim().isEmpty()) {
                        System.out.print(" - " + arg.getDescription());
                    }
                } else {
                    System.out.print("   " + arg.getLabel());
                    if (arg.getDescription() != null && !arg.getDescription().trim().isEmpty()) {
                        System.out.print(" - " + arg.getDescription());
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
        
        // Show usage examples
        System.out.println("📖 Usage examples:");
        generateUsageExamples();
        
        System.out.println();
        System.out.println("💬 For detailed help, use: " + label + " -help");
    }

    /**
     * Gets available arguments at a specific error index
     *
     * @param commandContext The command context
     * @param errorIndex The index where the error occurred
     * @return Set of available arguments at that position
     */
    private SortedSet<CommandArgument> getAvailableArgumentsAtIndex(CommandContext commandContext, int errorIndex) {
        if (errorIndex == 0) {
            return arguments;
        }
        
        // Navigate to the correct argument level
        SortedSet<CommandArgument> currentArgs = arguments;
        CommandArgument currentArg = null;
        
        for (int i = 0; i < errorIndex && i < commandContext.args().length; i++) {
            int argIndex = i;
            currentArg = currentArgs.stream()
                    .filter(arg -> arg.getLabel().equals(commandContext.args()[argIndex]) || arg instanceof CommandArgumentValue)
                    .findFirst()
                    .orElse(null);
            
            if (currentArg == null) break;
            
            if (currentArg.getSubCommands() != null) {
                currentArgs = currentArg.getSubCommands();
            } else {
                currentArgs = new TreeSet<>();
                break;
            }
        }
        
        return currentArgs;
    }

    /**
     * Generates and prints usage examples for the command
     */
    private void generateUsageExamples() {
        // Show basic usage
        if (nativeAction != null) {
            System.out.println("   " + label + " (no arguments)");
        }
        
        // Show argument-based examples
        if (!arguments.isEmpty()) {
            generateExampleUsage(arguments, label, 0, 3); // Limit to 3 examples
        }
    }

    /**
     * Recursively generates example usage patterns
     *
     * @param args Current argument set
     * @param currentCommand Current command string being built
     * @param depth Current recursion depth
     * @param maxExamples Maximum number of examples to show
     */
    private void generateExampleUsage(SortedSet<CommandArgument> args, String currentCommand, int depth, int maxExamples) {
        if (depth >= maxExamples) return;
        
        int exampleCount = 0;
        for (CommandArgument arg : args) {
            if (exampleCount >= maxExamples) break;
            
            String exampleCommand;
            if (arg instanceof CommandArgumentValue) {
                exampleCommand = currentCommand + " <value>";
            } else {
                exampleCommand = currentCommand + " " + arg.getLabel();
            }
            
            if (arg.getCommandAction() != null) {
                System.out.println("   " + exampleCommand);
                exampleCount++;
            }
            
            // Show one level of sub-commands for the first few arguments
            if (arg.getSubCommands() != null && !arg.getSubCommands().isEmpty() && depth < 2) {
                generateExampleUsage(arg.getSubCommands(), exampleCommand, depth + 1, maxExamples - exampleCount);
            }
        }
    }
}