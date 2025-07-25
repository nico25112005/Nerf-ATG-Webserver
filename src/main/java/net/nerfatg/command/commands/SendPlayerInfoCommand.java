package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerInfo;

import java.util.Random;

public class SendPlayerInfoCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendPlayerInfoCommand(String label, Proxy proxy) {
        super(label);
        this.proxy = proxy;
        init();
    }

    private void init() {
        // -help argument
        CommandArgument help = new CommandArgument("-help", 0, ctx -> printHelp());
        addArgument(help);

        // -broadcast branch
        CommandArgument randomBroadcast = new CommandArgument("-random", 1, ctx -> broadcastRandom(ctx));

        CommandArgument manualBroadcast = new CommandArgument("-manual", 1, new CommandArgument[]{
                new CommandArgument("-playerid", 2, new CommandArgument[]{
                        new CommandArgumentValue(3, new CommandArgument[]{
                                new CommandArgument("-playername", 4, new CommandArgument[]{
                                        new CommandArgumentValue(5, new CommandArgument[]{
                                                new CommandArgument("-index", 6, new CommandArgument[]{
                                                        new CommandArgumentValue(7, ctx -> broadcastManual(ctx))
                                                }, null)
                                        }, null)
                                }, null)
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
                new CommandArgument("-playerid", 3, new CommandArgument[]{
                        new CommandArgumentValue(4, new CommandArgument[]{
                                new CommandArgument("-playername", 5, new CommandArgument[]{
                                        new CommandArgumentValue(6, new CommandArgument[]{
                                                new CommandArgument("-index", 7, new CommandArgument[]{
                                                        new CommandArgumentValue(8, ctx -> singleManual(ctx))
                                                }, null)
                                        }, null)
                                }, null)
                        }, null)
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
        System.out.println("Usage for sendplayerinfo:");
        System.out.println("  sendplayerinfo -help");
        System.out.println("  sendplayerinfo -broadcast -random");
        System.out.println("  sendplayerinfo -broadcast -manual -playerid <id> -playername <name> -index <index>");
        System.out.println("  sendplayerinfo -singleconnection <targetPlayerId> -random");
        System.out.println("  sendplayerinfo -singleconnection <targetPlayerId> -manual -playerid <id> -playername <name> -index <index>");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-playerid <id>: Player ID for the packet");
        System.out.println("-playername <name>: Player name for the packet");
        System.out.println("-index <index>: Index (team or other) for the packet");
    }

    private void broadcastRandom(CommandContext ctx) {
        PlayerInfo packet = new PlayerInfo(
                "player" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                PacketAction.Generic
        );
        proxy.broadcast(packet);
        System.out.println("Sent PlayerInfo packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = null, playerName = null;
        Byte index = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-playername".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerName = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Index must be a number");
                    return;
                }
            }
        }
        if (playerId == null || playerName == null || index == null) {
            System.out.println("Usage: sendplayerinfo -broadcast -manual -playerid <id> -playername <name> -index <index>");
            return;
        }
        PlayerInfo packet = new PlayerInfo(playerId, playerName, index, PacketAction.Generic);
        proxy.broadcast(packet);
        System.out.println("Sent PlayerInfo packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendplayerinfo -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        PlayerInfo packet = new PlayerInfo(
                "player" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                PacketAction.Generic
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerInfo packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String playerId = null, playerName = null;
        Byte index = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-playername".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerName = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Index must be a number");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (playerId == null || playerName == null || index == null) {
            System.out.println("Usage: sendplayerinfo -singleconnection <targetPlayerId> -manual -playerid <id> -playername <name> -index <index>");
            return;
        }
        PlayerInfo packet = new PlayerInfo(playerId, playerName, index, PacketAction.Generic);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerInfo packet to " + targetPlayer + ": " + packet);
    }
} 