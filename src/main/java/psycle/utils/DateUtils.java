package psycle.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Static utility methods for working with dates.
 * Dates are stored in files as yyyy-MM-dd (for example 2026-11-09).
 */
public class DateUtils {

    private static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    /** Converts text like "2026-11-09" into a LocalDate. Throws a friendly error if invalid. */
    public static LocalDate parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("The date is empty.");
        }
        try {
            return LocalDate.parse(text.trim(), STORAGE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + text + " (use yyyy-MM-dd)");
        }
    }

    /** Returns true if the text is a valid yyyy-MM-dd date. */
    public static boolean isValid(String text) {
        try {
            parse(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** LocalDate -> "2026-11-09" (used for saving to files). */
    public static String format(LocalDate date) {
        return date.format(STORAGE_FORMAT);
    }

    /** LocalDate -> "9 Nov 2026" (used for showing to the user). */
    public static String formatNice(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DISPLAY_FORMAT);
    }

    /** Number of days from 'from' to 'to' (negative if 'to' is earlier). */
    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    private DateUtils() {
    }
}
