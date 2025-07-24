package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.BaseLocation;

public class SendBaseLocationCommand extends Command {
    public SendBaseLocationCommand(String label) {
        super(label);
        setHelpText("Usage: sendbaselocation (-broadcast | -playerid <id>) (-random | -teamIndex <idx> -longitude <lon> -latitude <lat>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -teamIndex <idx>: byte\n" +
                "  -longitude <lon>: double\n" +
                "  -latitude <lat>: double");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-teamIndex", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-longitude", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-latitude", 5, new CommandArgumentValue(6, this::sendManualBroadcast))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-teamIndex", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-longitude", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-latitude", 6, new CommandArgumentValue(7, this::sendManualTarget))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        BaseLocation packet = new BaseLocation(CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted BaseLocation packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 6) {
            System.out.println(helpText);
            return;
        }
        byte teamIndex = CommandUtil.parseByte(args[2], (byte)0);
        double longitude = CommandUtil.parseDouble(args[4], 0.0);
        double latitude = CommandUtil.parseDouble(args[6], 0.0);
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted BaseLocation packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        BaseLocation packet = new BaseLocation(CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent BaseLocation packet to " + playerId + ": " + packet);
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
        byte teamIndex = CommandUtil.parseByte(args[3], (byte)0);
        double longitude = CommandUtil.parseDouble(args[5], 0.0);
        double latitude = CommandUtil.parseDouble(args[7], 0.0);
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent BaseLocation packet to " + playerId + ": " + packet);
    }
} 