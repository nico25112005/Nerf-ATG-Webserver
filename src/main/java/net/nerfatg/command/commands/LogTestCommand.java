/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.logging.LogLevel;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;

/**
 * Demonstration command showing the enhanced logging system capabilities
 */
public class LogTestCommand extends Command {

    private static final NerfLogger logger = LoggerFactory.getLogger("LogTestCommand");

    public LogTestCommand(String label) {
        super(label, "Test command to demonstrate the enhanced logging system");
        setLogger(logger);
        
        setNativeAction(this::showLogLevels);
        
        // Add sub-commands for different log levels
        addArgument(new CommandArgument("error", "Test error level logging", 0, this::testError));
        addArgument(new CommandArgument("warning", "Test warning level logging", 0, this::testWarning));
        addArgument(new CommandArgument("info", "Test info level logging", 0, this::testInfo));
        addArgument(new CommandArgument("debug", "Test debug level logging", 0, this::testDebug));
        addArgument(new CommandArgument("important", "Test important level logging", 0, this::testImportant));
        addArgument(new CommandArgument("fine", "Test fine level logging", 0, this::testFine));
    }

    private void showLogLevels(CommandContext ctx) {
        logger.info("=== Enhanced Logging System Demo ===");
        logger.important("✨ JLine integration active - console output with ANSI erase line");
        logger.info("Available log levels:");
        logger.error("ERROR - Critical errors (Red)");
        logger.warning("WARNING - Warning messages (Yellow)");
        logger.info("INFO - General information (Green)");
        logger.debug("DEBUG - Debug information (Cyan)");
        logger.important("IMPORTANT - Important status (Green)");
        logger.fine("FINE - Fine-grained debug (Green)");
        logger.info("Usage: logtest <level> | logtest jline | logtest <custom_message>");
        logger.debug("Console output is managed by JLine with proper line clearing");
    }

    private void testError(CommandContext ctx) {
        logger.error("This is a test ERROR message - shown in red");
        logger.info("Error level test completed");
    }

    private void testWarning(CommandContext ctx) {
        logger.warning("This is a test WARNING message - shown in yellow");
        logger.info("Warning level test completed");
    }

    private void testInfo(CommandContext ctx) {
        logger.info("This is a test INFO message - shown in green");
        logger.info("Info level test completed");
    }

    private void testDebug(CommandContext ctx) {
        logger.debug("This is a test DEBUG message - shown in cyan");
        logger.info("Debug level test completed");
    }

    private void testImportant(CommandContext ctx) {
        logger.important("This is a test IMPORTANT message - shown in green");
        logger.info("Important level test completed");
    }

    private void testFine(CommandContext ctx) {
        logger.fine("This is a test FINE message - shown in green");
        logger.info("Fine level test completed");
    }

    private void logCustomMessage(CommandContext ctx) {
        String message = ctx.args()[0];
        logger.info("Custom message: " + message);
        logger.debug("Custom message logged with INFO level");
    }

    private void testJLineIntegration(CommandContext ctx) {
        logger.important("Testing JLine integration...");
        
        // Test rapid logging to show line clearing
        for (int i = 1; i <= 5; i++) {
            logger.info("JLine test message " + i + "/5 - notice how the prompt stays clean");
            try {
                Thread.sleep(500); // Small delay to see the effect
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        logger.important("JLine integration test completed!");
        logger.debug("Console prompt should remain clean and functional");
    }
}