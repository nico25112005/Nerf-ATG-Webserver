package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.JoinGame;

public class SendJoinGameCommand extends Command {
    public SendJoinGameCommand(String label) {
        super(label);
        setHelpText("Usage: sendjoingame (-broadcast | -playerid <id>) (-random | -playerId <id> -gameName <name>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -playerId <id>: string (8 chars)\n" +
                "  -gameName <name>: string (12 chars)");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-playerId", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-gameName", 3, new CommandArgumentValue(4, this::sendManualBroadcast))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-playerId", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-gameName", 4, new CommandArgumentValue(5, this::sendManualTarget))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        JoinGame packet = new JoinGame(CommandUtil.randomString(8), CommandUtil.randomString(12), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted JoinGame packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 4) {
            System.out.println(helpText);
            return;
        }
        JoinGame packet = new JoinGame(args[2], args[4], PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted JoinGame packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        JoinGame packet = new JoinGame(playerId, CommandUtil.randomString(12), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent JoinGame packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 5) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        JoinGame packet = new JoinGame(args[3], args[5], PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent JoinGame packet to " + playerId + ": " + packet);
    }
} 