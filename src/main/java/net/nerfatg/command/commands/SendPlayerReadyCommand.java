package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerReady;

import java.util.Random;

public class SendPlayerReadyCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendPlayerReadyCommand(String label, Proxy proxy) {
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
                    new CommandArgument("-health", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-weapon", 6, new CommandArgument[]{
                                new CommandArgumentValue(7, new CommandArgument[]{
                                    new CommandArgument("-damping", 8, new CommandArgument[]{
                                        new CommandArgumentValue(9, new CommandArgument[]{
                                            new CommandArgument("-action", 10, new CommandArgument[]{
                                                new CommandArgumentValue(11, ctx -> broadcastManual(ctx))
                                            }, null)
                                        }, null)
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
                    new CommandArgument("-health", 5, new CommandArgument[]{
                        new CommandArgumentValue(6, new CommandArgument[]{
                            new CommandArgument("-weapon", 7, new CommandArgument[]{
                                new CommandArgumentValue(8, new CommandArgument[]{
                                    new CommandArgument("-damping", 9, new CommandArgument[]{
                                        new CommandArgumentValue(10, ctx -> singleManual(ctx))
                                    }, null)
                                }, null)
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
        System.out.println("Usage for sendplayerready:");
        System.out.println("  sendplayerready -help");
        System.out.println("  sendplayerready -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerready -broadcast -manual -playerid <id> -health <byte> -weapon <byte> -damping <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerready -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerready -singleconnection <targetPlayerId> -manual -playerid <id> -health <byte> -weapon <byte> -damping <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-playerid <id>: Player ID");
        System.out.println("-health <byte>: Health");
        System.out.println("-weapon <byte>: Weapon");
        System.out.println("-damping <byte>: Damping");
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
        PlayerReady packet = new PlayerReady(
            "player" + random.nextInt(1000),
            (byte) random.nextInt(100),
            (byte) random.nextInt(10),
            (byte) random.nextInt(10),
            action
        );
        proxy.broadcast(packet);
        System.out.println("Sent PlayerReady packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        String playerId = null;
        Byte health = null, weapon = null, damping = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-health".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    health = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("health must be a byte");
                    return;
                }
            } else if ("-weapon".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    weapon = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("weapon must be a byte");
                    return;
                }
            } else if ("-damping".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    damping = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("damping must be a byte");
                    return;
                }
            }
        }
        if (playerId == null || health == null || weapon == null || damping == null) {
            System.out.println("Usage: sendplayerready -broadcast -manual -playerid <id> -health <byte> -weapon <byte> -damping <byte> [-action <action>]");
            return;
        }
        PlayerReady packet = new PlayerReady(playerId, health, weapon, damping, action);
        proxy.broadcast(packet);
        System.out.println("Sent PlayerReady packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendplayerready -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        PlayerReady packet = new PlayerReady(
            "player" + random.nextInt(1000),
            (byte) random.nextInt(100),
            (byte) random.nextInt(10),
            (byte) random.nextInt(10),
            PacketAction.Add
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerReady packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String playerId = null;
        Byte health = null, weapon = null, damping = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-health".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    health = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("health must be a byte");
                    return;
                }
            } else if ("-weapon".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    weapon = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("weapon must be a byte");
                    return;
                }
            } else if ("-damping".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    damping = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("damping must be a byte");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (playerId == null || health == null || weapon == null || damping == null) {
            System.out.println("Usage: sendplayerready -singleconnection <targetPlayerId> -manual -playerid <id> -health <byte> -weapon <byte> -damping <byte>");
            return;
        }
        PlayerReady packet = new PlayerReady(playerId, health, weapon, damping, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerReady packet to " + targetPlayer + ": " + packet);
    }
} 