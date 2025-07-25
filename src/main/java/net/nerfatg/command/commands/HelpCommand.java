/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandContext;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;

/**
 * Help command that displays available commands and usage information
 */
public class HelpCommand extends Command {

    private static final NerfLogger logger = LoggerFactory.getLogger("HelpCommand");

    public HelpCommand(String label) {
        super(label, "Display help information and available commands");
        setLogger(logger);
        
        setNativeAction(this::showHelp);
    }

    private void showHelp(CommandContext ctx) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 NerfATG Server - Available Commands");
        System.out.println("=".repeat(60));
        System.out.println();
        
        System.out.println("📋 GENERAL COMMANDS:");
        System.out.println("  help                    - Show this help message");
        System.out.println("  version                 - Display server version");
        System.out.println();
        
        System.out.println("🌐 SERVER COMMANDS:");
        System.out.println("  broadcast <message>     - Broadcast message to all players");
        System.out.println();
        
        System.out.println("🔧 TESTING & DEBUG:");
        System.out.println("  logtest                 - Show logging system demo");
        System.out.println("  completion              - Test tab completion functionality");
        System.out.println("  consoletest             - Test mixed console output");
        System.out.println();
        
        System.out.println("💡 TIPS:");
        System.out.println("  • Use TAB for command completion");
        System.out.println("  • Use '<command> -help' for detailed command help");
        System.out.println("  • Try 'completion' command to test tab completion");
        System.out.println();

        System.out.println("=".repeat(60));
    }
}