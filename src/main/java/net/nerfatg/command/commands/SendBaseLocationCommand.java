package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.BaseLocation;

import java.util.Random;

public class SendBaseLocationCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendBaseLocationCommand(String label, Proxy proxy) {
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
            new CommandArgument("-teamindex", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-longitude", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-latitude", 6, new CommandArgument[]{
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
            new CommandArgument("-teamindex", 3, new CommandArgument[]{
                new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-longitude", 5, new CommandArgument[]{
                        new CommandArgumentValue(6, new CommandArgument[]{
                            new CommandArgument("-latitude", 7, new CommandArgument[]{
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
        System.out.println("Usage for sendbaselocation:");
        System.out.println("  sendbaselocation -help");
        System.out.println("  sendbaselocation -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendbaselocation -broadcast -manual -teamindex <byte> -longitude <double> -latitude <double> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendbaselocation -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendbaselocation -singleconnection <targetPlayerId> -manual -teamindex <byte> -longitude <double> -latitude <double> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-teamindex <byte>: Team index");
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
        byte teamIndex = (byte) random.nextInt(2); // 0 or 1
        double longitude = random.nextDouble() * 180 - 90; // -90 to +90
        double latitude = random.nextDouble() * 360 - 180; // -180 to +180
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, action);
        proxy.broadcast(packet);
        System.out.println("Sent BaseLocation packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        Byte teamIndex = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-teamindex".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    teamIndex = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("teamindex must be a byte");
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
        if (teamIndex == null || longitude == null || latitude == null) {
            System.out.println("Usage: sendbaselocation -broadcast -manual -teamindex <byte> -longitude <double> -latitude <double> [-action <action>]");
            return;
        }
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, action);
        proxy.broadcast(packet);
        System.out.println("Sent BaseLocation packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendbaselocation -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        byte teamIndex = (byte) random.nextInt(2);
        double longitude = random.nextDouble() * 180 - 90;
        double latitude = random.nextDouble() * 360 - 180;
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent BaseLocation packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        Byte teamIndex = null;
        Double longitude = null, latitude = null;
        for (int i = 0; i < args.length; i++) {
            if ("-teamindex".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    teamIndex = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("teamindex must be a byte");
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
        if (teamIndex == null || longitude == null || latitude == null) {
            System.out.println("Usage: sendbaselocation -singleconnection <targetPlayerId> -manual -teamindex <byte> -longitude <double> -latitude <double>");
            return;
        }
        BaseLocation packet = new BaseLocation(teamIndex, longitude, latitude, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent BaseLocation packet to " + targetPlayer + ": " + packet);
    }
} 