package fr.Nosta.ChillUHC.Utils;

public class TimeUtils {

    public static String formatToMMSS(long totalSeconds) {
        long mins = totalSeconds / 60;
        long secs = totalSeconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static String formatToCompactMinutesSeconds(long totalSeconds) {
        long mins = totalSeconds / 60;
        long secs = totalSeconds % 60;
        return String.format("%dm%02ds", mins, secs);
    }
}
