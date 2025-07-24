package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerInfo;

public class SendPlayerInfoCommand extends Command {
    public SendPlayerInfoCommand(String label) {
        super(label);
        setHelpText("Usage: sendplayerinfo (-broadcast | -playerid <id>) (-random | -playerId <id> -name <name> -index <idx>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -playerId <id>: string (8 chars)\n" +
                "  -name <name>: string (12 chars)\n" +
                "  -index <idx>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-playerId", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-name", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-index", 5, new CommandArgumentValue(6, this::sendManualBroadcast))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-playerId", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-name", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-index", 6, new CommandArgumentValue(7, this::sendManualTarget))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        PlayerInfo packet = new PlayerInfo(CommandUtil.randomString(8), CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerInfo packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 6) {
            System.out.println(helpText);
            return;
        }
        PlayerInfo packet = new PlayerInfo(args[2], args[4], CommandUtil.parseByte(args[6], (byte)0), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerInfo packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        PlayerInfo packet = new PlayerInfo(playerId, CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerInfo packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 7) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        PlayerInfo packet = new PlayerInfo(args[3], args[5], CommandUtil.parseByte(args[7], (byte)0), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerInfo packet to " + playerId + ": " + packet);
    }
} 