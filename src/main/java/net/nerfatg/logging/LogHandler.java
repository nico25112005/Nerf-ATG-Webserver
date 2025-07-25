/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging;

import jline.console.ConsoleReader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    private final LogRegistry logRegistry;
    private final ConsoleReader consoleReader;

    public LogHandler(ConsoleReader consoleReader, Formatter formatter, Formatter fileFormatter, File loggingFolder) {
        this.consoleReader = consoleReader;
        File file = new File(loggingFolder.getPath() + "/" + getSimpleDateForFile() + ".log");
        initFile(file);

        this.logRegistry = new LogRegistry(file, formatter, fileFormatter);
        setFormatter(formatter);
    }

    public LogHandler(ConsoleReader consoleReader, Formatter formatter, Formatter fileFormatter) {
        this.consoleReader = consoleReader;
        this.logRegistry = new LogRegistry(formatter, fileFormatter);
        setFormatter(formatter);
    }

    // Backward compatibility constructors (fallback to System.out)
    public LogHandler(Formatter formatter, Formatter fileFormatter, File loggingFolder) {
        this(null, formatter, fileFormatter, loggingFolder);
    }

    public LogHandler(Formatter formatter, Formatter fileFormatter) {
        this(null, formatter, fileFormatter);
    }

    private volatile boolean initializationPhase = true;

    @Override
    public void publish(LogRecord record) {
        String formatted = getFormatter().format(record);
        
        if (consoleReader != null && !initializationPhase) {
            try {
                // During normal operation, use jline properly
                consoleReader.print("\r\u001B[K" + formatted);
                consoleReader.flush();

                // Redraw the prompt line
                if (consoleReader.getPrompt() != null && !consoleReader.getPrompt().isEmpty()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    consoleReader.drawLine();
                    consoleReader.flush();
                }
            } catch (IOException e) {
                // Fallback to System.out if jline fails
                System.err.println("JLine error, falling back to System.out: " + e.getMessage());
                System.out.print(formatted);
            }
        } else {
            // During initialization or when no ConsoleReader, use System.out
            System.out.print(formatted);
        }
        
        logRegistry.register(record);
    }

    /**
     * Call this method when initialization is complete to enable full jline integration
     */
    public void setInitializationComplete() {
        this.initializationPhase = false;
    }

    public LogRegistry getRegistry() {
        return logRegistry;
    }

    @Override
    public void flush() {
        // Implementation can be added if needed
    }

    @Override
    public void close() throws SecurityException {
        // Implementation can be added if needed
    }

    private String getSimpleDateForFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    private void initFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
}
