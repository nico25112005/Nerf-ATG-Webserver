package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.PlayerStatus;

import java.util.Random;

public class SendPlayerStatusCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendPlayerStatusCommand(String label, Proxy proxy) {
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
            new CommandArgument("-playerid", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-name", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-index", 6, new CommandArgument[]{
                                new CommandArgumentValue(7, new CommandArgument[]{
                                    new CommandArgument("-longitude", 8, new CommandArgument[]{
                                        new CommandArgumentValue(9, new CommandArgument[]{
                                            new CommandArgument("-latitude", 10, new CommandArgument[]{
                                                new CommandArgumentValue(11, new CommandArgument[]{
                                                    new CommandArgument("-health", 12, new CommandArgument[]{
                                                        new CommandArgumentValue(13, new CommandArgument[]{
                                                            new CommandArgument("-action", 14, new CommandArgumentValue(15, ctx -> broadcastManual(ctx))),
                                                            new CommandArgumentValue(14, ctx -> broadcastManual(ctx))
                                                        }, null)
                                                    }, null)
                                                }, null)
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

        // -singleconnection branch
        CommandArgument randomSingle = new CommandArgument("-random", 2, ctx -> singleRandom(ctx));

        CommandArgument manualSingle = new CommandArgument("-manual", 2, new CommandArgument[]{
                new CommandArgument("-playerid", 3, new CommandArgument[]{
                        new CommandArgumentValue(4, new CommandArgument[]{
                                new CommandArgument("-name", 5, new CommandArgument[]{
                                        new CommandArgumentValue(6, new CommandArgument[]{
                                                new CommandArgument("-index", 7, new CommandArgument[]{
                                                        new CommandArgumentValue(8, new CommandArgument[]{
                                                                new CommandArgument("-longitude", 9, new CommandArgument[]{
                                                                        new CommandArgumentValue(10, new CommandArgument[]{
                                                                                new CommandArgument("-latitude", 11, new CommandArgument[]{
                                                                                        new CommandArgumentValue(12, new CommandArgument[]{
                                                                                                new CommandArgument("-health", 13, new CommandArgument[]{
                                                                                                        new CommandArgumentValue(14, new CommandArgument[]{
                                                                                                                new CommandArgument("-action", 15, new CommandArgumentValue(16, ctx -> singleManual(ctx))),
                                                                                                                new CommandArgumentValue(15, ctx -> singleManual(ctx))
                                                                                                        }, null)
                                                                                                }, null)
                                                                                        }, null)
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
        System.out.println("Usage for sendplayerstatus:");
        System.out.println("  sendplayerstatus -help");
        System.out.println("  sendplayerstatus -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerstatus -broadcast -manual -playerid <id> -name <name> -index <byte> -longitude <double> -latitude <double> -health <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerstatus -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendplayerstatus -singleconnection <targetPlayerId> -manual -playerid <id> -name <name> -index <byte> -longitude <double> -latitude <double> -health <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-playerid <id>: Player ID");
        System.out.println("-name <name>: Player name");
        System.out.println("-index <byte>: Team index");
        System.out.println("-longitude <double>: Longitude");
        System.out.println("-latitude <double>: Latitude");
        System.out.println("-health <byte>: Health value");
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
        PlayerStatus packet = new PlayerStatus(
                "player" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                random.nextDouble() * 180 - 90,
                random.nextDouble() * 360 - 180,
                (byte) random.nextInt(100),
                action
        );
        proxy.broadcast(packet);
        System.out.println("Sent PlayerStatus packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        String playerId = null, name = null;
        Byte index = null, health = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a number");
                    return;
                }
            } else if ("-longitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    longitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("longitude must be a number");
                    return;
                }
            } else if ("-latitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    latitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("latitude must be a number");
                    return;
                }
            } else if ("-health".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    health = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("health must be a number");
                    return;
                }
            }
        }
        if (playerId == null || name == null || index == null || longitude == null || latitude == null || health == null) {
            System.out.println("Usage: sendplayerstatus -broadcast -manual -playerid <id> -name <name> -index <byte> -longitude <double> -latitude <double> -health <byte> [-action <action>]");
            return;
        }
        PlayerStatus packet = new PlayerStatus(playerId, name, index, longitude, latitude, health, action);
        proxy.broadcast(packet);
        System.out.println("Sent PlayerStatus packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendplayerstatus -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        PacketAction action = parseAction(args);
        PlayerStatus packet = new PlayerStatus(
                "player" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                random.nextDouble() * 180 - 90,
                random.nextDouble() * 360 - 180,
                (byte) random.nextInt(100),
                action
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerStatus packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String playerId = null, name = null;
        Byte index = null, health = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-playerid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                playerId = args[i + 1];
            } else if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a number");
                    return;
                }
            } else if ("-longitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    longitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("longitude must be a number");
                    return;
                }
            } else if ("-latitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    latitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("latitude must be a number");
                    return;
                }
            } else if ("-health".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    health = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("health must be a number");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (playerId == null || name == null || index == null || longitude == null || latitude == null || health == null) {
            System.out.println("Usage: sendplayerstatus -singleconnection <targetPlayerId> -manual -playerid <id> -name <name> -index <byte> -longitude <double> -latitude <double> -health <byte> [-action <action>]");
            return;
        }
        PacketAction action = parseAction(args);
        PlayerStatus packet = new PlayerStatus(playerId, name, index, longitude, latitude, health, action);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent PlayerStatus packet to " + targetPlayer + ": " + packet);
    }
} 