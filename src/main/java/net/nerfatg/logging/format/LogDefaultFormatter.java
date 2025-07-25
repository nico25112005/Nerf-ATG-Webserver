/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging.format;

import net.nerfatg.logging.LogLevel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

public class LogDefaultFormatter extends LogFormatter {

    public LogDefaultFormatter(boolean colors) {
        super(colors);
    }

    @Override
    public String formatWithColors(LogRecord record) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        String threadName = Thread.currentThread().getName();
        String loggerName = record.getLoggerName();
        String message = record.getMessage();

        // ANSI color constants
        String BLUE_BRIGHT = "\u001B[94m";
        String RED = LogLevel.RED;
        String RESET = LogLevel.RESET;

        if (record.getLevel() instanceof LogLevel logLevel) {
            return BLUE_BRIGHT + "[" + RED + date + BLUE_BRIGHT + "] " + "[" + RED + threadName + BLUE_BRIGHT + "] " + "[" + RED + loggerName + BLUE_BRIGHT + "] " + "[" + logLevel.color + logLevel.getName() + BLUE_BRIGHT + "] -> " + logLevel.color + message + RESET + "\n";
        }

        return BLUE_BRIGHT + "[" + RED + date + BLUE_BRIGHT + "] " + "[" + RED + threadName + BLUE_BRIGHT + "] " + "[" + RED + loggerName + BLUE_BRIGHT + "] " + "[" + record.getLevel().getName() + BLUE_BRIGHT + "] -> " + message + RESET + "\n";
    }

    @Override
    public String formatWithOutColors(LogRecord record) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        String threadName = Thread.currentThread().getName();
        String levelName = record.getLevel().getName();
        String loggerName = record.getLoggerName();
        String message = record.getMessage();

        return "[" + date + "] " + "[" + threadName + "] " + "[" + loggerName + "] [" + levelName + "] -> " + message + "\n";
    }
}