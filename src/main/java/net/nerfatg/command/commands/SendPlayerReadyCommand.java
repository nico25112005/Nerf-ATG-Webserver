package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerReady;

public class SendPlayerReadyCommand extends Command {
    public SendPlayerReadyCommand(String label) {
        super(label);
        setHelpText("Usage: sendplayerready (-broadcast | -playerid <id>) (-random | -playerId <id> -health <h> -weapon <w> -damping <d>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -playerId <id>: string (8 chars)\n" +
                "  -health <h>: byte\n" +
                "  -weapon <w>: byte\n" +
                "  -damping <d>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-playerId", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-health", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-weapon", 5, new CommandArgumentValue(6, new CommandArgument[]{
                        new CommandArgument("-damping", 7, new CommandArgumentValue(8, this::sendManualBroadcast))
                    }, null))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-playerId", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-health", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-weapon", 6, new CommandArgumentValue(7, new CommandArgument[]{
                        new CommandArgument("-damping", 8, new CommandArgumentValue(9, this::sendManualTarget))
                    }, null))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        PlayerReady packet = new PlayerReady(CommandUtil.randomString(8), CommandUtil.randomByte(0, 100), CommandUtil.randomByte(0, 10), CommandUtil.randomByte(0, 100), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerReady packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 8) {
            System.out.println(helpText);
            return;
        }
        PlayerReady packet = new PlayerReady(args[2], CommandUtil.parseByte(args[4], (byte)100), CommandUtil.parseByte(args[6], (byte)0), CommandUtil.parseByte(args[8], (byte)0), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted PlayerReady packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        PlayerReady packet = new PlayerReady(playerId, CommandUtil.randomByte(0, 100), CommandUtil.randomByte(0, 10), CommandUtil.randomByte(0, 100), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerReady packet to " + playerId + ": " + packet);
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
        PlayerReady packet = new PlayerReady(args[3], CommandUtil.parseByte(args[5], (byte)100), CommandUtil.parseByte(args[7], (byte)0), CommandUtil.parseByte(args[9], (byte)0), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent PlayerReady packet to " + playerId + ": " + packet);
    }
} 