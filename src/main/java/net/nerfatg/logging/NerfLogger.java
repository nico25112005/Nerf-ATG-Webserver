/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging;

import jline.console.ConsoleReader;
import net.nerfatg.logging.format.LogDefaultFormatter;

import java.io.File;
import java.util.logging.Logger;

/**
 * Enhanced logger for NerfATG project with colored output and file logging
 */
public class NerfLogger extends Logger implements LoggerInterface {

    private final LogDispatcher<NerfLogger> dispatcher;
    private final LogHandler logHandler;

    public NerfLogger(String name, boolean useColors, File logDirectory, ConsoleReader consoleReader) {
        super(name, null);
        
        // Create formatters
        LogDefaultFormatter consoleFormatter = new LogDefaultFormatter(useColors);
        LogDefaultFormatter fileFormatter = new LogDefaultFormatter(false); // No colors in file
        

        this.logHandler = new LogHandler(consoleReader, consoleFormatter, fileFormatter, logDirectory);
        
        // Add handler to logger
        addHandler(logHandler);
        setUseParentHandlers(false);
        
        // Create and start dispatcher thread
        this.dispatcher = new LogDispatcher<>(this);
        this.dispatcher.start();
    }

    public NerfLogger(String name, boolean useColors, File logDirectory) {
        this(name, useColors, logDirectory, null);
    }

    public NerfLogger(String name, boolean useColors) {
        this(name, useColors, null, null);
    }

    public NerfLogger(String name) {
        this(name, true, null, null);
    }

    public NerfLogger(String name, ConsoleReader consoleReader) {
        this(name, true, null, consoleReader);
    }

    @Override
    public void doLog(java.util.logging.LogRecord record) {
        super.log(record);
    }

    // Convenience methods for custom log levels
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void important(String message) {
        log(LogLevel.IMPORTANT, message);
    }

    public LogRegistry getLogRegistry() {
        return logHandler.getRegistry();
    }

    public void shutdown() {
        if (dispatcher != null) {
            dispatcher.interrupt();
        }
        if (logHandler != null && logHandler.getRegistry() != null) {
            logHandler.getRegistry().close();
        }
    }

    /**
     * Call this method when initialization is complete to enable full jline integration
     */
    public void setInitializationComplete() {
        if (logHandler != null) {
            logHandler.setInitializationComplete();
        }
    }
}