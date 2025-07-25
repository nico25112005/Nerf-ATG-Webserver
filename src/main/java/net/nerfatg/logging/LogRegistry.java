/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogRegistry {

    private final List<LogRecord> history;
    private final File logFile;
    private final Formatter infoFormatter;
    private final Formatter showFormatter;
    private PrintWriter fileWriter;

    public LogRegistry(File file, Formatter infoFormatter, Formatter showFormatter) {
        this.history = new ArrayList<>();
        this.logFile = file;
        this.infoFormatter = infoFormatter;
        this.showFormatter = showFormatter;
        
        try {
            this.fileWriter = new PrintWriter(new FileWriter(file, true));
        } catch (IOException e) {
            System.err.println("Failed to initialize log file writer: " + e.getMessage());
        }
    }

    public LogRegistry(Formatter infoFormatter, Formatter showFormatter) {
        this.history = new ArrayList<>();
        this.logFile = null;
        this.infoFormatter = infoFormatter;
        this.showFormatter = showFormatter;
        this.fileWriter = null;
    }

    public void register(LogRecord logRecord) {
        history.add(logRecord);
        
        // Write to file if file writer is available
        if (fileWriter != null) {
            fileWriter.print(showFormatter.format(logRecord));
            fileWriter.flush();
        }
    }

    public void unregister(LogRecord logRecord) {
        history.remove(logRecord);
    }

    public List<LogRecord> getHistory() {
        return new ArrayList<>(history);
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}