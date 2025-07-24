package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandContext;
import net.nerfatg.proxy.Proxy;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListConnectionsCommand extends Command {
    public ListConnectionsCommand(String label) {
        super(label);
        setNativeAction(this::listConnections);
    }

    private void listConnections(CommandContext ctx) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"#", "Identifier", "Remote Address", "Open"});
        int i = 1;
        for (Map.Entry<String, SocketChannel> entry : Proxy.getPlayerClients().entrySet()) {
            String id = entry.getKey();
            SocketChannel channel = entry.getValue();
            String remote = "?";
            String open = "?";
            try {
                SocketAddress addr = channel.getRemoteAddress();
                remote = addr != null ? addr.toString() : "?";
                open = channel.isOpen() ? "YES" : "NO";
            } catch (Exception e) {
                remote = "<error>";
                open = "NO";
            }
            rows.add(new String[]{String.valueOf(i++), id, remote, open});
        }
        printTable(rows);
    }

    private void printTable(List<String[]> rows) {
        int[] widths = new int[rows.get(0).length];
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        StringBuilder sb = new StringBuilder();
        String sep = "+";
        for (int w : widths) sep += "-".repeat(w + 2) + "+";
        sb.append(sep).append("\n");
        for (int r = 0; r < rows.size(); r++) {
            String[] row = rows.get(r);
            sb.append("|");
            for (int i = 0; i < row.length; i++) {
                sb.append(" ").append(String.format("%-" + widths[i] + "s", row[i])).append(" ");
                sb.append("|");
            }
            sb.append("\n");
            if (r == 0) sb.append(sep).append("\n");
        }
        sb.append(sep);
        System.out.println(sb.toString());
    }
} 