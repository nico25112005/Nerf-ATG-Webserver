/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command.commands;

import net.nerfatg.NerfATGServer;
import net.nerfatg.command.Command;
import net.nerfatg.command.CommandContext;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;

public class VersionCommand extends Command {

    private static final NerfLogger logger = LoggerFactory.getLogger("VersionCommand");

    public VersionCommand(String label) {
        super(label, "Display the current server version");
        setLogger(logger); // Set the enhanced logger
        init();
    }

    public void init() {
        setNativeAction(this::nativeAction);
    }

    private void nativeAction(CommandContext commandContext) {
        logger.info("Version command executed");
        logger.important("NerfATG Server Version: " + NerfATGServer.VERSION);
    }
}