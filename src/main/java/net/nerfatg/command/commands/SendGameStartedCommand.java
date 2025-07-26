package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameStarted;

import java.util.Random;

public class SendGameStartedCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendGameStartedCommand(String label, Proxy proxy) {
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
            new CommandArgument("-leaderid", 2, new CommandArgument[]{
                new CommandArgumentValue(3, new CommandArgument[]{
                    new CommandArgument("-leadername", 4, new CommandArgument[]{
                        new CommandArgumentValue(5, new CommandArgument[]{
                            new CommandArgument("-teamindex", 6, new CommandArgument[]{
                                new CommandArgumentValue(7, new CommandArgument[]{
                                    new CommandArgument("-playercount", 8, new CommandArgument[]{
                                        new CommandArgumentValue(9, new CommandArgument[]{
                                            new CommandArgument("-action", 10, new CommandArgumentValue(11, ctx -> broadcastManual(ctx))),
                                            new CommandArgumentValue(10, ctx -> broadcastManual(ctx))
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
                new CommandArgument("-leaderid", 3, new CommandArgument[]{
                        new CommandArgumentValue(4, new CommandArgument[]{
                                new CommandArgument("-leadername", 5, new CommandArgument[]{
                                        new CommandArgumentValue(6, new CommandArgument[]{
                                                new CommandArgument("-teamindex", 7, new CommandArgument[]{
                                                        new CommandArgumentValue(8, new CommandArgument[]{
                                                                new CommandArgument("-playercount", 9, new CommandArgument[]{
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
        System.out.println("Usage for sendgamestarted:");
        System.out.println("  sendgamestarted -help");
        System.out.println("  sendgamestarted -broadcast -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendgamestarted -broadcast -manual -leaderid <id> -leadername <name> -teamindex <byte> -playercount <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendgamestarted -singleconnection <targetPlayerId> -random [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println("  sendgamestarted -singleconnection <targetPlayerId> -manual -leaderid <id> -leadername <name> -teamindex <byte> -playercount <byte> [-action <Generic|Add|Update|Remove|Replace>]");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-leaderid <id>: Leader ID");
        System.out.println("-leadername <name>: Leader name");
        System.out.println("-teamindex <byte>: Team index");
        System.out.println("-playercount <byte>: Player count");
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
        GameStarted packet = new GameStarted(
                "player" + random.nextInt(1000),
                "leader" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                (byte) random.nextInt(10),
                action
        );
        proxy.broadcast(packet);
        System.out.println("Sent GameStarted packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        PacketAction action = parseAction(args);
        if (action == null) return;
        String leaderId = null, leaderName = null;
        Byte teamIndex = null, playerCount = null;
        for (int i = 0; i < args.length; i++) {
            if ("-leaderid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                leaderId = args[i + 1];
            } else if ("-leadername".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                leaderName = args[i + 1];
            } else if ("-teamindex".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    teamIndex = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("teamindex must be a number");
                    return;
                }
            } else if ("-playercount".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    playerCount = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("playercount must be a number");
                    return;
                }
            }
        }
        if (leaderId == null || leaderName == null || teamIndex == null || playerCount == null) {
            System.out.println("Usage: sendgamestarted -broadcast -manual -leaderid <id> -leadername <name> -teamindex <byte> -playercount <byte> [-action <action>]");
            return;
        }
        GameStarted packet = new GameStarted(leaderId, leaderId, leaderName, teamIndex, playerCount, action);
        proxy.broadcast(packet);
        System.out.println("Sent GameStarted packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendgamestarted -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        GameStarted packet = new GameStarted(
                "player" + random.nextInt(1000),
                "leader" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                (byte) random.nextInt(10),
                PacketAction.Add
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent GameStarted packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        String leaderId = null, leaderName = null;
        Byte teamIndex = null, playerCount = null;
        for (int i = 0; i < args.length; i++) {
            if ("-leaderid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                leaderId = args[i + 1];
            } else if ("-leadername".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                leaderName = args[i + 1];
            } else if ("-teamindex".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    teamIndex = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("teamindex must be a number");
                    return;
                }
            } else if ("-playercount".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    playerCount = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("playercount must be a number");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (leaderId == null || leaderName == null || teamIndex == null || playerCount == null) {
            System.out.println("Usage: sendgamestarted -singleconnection <targetPlayerId> -manual -leaderid <id> -leadername <name> -teamindex <byte> -playercount <byte>");
            return;
        }
        GameStarted packet = new GameStarted(leaderId, leaderId, leaderName, teamIndex, playerCount, PacketAction.Add);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent GameStarted packet to " + targetPlayer + ": " + packet);
    }
} 