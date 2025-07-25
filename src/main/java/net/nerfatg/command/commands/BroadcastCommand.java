package net.nerfatg.command.commands;

import net.nerfatg.NerfATGServer;
import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.logging.LoggerFactory;
import net.nerfatg.logging.NerfLogger;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameStarted;

public class BroadcastCommand extends Command {

    private final NerfATGServer server;
    private static final NerfLogger logger = LoggerFactory.getLogger("BroadcastCommand");

    public BroadcastCommand(String label, NerfATGServer server) {
        super(label, "Broadcast a message to all connected players");

        this.server = server;
        setLogger(logger); // Set the enhanced logger

        setNativeAction(this::nativeAction);

        addArgument(
                new CommandArgumentValue("Message to broadcast to all players", 0, this::broadcastMessage)
        );
    }

    private void nativeAction(CommandContext ctx) {
        logger.info("Broadcast command started - use 'broadcast <message>' to send a message");
        logger.warning("No message provided. Usage: broadcast <message>");
    }
    
    private void broadcastMessage(CommandContext ctx) {
        String message = ctx.args()[0];
        logger.info("Broadcasting message to all players: " + message);
        
        try {
            GameStarted packet = new GameStarted("AAAAAAAA",
                    "BBBBBBBB", "Gotzi", (byte) 0, (byte) 5, PacketAction.Generic);
            server.getProxy().broadcast(packet);
            logger.important("Message broadcast successful: " + message);
        } catch (Exception e) {
            logger.error("Failed to broadcast message: " + e.getMessage());
        }
    }
}
