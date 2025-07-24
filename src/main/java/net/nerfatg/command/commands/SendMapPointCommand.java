package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.MapPoint;

public class SendMapPointCommand extends Command {
    public SendMapPointCommand(String label) {
        super(label);
        setHelpText("Usage: sendmappoint (-broadcast | -playerid <id>) (-random | -name <name> -index <idx> -longitude <lon> -latitude <lat>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -name <name>: string (12 chars)\n" +
                "  -index <idx>: byte\n" +
                "  -longitude <lon>: double\n" +
                "  -latitude <lat>: double");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-name", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-index", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-longitude", 5, new CommandArgumentValue(6, new CommandArgument[]{
                        new CommandArgument("-latitude", 7, new CommandArgumentValue(8, this::sendManualBroadcast))
                    }, null))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-name", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-index", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-longitude", 6, new CommandArgumentValue(7, new CommandArgument[]{
                        new CommandArgument("-latitude", 8, new CommandArgumentValue(9, this::sendManualTarget))
                    }, null))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        MapPoint packet = new MapPoint(CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted MapPoint packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 8) {
            System.out.println(helpText);
            return;
        }
        MapPoint packet = new MapPoint(args[2], CommandUtil.parseByte(args[4], (byte)0), CommandUtil.parseDouble(args[6], 0.0), CommandUtil.parseDouble(args[8], 0.0), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted MapPoint packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        MapPoint packet = new MapPoint(CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent MapPoint packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 9) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        MapPoint packet = new MapPoint(args[3], CommandUtil.parseByte(args[5], (byte)0), CommandUtil.parseDouble(args[7], 0.0), CommandUtil.parseDouble(args[9], 0.0), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent MapPoint packet to " + playerId + ": " + packet);
    }
} 