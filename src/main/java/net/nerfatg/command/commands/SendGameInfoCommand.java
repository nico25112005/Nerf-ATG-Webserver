package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.game.GameType;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameInfo;

public class SendGameInfoCommand extends Command {
    public SendGameInfoCommand(String label) {
        super(label);
        setHelpText("Usage: sendgameinfo (-broadcast | -playerid <id>) (-random | -gameType <type> -gameId <id> -gameName <name> -playerCount <count> -maxPlayer <max>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -gameType <type>: one of " + java.util.Arrays.toString(GameType.values()) + "\n" +
                "  -gameId <id>: string (8 chars)\n" +
                "  -gameName <name>: string (12 chars)\n" +
                "  -playerCount <count>: byte\n" +
                "  -maxPlayer <max>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-gameType", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-gameId", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-gameName", 5, new CommandArgumentValue(6, new CommandArgument[]{
                        new CommandArgument("-playerCount", 7, new CommandArgumentValue(8, new CommandArgument[]{
                            new CommandArgument("-maxPlayer", 9, new CommandArgumentValue(10, this::sendManualBroadcast))
                        }, null))
                    }, null))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-gameType", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-gameId", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-gameName", 6, new CommandArgumentValue(7, new CommandArgument[]{
                        new CommandArgument("-playerCount", 8, new CommandArgumentValue(9, new CommandArgument[]{
                            new CommandArgument("-maxPlayer", 10, new CommandArgumentValue(11, this::sendManualTarget))
                        }, null))
                    }, null))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        GameInfo packet = new GameInfo(
            CommandUtil.randomGameType(),
            CommandUtil.randomString(8),
            CommandUtil.randomString(12),
            CommandUtil.randomByte(1, 10),
            CommandUtil.randomByte(1, 10),
            PacketAction.Generic
        );
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted GameInfo packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 10) {
            System.out.println(helpText);
            return;
        }
        GameType gameType = CommandUtil.parseGameType(args[2], GameType.values()[0]);
        String gameId = args[4];
        String gameName = args[6];
        byte playerCount = CommandUtil.parseByte(args[8], (byte)1);
        byte maxPlayer = CommandUtil.parseByte(args[10], (byte)1);
        GameInfo packet = new GameInfo(gameType, gameId, gameName, playerCount, maxPlayer, PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted GameInfo packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        GameInfo packet = new GameInfo(
            CommandUtil.randomGameType(),
            CommandUtil.randomString(8),
            CommandUtil.randomString(12),
            CommandUtil.randomByte(1, 10),
            CommandUtil.randomByte(1, 10),
            PacketAction.Generic
        );
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent GameInfo packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 11) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        GameType gameType = CommandUtil.parseGameType(args[3], GameType.values()[0]);
        String gameId = args[5];
        String gameName = args[7];
        byte playerCount = CommandUtil.parseByte(args[9], (byte)1);
        byte maxPlayer = CommandUtil.parseByte(args[11], (byte)1);
        GameInfo packet = new GameInfo(gameType, gameId, gameName, playerCount, maxPlayer, PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent GameInfo packet to " + playerId + ": " + packet);
    }
} 