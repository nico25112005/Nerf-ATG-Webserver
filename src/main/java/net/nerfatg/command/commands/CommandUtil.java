package net.nerfatg.command.commands;

import net.nerfatg.game.GameType;
import java.util.Random;

public class CommandUtil {
    private static final Random RANDOM = new Random();
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    public static byte randomByte(int min, int max) {
        return (byte) (min + RANDOM.nextInt(max - min + 1));
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    public static GameType randomGameType() {
        GameType[] values = GameType.values();
        return values[RANDOM.nextInt(values.length)];
    }

    public static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    public static byte parseByte(String s, byte def) {
        try { return Byte.parseByte(s); } catch (Exception e) { return def; }
    }
    public static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
    public static GameType parseGameType(String s, GameType def) {
        try { return GameType.valueOf(s); } catch (Exception e) { return def; }
    }
} 