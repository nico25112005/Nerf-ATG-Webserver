package net.nerfatg.command.commands;

import net.nerfatg.NerfATGServer;
import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.packet.PacketAction;
import net.nerfatg.proxy.packet.packets.GameStarted;

public class BroadcastCommand extends Command {

    private final NerfATGServer server;

    public BroadcastCommand(String label, NerfATGServer server) {
        super(label);

        this.server = server;

        setNativeAction(this::nativeAction);

        addArgument(
                new CommandArgumentValue(0, this::broadcastMessage)
        );
    }

    private void nativeAction(CommandContext ctx) {
        System.out.println("Broadcast command started");
    }
    
    private void broadcastMessage(CommandContext ctx) {
        String message = ctx.args()[0];

        GameStarted packet = new GameStarted("AAAAAAAA",
                "BBBBBBBB", "Gotzi", (byte) 0, (byte) 5, PacketAction.Generic);

        server.getProxy().broadcast(
                packet
        );

        System.out.println("Broadcasting finished! " + message);
    }
}
