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
        CommandArgument help = new CommandArgument("-help", 0, ctx -> printHelp());
        addArgument(help);

        CommandArgument randomBroadcast = new CommandArgument("-random", 1, new CommandArgument[]{
            new CommandArgument("-action", 2, new CommandArgument[]{
                new CommandArgumentValue(3, (this::broadcastRandom))
            }, null)
        }, null);

        CommandArgument manualBroadcast = new CommandArgument("-manual", 1, new CommandArgument[]{
            new CommandArgument("-playerid", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-name", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-index", 6, new CommandArgument[]{
                                new CommandArgumentValue(7, new CommandArgument[]{
                                    new CommandArgument("-action", 8, new CommandArgument[]{
                                        new CommandArgumentValue(9, ctx -> broadcastManual(ctx))
                                    }, null)
                                }, null)
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

        CommandArgument randomSingle = new CommandArgument("-random", 2, ctx -> singleRandom(ctx));
        CommandArgument manualSingle = new CommandArgument("-manual", 2, new CommandArgument[]{
            new CommandArgument("-playerid", 3, new CommandArgument[]{
                new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-name", 5, new CommandArgument[]{
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
        System.out.println("  sendplayerinfo -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerinfo -broadcast -manual -playerid <id> -name <name> -index <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerinfo -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerinfo -singleconnection <targetPlayerId> -manual -playerid <id> -name <name> -index <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-playerid <id>: Player ID");
        System.out.println("-name <name>: Player name");
        System.out.println("-index <byte>: Team index");
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
        PlayerInfo packet = new PlayerInfo(
            "player" + random.nextInt(1000),
            "name" + random.nextInt(1000),
            (byte) random.nextInt(2),
            action
        );
        proxy.broadcast(packet);
        System.out.println("Sent PlayerInfo packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        String playerId = null, name = null;
        Byte index = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a byte");
                    return;
                }
            }
        }
        if (playerId == null || name == null || index == null) {
            System.out.println("Usage: sendplayerinfo -broadcast -manual -playerid <id> -name <name> -index <byte> [-action <action>]");
            return;
        }
        PlayerInfo packet = new PlayerInfo(playerId, name, index, action);
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
            (byte) random.nextInt(2),
            PacketAction.Add
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerInfo packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String playerId = null, name = null;
        Byte index = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a byte");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (playerId == null || name == null || index == null) {
            System.out.println("Usage: sendplayerinfo -singleconnection <targetPlayerId> -manual -playerid <id> -name <name> -index <byte>");
            return;
        }
        PlayerInfo packet = new PlayerInfo(playerId, name, index, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerInfo packet to " + targetPlayer + ": " + packet);
    }
} 