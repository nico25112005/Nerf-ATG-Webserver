package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerStatus;

public class SendPlayerStatusCommand extends Command {
    public SendPlayerStatusCommand(String label) {
        super(label);
        setHelpText("Usage: sendplayerstatus (-broadcast | -playerid <id>) (-random | -playerId <id> -name <name> -index <idx> -longitude <lon> -latitude <lat> -health <h>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -playerId <id>: string (8 chars)\n" +
                "  -name <name>: string (12 chars)\n" +
                "  -index <idx>: byte\n" +
                "  -longitude <lon>: double\n" +
                "  -latitude <lat>: double\n" +
                "  -health <h>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-playerId", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-name", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-index", 5, new CommandArgumentValue(6, new CommandArgument[]{
                        new CommandArgument("-longitude", 7, new CommandArgumentValue(8, new CommandArgument[]{
                            new CommandArgument("-latitude", 9, new CommandArgumentValue(10, new CommandArgument[]{
                                new CommandArgument("-health", 11, new CommandArgumentValue(12, this::sendManualBroadcast))
                            }, null))
                        }, null))
                    }, null))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-playerId", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-name", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-index", 6, new CommandArgumentValue(7, new CommandArgument[]{
                        new CommandArgument("-longitude", 8, new CommandArgumentValue(9, new CommandArgument[]{
                            new CommandArgument("-latitude", 10, new CommandArgumentValue(11, new CommandArgument[]{
                                new CommandArgument("-health", 12, new CommandArgumentValue(13, this::sendManualTarget))
                            }, null))
                        }, null))
                    }, null))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        PlayerStatus packet = new PlayerStatus(CommandUtil.randomString(8), CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), CommandUtil.randomByte(0, 100), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerStatus packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 12) {
            System.out.println(helpText);
            return;
        }
        PlayerStatus packet = new PlayerStatus(args[2], args[4], CommandUtil.parseByte(args[6], (byte)0), CommandUtil.parseDouble(args[8], 0.0), CommandUtil.parseDouble(args[10], 0.0), CommandUtil.parseByte(args[12], (byte)100), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerStatus packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        PlayerStatus packet = new PlayerStatus(playerId, CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), CommandUtil.randomDouble(-180, 180), CommandUtil.randomDouble(-90, 90), CommandUtil.randomByte(0, 100), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerStatus packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 13) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        PlayerStatus packet = new PlayerStatus(args[3], args[5], CommandUtil.parseByte(args[7], (byte)0), CommandUtil.parseDouble(args[9], 0.0), CommandUtil.parseDouble(args[11], 0.0), CommandUtil.parseByte(args[13], (byte)100), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerStatus packet to " + playerId + ": " + packet);
    }
} 