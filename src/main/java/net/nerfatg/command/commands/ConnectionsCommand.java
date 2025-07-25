package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandContext;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.util.Map;

public class ConnectionsCommand extends Command {
    private final Map<String, SocketChannel> playerClients;

    public ConnectionsCommand(String label, Map<String, SocketChannel> playerClients) {
        super(label);
        this.playerClients = playerClients;
        setNativeAction(this::nativeAction);
    }

    private void nativeAction(CommandContext ctx) {
        if (playerClients.isEmpty()) {
            System.out.println("No active player connections.");
            return;
        }
        System.out.println("Active player connections:");
        for (Map.Entry<String, SocketChannel> entry : playerClients.entrySet()) {
            String playerId = entry.getKey();
            SocketChannel channel = entry.getValue();
            try {
                System.out.println("Player: " + playerId + " | Remote: " + channel.getRemoteAddress() + " -> Local: " + channel.getLocalAddress());
            } catch (IOException e) {
                System.out.println("Player: " + playerId + " | Error reading address for channel: " + e.getMessage());
            }
        }
    }
} 