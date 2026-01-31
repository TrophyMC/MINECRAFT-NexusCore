package de.mecrytv.nexusCore.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    public static String formatTimestamp(long millis) {
        if (millis == -1) return "Permanent";
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_ZONE).format(DATE_TIME_FORMATTER);
    }


    public static String formatRemainingTime(long expiryMillis) {
        if (expiryMillis == -1) return "Permanent";

        long durationMillis = expiryMillis - System.currentTimeMillis();
        if (durationMillis <= 0) return "Abgelaufen";

        Duration duration = Duration.ofMillis(durationMillis);

        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 && days == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatDuration(long durationMillis) {
        if (durationMillis == -1) return "Permanent";
        if (durationMillis <= 0) return "0s";

        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" Tage ");
        if (hours > 0) sb.append(hours).append(" Stunden ");
        if (minutes > 0) sb.append(minutes).append(" Minuten ");
        if (seconds > 0 && days == 0) sb.append(seconds).append(" Sekunden");

        return sb.toString().trim();
    }

    public static long getDurationMillis(long amount, TimeUnit unit) {
        return unit.toMillis(amount);
    }

    public static long days(int d) { return TimeUnit.DAYS.toMillis(d); }
    public static long hours(int h) { return TimeUnit.HOURS.toMillis(h); }
    public static long minutes(int m) { return TimeUnit.MINUTES.toMillis(m); }
}