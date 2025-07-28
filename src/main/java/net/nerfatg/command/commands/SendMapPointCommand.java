package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.MapPoint;

import java.util.Random;

public class SendMapPointCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendMapPointCommand(String label, Proxy proxy) {
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
            new CommandArgument("-name", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-index", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-longitude", 6, new CommandArgument[]{
                                new CommandArgumentValue(7, new CommandArgument[]{
                                    new CommandArgument("-latitude", 8, new CommandArgument[]{
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
            new CommandArgument("-name", 3, new CommandArgument[]{
                new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-index", 5, new CommandArgument[]{
                        new CommandArgumentValue(6, new CommandArgument[]{
                            new CommandArgument("-longitude", 7, new CommandArgument[]{
                                new CommandArgumentValue(8, new CommandArgument[]{
                                    new CommandArgument("-latitude", 9, new CommandArgument[]{
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
        System.out.println("Usage for sendmappoint:");
        System.out.println("  sendmappoint -help");
        System.out.println("  sendmappoint -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendmappoint -broadcast -manual -name <name> -index <byte> -longitude <double> -latitude <double> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendmappoint -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendmappoint -singleconnection <targetPlayerId> -manual -name <name> -index <byte> -longitude <double> -latitude <double> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-name <name>: Name");
        System.out.println("-index <byte>: Index");
        System.out.println("-longitude <double>: Longitude");
        System.out.println("-latitude <double>: Latitude");
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
        MapPoint packet = new MapPoint(
            "name" + random.nextInt(1000),
            (byte) random.nextInt(2),
            random.nextDouble() * 180 - 90,
            random.nextDouble() * 360 - 180,
            action
        );
        proxy.broadcast(packet);
        System.out.println("Sent MapPoint packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        String name = null;
        Byte index = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a byte");
                    return;
                }
            } else if ("-longitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    longitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("longitude must be a double");
                    return;
                }
            } else if ("-latitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    latitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("latitude must be a double");
                    return;
                }
            }
        }
        if (name == null || index == null || longitude == null || latitude == null) {
            System.out.println("Usage: sendmappoint -broadcast -manual -name <name> -index <byte> -longitude <double> -latitude <double> [-action <action>]");
            return;
        }
        MapPoint packet = new MapPoint(name, index, longitude, latitude, action);
        proxy.broadcast(packet);
        System.out.println("Sent MapPoint packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendmappoint -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        MapPoint packet = new MapPoint(
            "name" + random.nextInt(1000),
            (byte) random.nextInt(2),
            random.nextDouble() * 180 - 90,
            random.nextDouble() * 360 - 180,
            PacketAction.Add
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent MapPoint packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String name = null;
        Byte index = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-name".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                name = args[i + 1];
            } else if ("-index".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    index = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("index must be a byte");
                    return;
                }
            } else if ("-longitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    longitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("longitude must be a double");
                    return;
                }
            } else if ("-latitude".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    latitude = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("latitude must be a double");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (name == null || index == null || longitude == null || latitude == null) {
            System.out.println("Usage: sendmappoint -singleconnection <targetPlayerId> -manual -name <name> -index <byte> -longitude <double> -latitude <double>");
            return;
        }
        MapPoint packet = new MapPoint(name, index, longitude, latitude, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent MapPoint packet to " + targetPlayer + ": " + packet);
    }
} 