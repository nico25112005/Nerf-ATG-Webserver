package net.nerfatg.command.commands;

import net.nerfatg.command.*;
import net.nerfatg.game.GameType;
import net.nerfatg.proxy.Proxy;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.CreateGame;

public class SendCreateGameCommand extends Command {
    public SendCreateGameCommand(String label) {
        super(label);
        setHelpText("Usage: sendcreategame (-broadcast | -playerid <id>) (-random | -gameType <type> -gameName <name> -maxPlayer <max>)\n" +
                "  -broadcast: send to all connections\n" +
                "  -playerid <id>: string identifier for a connection\n" +
                "  -random: generate all parameters randomly\n" +
                "  -gameType <type>: one of " + java.util.Arrays.toString(GameType.values()) + "\n" +
                "  -gameName <name>: string (12 chars)\n" +
                "  -maxPlayer <max>: byte");
        // -broadcast -random
        addArgument(new CommandArgument("-broadcast", 0, new CommandArgument[]{
            new CommandArgument("-random", 1, this::sendRandomBroadcast),
            new CommandArgument("-gameType", 1, new CommandArgumentValue(2, new CommandArgument[]{
                new CommandArgument("-gameName", 3, new CommandArgumentValue(4, new CommandArgument[]{
                    new CommandArgument("-maxPlayer", 5, new CommandArgumentValue(6, this::sendManualBroadcast))
                }, null))
            }, null))
        }, null));
        // -playerid <id> -random
        addArgument(new CommandArgument("-playerid", 0, new CommandArgumentValue(1, new CommandArgument[]{
            new CommandArgument("-random", 2, this::sendRandomTarget),
            new CommandArgument("-gameType", 2, new CommandArgumentValue(3, new CommandArgument[]{
                new CommandArgument("-gameName", 4, new CommandArgumentValue(5, new CommandArgument[]{
                    new CommandArgument("-maxPlayer", 6, new CommandArgumentValue(7, this::sendManualTarget))
                }, null))
            }, null))
        }, null)));
    }

    private void sendRandomBroadcast(CommandContext ctx) {
        CreateGame packet = new CreateGame(CommandUtil.randomString(8), CommandUtil.randomGameType(), CommandUtil.randomString(12), CommandUtil.randomByte(1, 10), PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted CreateGame packet: " + packet);
    }

    private void sendManualBroadcast(CommandContext ctx) {
        String[] args = ctx.args();
        if (args.length < 6) {
            System.out.println(helpText);
            return;
        }
        GameType gameType = CommandUtil.parseGameType(args[2], GameType.values()[0]);
        String gameName = args[4];
        byte maxPlayer = CommandUtil.parseByte(args[6], (byte)1);
        CreateGame packet = new CreateGame(CommandUtil.randomString(8), gameType, gameName, maxPlayer, PacketAction.Generic);
        Proxy.getInstance().broadcast(packet);
        System.out.println("Broadcasted CreateGame packet: " + packet);
    }

    private void sendRandomTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        CreateGame packet = new CreateGame(playerId, CommandUtil.randomGameType(), CommandUtil.randomString(12), CommandUtil.randomByte(1, 10), PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent CreateGame packet to " + playerId + ": " + packet);
    }

    private void sendManualTarget(CommandContext ctx) {
        String[] args = ctx.args();
        String playerId = args[1];
        if (args.length < 7) {
            System.out.println(helpText);
            return;
        }
        if (!Proxy.getPlayerClients().containsKey(playerId)) {
            System.out.println("No such target: " + playerId);
            return;
        }
        GameType gameType = CommandUtil.parseGameType(args[3], GameType.values()[0]);
        String gameName = args[5];
        byte maxPlayer = CommandUtil.parseByte(args[7], (byte)1);
        CreateGame packet = new CreateGame(playerId, gameType, gameName, maxPlayer, PacketAction.Generic);
        Proxy.getInstance().send(playerId, packet);
        System.out.println("Sent CreateGame packet to " + playerId + ": " + packet);
    }
} 