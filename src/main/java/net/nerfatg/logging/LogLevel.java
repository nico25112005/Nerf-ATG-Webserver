/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.logging;

public class LogLevel extends java.util.logging.Level {

    // ANSI Color constants
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String RESET = "\u001B[0m";

    public static final LogLevel ERROR = new LogLevel("Error", RED, 950);
    public static final LogLevel INFO = new LogLevel("Information", GREEN, 951);
    public static final LogLevel DEBUG = new LogLevel("Debug", CYAN, 952);
    public static final LogLevel WARNING = new LogLevel("Warning", YELLOW, 953);
    public static final LogLevel IMPORTANT = new LogLevel("Important", GREEN, 954);
    public static final LogLevel FINE = new LogLevel("Fine", GREEN, 955);

    public String color;

    protected LogLevel(String name, String color, int value) {
        super(name, value);
        this.color = color;
    }

    public String getColoredName() {
        return color + getName() + RESET;
    }
}
