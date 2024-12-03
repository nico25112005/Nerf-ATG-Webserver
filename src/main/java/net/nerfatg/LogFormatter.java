package net.nerfatg;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private final SimpleDateFormat dateFormat;

    public LogFormatter() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public String format(LogRecord record) {
        return String.format("%s %s(%s): %s%n", dateFormat.format(record.getMillis()), record.getLoggerName(), record.getLevel(), record.getMessage() );
    }
}
