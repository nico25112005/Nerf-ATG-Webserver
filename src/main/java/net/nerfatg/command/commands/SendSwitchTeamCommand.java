package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.SwitchTeam;

public class SendSwitchTeamCommand extends Command {
    public SendSwitchTeamCommand(String label) {
        super(label);
        setHelpText("Usage: sendswitchteam (-broadcast | -playerid <id>) (-random | -playerId <id>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -playerId <id>: string (8 chars)");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-playerId", 1, new CommandArgumentValue(2, this::sendManualBroadcast))
        }, null));
        // -playerid <id> -random or manual
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-playerId", 2, new CommandArgumentValue(3, this::sendManualTarget))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        SwitchTeam packet = new SwitchTeam(CommandUtil.randomString(8), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted SwitchTeam packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 3) {
            System.out.println(helpText);
            return;
        }
        SwitchTeam packet = new SwitchTeam(args[2], PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted SwitchTeam packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        SwitchTeam packet = new SwitchTeam(playerId, PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent SwitchTeam packet to " + playerId + ": " + packet);
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
        SwitchTeam packet = new SwitchTeam(args[3], PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent SwitchTeam packet to " + playerId + ": " + packet);
    }
} 