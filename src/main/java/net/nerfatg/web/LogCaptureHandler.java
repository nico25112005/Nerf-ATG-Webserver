package net.nerfatg.web;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Deque;

/**
 * Captures the last N log records in memory for the debug dashboard.
 * Attach to any Logger (root or specific) to start collecting.
 */
public class LogCaptureHandler extends Handler {

    private static final int MAX_ENTRIES = 500;
    private static final Deque<LogEntry> entries = new ConcurrentLinkedDeque<>();

    public static class LogEntry {
        public final long timestamp;
        public final String level;
        public final String logger;
        public final String message;
        public final String thrown;

        LogEntry(LogRecord record) {
            this.timestamp = record.getMillis();
            this.level = record.getLevel().getName();
            this.logger = record.getLoggerName();
            this.message = record.getMessage();
            if (record.getThrown() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(record.getThrown().getClass().getName())
                  .append(": ").append(record.getThrown().getMessage()).append("\n");
                for (StackTraceElement ste : record.getThrown().getStackTrace()) {
                    sb.append("    at ").append(ste.toString()).append("\n");
                }
                this.thrown = sb.toString();
            } else {
                this.thrown = null;
            }
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"timestamp\":").append(timestamp);
            sb.append(",\"level\":\"").append(escape(level)).append("\"");
            sb.append(",\"logger\":\"").append(escape(logger)).append("\"");
            sb.append(",\"message\":\"").append(escape(message)).append("\"");
            if (thrown != null) {
                sb.append(",\"thrown\":\"").append(escape(thrown)).append("\"");
            }
            sb.append("}");
            return sb.toString();
        }

        private String escape(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"': sb.append("\\\""); break;
                    case '\\': sb.append("\\\\"); break;
                    case '\n': sb.append("\\n"); break;
                    case '\r': sb.append("\\r"); break;
                    case '\t': sb.append("\\t"); break;
                    default:
                        if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                        else sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    @Override
    public void publish(LogRecord record) {
        entries.addLast(new LogEntry(record));
        while (entries.size() > MAX_ENTRIES) {
            entries.pollFirst();
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    /**
     * Returns the last `limit` log entries as a JSON array string.
     */
    public static String getLogsAsJson(int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        int count = 0;
        for (LogEntry entry : entries) {
            if (count >= limit) break;
            // Skip entries with empty message (jline prompt spam)
            if (entry.message == null || entry.message.isEmpty() || entry.message.equals("> ")) continue;
            if (entry.message.startsWith(">") && entry.message.trim().matches("^>+$")) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append(entry.toJson());
            count++;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns all captured entries (up to MAX_ENTRIES).
     */
    public static String getAllLogsAsJson() {
        return getLogsAsJson(MAX_ENTRIES);
    }
}