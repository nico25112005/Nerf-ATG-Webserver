package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameInfo;
import net.nerfatg.game.GameType;

import java.util.Random;

public class SendGameInfoCommand extends Command {
    private final Proxy proxy;
    private final Random random = new Random();

    public SendGameInfoCommand(String label, Proxy proxy) {
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
                new CommandArgument("-gametype", 2, new CommandArgument[]{
                        new CommandArgumentValue(3, new CommandArgument[]{
                                new CommandArgument("-gameid", 4, new CommandArgument[]{
                                        new CommandArgumentValue(5, new CommandArgument[]{
                                                new CommandArgument("-gamename", 6, new CommandArgument[]{
                                                        new CommandArgumentValue(7, new CommandArgument[]{
                                                                new CommandArgument("-playercount", 8, new CommandArgument[]{
                                                                        new CommandArgumentValue(9, new CommandArgument[]{
                                                                                new CommandArgument("-maxplayer", 10, new CommandArgument[]{
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

        // -singleconnection branch
        CommandArgument randomSingle = new CommandArgument("-random", 2, ctx -> singleRandom(ctx));

        CommandArgument manualSingle = new CommandArgument("-manual", 2, new CommandArgument[]{
                new CommandArgument("-gametype", 3, new CommandArgument[]{
                        new CommandArgumentValue(4, new CommandArgument[]{
                                new CommandArgument("-gameid", 5, new CommandArgument[]{
                                        new CommandArgumentValue(6, new CommandArgument[]{
                                                new CommandArgument("-gamename", 7, new CommandArgument[]{
                                                        new CommandArgumentValue(8, new CommandArgument[]{
                                                                new CommandArgument("-playercount", 9, new CommandArgument[]{
                                                                        new CommandArgumentValue(10, new CommandArgument[]{
                                                                                new CommandArgument("-maxplayer", 11, new CommandArgument[]{
                                                                                        new CommandArgumentValue(12, ctx -> singleManual(ctx))
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
        System.out.println("Usage for sendgameinfo:");
        System.out.println("  sendgameinfo -help");
        System.out.println("  sendgameinfo -broadcast -random");
        System.out.println("  sendgameinfo -broadcast -manual -gametype <Team|DeathMatch> -gameid <id> -gamename <name> -playercount <count> -maxplayer <count>");
        System.out.println("  sendgameinfo -singleconnection <targetPlayerId> -random");
        System.out.println("  sendgameinfo -singleconnection <targetPlayerId> -manual -gametype <Team|DeathMatch> -gameid <id> -gamename <name> -playercount <count> -maxplayer <count>");
        System.out.println();
        System.out.println("-broadcast: Send to all clients");
        System.out.println("-singleconnection <targetPlayerId>: Send to a specific client");
        System.out.println("-random: Use random/placeholder values");
        System.out.println("-manual: Specify all properties as named arguments");
        System.out.println("-gametype <Team|DeathMatch>: Game type");
        System.out.println("-gameid <id>: Game ID");
        System.out.println("-gamename <name>: Game name");
        System.out.println("-playercount <count>: Current player count");
        System.out.println("-maxplayer <count>: Max player count");
    }

    private void broadcastRandom(CommandContext ctx) {
        GameInfo packet = new GameInfo(
                GameType.values()[random.nextInt(GameType.values().length)],
                "game" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                (byte) (1 + random.nextInt(10)),
                PacketAction.Generic
        );
        proxy.broadcast(packet);
        System.out.println("Sent GameInfo packet to all clients: " + packet);
    }

    private void broadcastManual(CommandContext ctx) {
        String[] args = ctx.args();
        GameType gameType = null;
        String gameId = null, gameName = null;
        Byte playerCount = null, maxPlayer = null;
        for (int i = 0; i < args.length; i++) {
            if ("-gametype".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    gameType = GameType.valueOf(args[i + 1]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid gametype. Use Team or DeathMatch.");
                    return;
                }
            } else if ("-gameid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                gameId = args[i + 1];
            } else if ("-gamename".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                gameName = args[i + 1];
            } else if ("-playercount".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    playerCount = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("playercount must be a number");
                    return;
                }
            } else if ("-maxplayer".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    maxPlayer = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("maxplayer must be a number");
                    return;
                }
            }
        }
        if (gameType == null || gameId == null || gameName == null || playerCount == null || maxPlayer == null) {
            System.out.println("Usage: sendgameinfo -broadcast -manual -gametype <Team|DeathMatch> -gameid <id> -gamename <name> -playercount <count> -maxplayer <count>");
            return;
        }
        GameInfo packet = new GameInfo(gameType, gameId, gameName, playerCount, maxPlayer, PacketAction.Generic);
        proxy.broadcast(packet);
        System.out.println("Sent GameInfo packet to all clients: " + packet);
    }

    private void singleRandom(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 2) {
            System.out.println("Usage: sendgameinfo -singleconnection <playerId> -random");
            return;
        }
        String targetPlayer = args[1];
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        GameInfo packet = new GameInfo(
                GameType.values()[random.nextInt(GameType.values().length)],
                "game" + random.nextInt(1000),
                "name" + random.nextInt(1000),
                (byte) random.nextInt(10),
                (byte) (1 + random.nextInt(10)),
                PacketAction.Generic
        );
        proxy.send(targetPlayer, packet);
        System.out.println("Sent GameInfo packet to " + targetPlayer + ": " + packet);
    }

    private void singleManual(CommandContext ctx) {
        String[] args = ctx.args();
        String targetPlayer = args[1];
        GameType gameType = null;
        String gameId = null, gameName = null;
        Byte playerCount = null, maxPlayer = null;
        for (int i = 0; i < args.length; i++) {
            if ("-gametype".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    gameType = GameType.valueOf(args[i + 1]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid gametype. Use Team or DeathMatch.");
                    return;
                }
            } else if ("-gameid".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                gameId = args[i + 1];
            } else if ("-gamename".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                gameName = args[i + 1];
            } else if ("-playercount".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    playerCount = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("playercount must be a number");
                    return;
                }
            } else if ("-maxplayer".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    maxPlayer = Byte.parseByte(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("maxplayer must be a number");
                    return;
                }
            }
        }
        if (!proxy.getPlayerClients().containsKey(targetPlayer)) {
            System.out.println("No such playerId connected: " + targetPlayer);
            return;
        }
        if (gameType == null || gameId == null || gameName == null || playerCount == null || maxPlayer == null) {
            System.out.println("Usage: sendgameinfo -singleconnection <targetPlayerId> -manual -gametype <Team|DeathMatch> -gameid <id> -gamename <name> -playercount <count> -maxplayer <count>");
            return;
        }
        GameInfo packet = new GameInfo(gameType, gameId, gameName, playerCount, maxPlayer, PacketAction.Generic);
        proxy.send(targetPlayer, packet);
        System.out.println("Sent GameInfo packet to " + targetPlayer + ": " + packet);
    }
} 