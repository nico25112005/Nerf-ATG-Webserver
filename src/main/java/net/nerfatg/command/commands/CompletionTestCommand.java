/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;

/**
 * Test command to verify tab completion is working properly
 */
public class CompletionTestCommand extends Command {

    private static final NerfLogger logger = LoggerFactory.getLogger("CompletionTestCommand");

    public CompletionTestCommand(String label) {
        super(label, "Test command for verifying tab completion functionality");
        setLogger(logger);
        
        setNativeAction(this::showCompletionInfo);
        
        // Add various arguments to test completion
        addArgument(new CommandArgument("start", "Start something", 0, this::startAction));
        addArgument(new CommandArgument("stop", "Stop something", 0, this::stopAction));
        addArgument(new CommandArgument("status", "Show status", 0, this::statusAction));
        addArgument(new CommandArgument("config", "Configuration options", 0, null,
                new CommandArgument("set", "Set a configuration value", 1, this::configSetAction),
                new CommandArgument("get", "Get a configuration value", 1, this::configGetAction),
                new CommandArgument("list", "List all configuration values", 1, this::configListAction)));
    }

    private void showCompletionInfo(CommandContext ctx) {
        logger.info("=== Tab Completion Test Command ===");
        System.out.println("This command tests tab completion functionality.");
        System.out.println();
        System.out.println("Try these completion tests:");
        System.out.println("1. Type 'comp' and press TAB - should complete to 'completion'");
        System.out.println("2. Type 'completion ' and press TAB - should show: start, stop, status, config");
        System.out.println("3. Type 'completion st' and press TAB - should show: start, stop, status");
        System.out.println("4. Type 'completion config ' and press TAB - should show: set, get, list");
        System.out.println("5. Type any custom value and press TAB");
        System.out.println();
        System.out.println("Available sub-commands:");
        System.out.println("  start   - Start something");
        System.out.println("  stop    - Stop something");
        System.out.println("  status  - Show status");
        System.out.println("  config  - Configuration options");
        System.out.println("    set   - Set a configuration value");
        System.out.println("    get   - Get a configuration value");
        System.out.println("    list  - List all configuration values");
        System.out.println();
        logger.debug("Completion test info displayed");
    }

    private void startAction(CommandContext ctx) {
        logger.info("Start action executed");
        System.out.println("✅ Start action completed!");
    }

    private void stopAction(CommandContext ctx) {
        logger.info("Stop action executed");
        System.out.println("⏹️ Stop action completed!");
    }

    private void statusAction(CommandContext ctx) {
        logger.info("Status action executed");
        System.out.println("📊 Status: All systems operational");
    }

    private void configSetAction(CommandContext ctx) {
        logger.info("Config set action executed");
        System.out.println("⚙️ Configuration set action completed!");
    }

    private void configGetAction(CommandContext ctx) {
        logger.info("Config get action executed");
        System.out.println("📖 Configuration get action completed!");
    }

    private void configListAction(CommandContext ctx) {
        logger.info("Config list action executed");
        System.out.println("📋 Configuration list action completed!");
    }

    private void valueAction(CommandContext ctx) {
        String value = ctx.args()[0];
        logger.info("Value action executed with: " + value);
        System.out.println("💡 Custom value received: " + value);
    }
}