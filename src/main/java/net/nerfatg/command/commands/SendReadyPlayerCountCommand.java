package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.ReadyPlayerCount;

import java.util.Random;

public class SendReadyPlayerCountCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendReadyPlayerCountCommand(String label, Proxy proxy) {
        super(label);
        this.proxy = proxy;
        init();
    }

    private void init() {
        // -help argument
        CommandArgument help = new CommandArgument("-help", 0, ctx -> printHelp());
        addArgument(help);

        // -broadcast branch
        CommandArgument randomBroadcast = new CommandArgument("-random", 1, new CommandArgument[]{
            new CommandArgument("-action", 2, new CommandArgumentValue(3, ctx -> broadcastRandom(ctx))),
            new CommandArgumentValue(2, ctx -> broadcastRandom(ctx))
        }, null);

        CommandArgument manualBroadcast = new CommandArgument("-manual", 1, new CommandArgument[]{
            new CommandArgument("-readyplayers", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-action", 4, new CommandArgumentValue(5, ctx -> broadcastManual(ctx))),
                    new CommandArgumentValue(4, ctx -> broadcastManual(ctx))
                }, null)
            }, null)
        }, null);

        CommandArgument broadcast = new CommandArgument("-broadcast", 0, new CommandArgument[]{
                randomBroadcast,
                manualBroadcast
        }, null);

        // -singleconnection branch
        CommandArgument randomSingle = new CommandArgument("-random", 2, ctx -> singleRandom(ctx));

        CommandArgument manualSingle = new CommandArgument("-manual", 2, new CommandArgument[]{
                new CommandArgument("-readyplayers", 3, new CommandArgument[]{
                        new CommandArgumentValue(4, ctx -> singleManual(ctx))
                }, null)
        }, null);

        CommandArgument singleConnection = new CommandArgument("-singleconnection", 0, new CommandArgument[]{
                new CommandArgumentValue(1, new CommandArgument[]{
                        randomSingle,
                        manualSingle
                }, null)
        }, null);

        addArgument(broadcast);
        addArgument(singleConnection);
    }

    private void printHelp() {
        System.out.println("Usage for sendreadyplayercount:");
        System.out.println("  sendreadyplayercount -help");
        System.out.println("  sendreadyplayercount -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendreadyplayercount -broadcast -manual -readyplayers <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendreadyplayercount -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendreadyplayercount -singleconnection <targetPlayerId> -manual -readyplayers <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-readyplayers <byte>: Number of ready players");
        System.out.println("-action <Generic|Add|Update|Remove|Replace>: Packet action (optional, default: Add)");
    }

    private PacketAction parseAction(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-action".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    return PacketAction.valueOf(args[i + 1]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid action. Use one of: Generic, Add, Update, Remove, Replace");
                    return null;
                }
            }
        }
        return PacketAction.Add;
    }
    private void broadcastRandom(CommandContext ctx) {
        PacketAction action = parseAction(ctx.args());
        if (action == null) return;
        ReadyPlayerCount packet = new ReadyPlayerCount(
                (byte) random.nextInt(10),
                action
        );
        proxy.broadcast(packet);
        System.out.println("Sent ReadyPlayerCount packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        Byte readyPlayers = null;
        for (int i = 0; i < args.length; i++) {
            if ("-readyplayers".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    readyPlayers = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("readyplayers must be a number");
                    return;
                }
            }
        }
        if (readyPlayers == null) {
            System.out.println("Usage: sendreadyplayercount -broadcast -manual -readyplayers <byte> [-action <action>]");
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(readyPlayers, action);
        proxy.broadcast(packet);
        System.out.println("Sent ReadyPlayerCount packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendreadyplayercount -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(
                (byte) random.nextInt(10),
                PacketAction.Add
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent ReadyPlayerCount packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        Byte readyPlayers = null;
        for (int i = 0; i < args.length; i++) {
            if ("-readyplayers".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    readyPlayers = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("readyplayers must be a number");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (readyPlayers == null) {
            System.out.println("Usage: sendreadyplayercount -singleconnection <targetPlayerId> -manual -readyplayers <byte>");
            return;
        }
        ReadyPlayerCount packet = new ReadyPlayerCount(readyPlayers, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent ReadyPlayerCount packet to " + targetPlayer + ": " + packet);
    }
} 