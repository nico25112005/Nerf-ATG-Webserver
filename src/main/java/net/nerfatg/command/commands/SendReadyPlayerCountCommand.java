package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.ReadyPlayerCount;

public class SendReadyPlayerCountCommand extends Command {
    public SendReadyPlayerCountCommand(String label) {
        super(label);
        setHelpText("Usage: sendreadyplayercount (-broadcast | -playerid <id>) (-random | -readyPlayers <count>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -readyPlayers <count>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-readyPlayers", 1, new CommandArgumentValue(2, this::sendManualBroadcast))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-readyPlayers", 2, new CommandArgumentValue(3, this::sendManualTarget))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        ReadyPlayerCount packet = new ReadyPlayerCount(CommandUtil.randomByte(0, 10), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted ReadyPlayerCount packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 3) {
            System.out.println(helpText);
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(CommandUtil.parseByte(args[2], (byte)0), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted ReadyPlayerCount packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(CommandUtil.randomByte(0, 10), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent ReadyPlayerCount packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 4) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(CommandUtil.parseByte(args[3], (byte)0), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent ReadyPlayerCount packet to " + playerId + ": " + packet);
    }
} 