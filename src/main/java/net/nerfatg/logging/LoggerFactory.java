/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging;

import jline.console.ConsoleReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating and managing NerfLogger instances
 */
public class LoggerFactory {
    
    private static final Map<String, NerfLogger> loggers = new HashMap<>();
    private static File defaultLogDirectory = new File("logs");
    private static boolean defaultUseColors = true;
    private static ConsoleReader defaultConsoleReader = null;

    /**
     * Gets or creates a logger with the specified name
     */
    public static NerfLogger getLogger(String name) {
        return loggers.computeIfAbsent(name, 
            k -> new NerfLogger(k, defaultUseColors, defaultLogDirectory, defaultConsoleReader));
    }

    /**
     * Gets or creates a logger for the specified class
     */
    public static NerfLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * Creates a logger with custom settings
     */
    public static NerfLogger createLogger(String name, boolean useColors, File logDirectory) {
        return createLogger(name, useColors, logDirectory, defaultConsoleReader);
    }

    /**
     * Creates a logger with custom settings including ConsoleReader
     */
    public static NerfLogger createLogger(String name, boolean useColors, File logDirectory, ConsoleReader consoleReader) {
        NerfLogger logger = new NerfLogger(name, useColors, logDirectory, consoleReader);
        loggers.put(name, logger);
        return logger;
    }

    /**
     * Sets the default log directory for new loggers
     */
    public static void setDefaultLogDirectory(File directory) {
        defaultLogDirectory = directory;
    }

    /**
     * Sets whether new loggers should use colors by default
     */
    public static void setDefaultUseColors(boolean useColors) {
        defaultUseColors = useColors;
    }

    /**
     * Sets the default ConsoleReader for new loggers
     */
    public static void setDefaultConsoleReader(ConsoleReader consoleReader) {
        defaultConsoleReader = consoleReader;
    }

    /**
     * Shuts down all loggers and cleans up resources
     */
    public static void shutdownAll() {
        for (NerfLogger logger : loggers.values()) {
            logger.shutdown();
        }
        loggers.clear();
    }

    /**
     * Gets all active loggers
     */
    public static Map<String, NerfLogger> getAllLoggers() {
        return new HashMap<>(loggers);
    }

    /**
     * Marks initialization as complete for all loggers to enable full jline integration
     */
    public static void setInitializationComplete() {
        for (NerfLogger logger : loggers.values()) {
            logger.setInitializationComplete();
        }
    }
}