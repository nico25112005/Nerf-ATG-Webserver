package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameStarted;

public class SendGameStartedCommand extends Command {
    public SendGameStartedCommand(String label) {
        super(label);
        setNativeAction(this::printHelp);
        addArgument(new CommandArgumentValue(0, this::sendGameStarted));
    }

    private void sendGameStarted(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            printHelp(ctx);
            return;
        }
        if (args.length < 2) {
            System.out.println("Usage: sendgamestarted <target|broadcast> <random|playerId leaderId leaderName teamIndex playerCount>");
            return;
        }
        String target = args[0];
        GameStarted packet;
        if (args[1].equalsIgnoreCase("random")) {
            packet = new GameStarted(CommandUtil.randomString(8), CommandUtil.randomString(8), CommandUtil.randomString(12), CommandUtil.randomByte(0, 10), CommandUtil.randomByte(1, 10), PacketAction.Generic);
        } else if (args.length >= 6) {
            String playerId = args[1];
            String leaderId = args[2];
            String leaderName = args[3];
            byte teamIndex = CommandUtil.parseByte(args[4], (byte)0);
            byte playerCount = CommandUtil.parseByte(args[5], (byte)1);
            packet = new GameStarted(playerId, leaderId, leaderName, teamIndex, playerCount, PacketAction.Generic);
        } else {
            System.out.println("Not enough arguments. Type 'sendgamestarted help' for usage.");
            return;
        }
        if (target.equalsIgnoreCase("broadcast")) {
            Proxy.getInstance().broadcast(packet);
            System.out.println("Broadcasted GameStarted packet: " + packet);
        } else {
            if (Proxy.getPlayerClients().containsKey(target)) {
                Proxy.getInstance().send(target, packet);
                System.out.println("Sent GameStarted packet to " + target + ": " + packet);
            } else {
                System.out.println("No such target: " + target);
            }
        }
    }

    private void printHelp(CommandContext ctx) {
        System.out.println("Usage: sendgamestarted <target|broadcast> <random|playerId leaderId leaderName teamIndex playerCount>");
        System.out.println("  target: string identifier for a connection");
        System.out.println("  broadcast: send to all connections");
        System.out.println("  random: generate all parameters randomly");
        System.out.println("  playerId: string (8 chars)");
        System.out.println("  leaderId: string (8 chars)");
        System.out.println("  leaderName: string (12 chars)");
        System.out.println("  teamIndex: byte");
        System.out.println("  playerCount: byte");
    }
} 