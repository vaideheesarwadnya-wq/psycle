package psycle.service;

import java.util.ArrayList;
import psycle.storage.DataManager;
import psycle.utils.DateUtils;

/**
 * Simple in-app reminders (no operating-system notifications).
 * Settings are stored in settings.txt through DataManager.
 */
public class ReminderService {

    public static final String KEY_DAILY = "reminder.daily";
    public static final String KEY_PERIOD = "reminder.period";
    public static final String KEY_DAYS = "reminder.days";
    public static final String KEY_DISCREET = "reminder.discreet";
    public static final int DEFAULT_DAYS_BEFORE = 2;

    /** Reads a true/false setting. */
    public static boolean isEnabled(DataManager dataManager, String key, boolean defaultValue) {
        return Boolean.parseBoolean(dataManager.getSetting(key, String.valueOf(defaultValue)));
    }

    /** How many days before the predicted period the reminder starts (1 to 7). */
    public static int getDaysBefore(DataManager dataManager) {
        try {
            int days = Integer.parseInt(
                    dataManager.getSetting(KEY_DAYS, String.valueOf(DEFAULT_DAYS_BEFORE)));
            return Math.max(1, Math.min(7, days));
        } catch (NumberFormatException e) {
            return DEFAULT_DAYS_BEFORE; // damaged setting: use the default
        }
    }

    /** All reminders that are due right now. */
    public static ArrayList<String> getReminders(DataManager dataManager) {
        ArrayList<String> reminders = new ArrayList<>();
        boolean discreet = isEnabled(dataManager, KEY_DISCREET, true);

        // Daily check-in: only if nothing is logged for today
        if (isEnabled(dataManager, KEY_DAILY, false)
                && dataManager.getEntryForDate(DateUtils.format(DateUtils.today())) == null) {
            reminders.add(discreet ? "Time for your daily check-in."
                    : "Remember to log how you feel today.");
        }

        // Upcoming period (needs at least one recorded period)
        if (isEnabled(dataManager, KEY_PERIOD, false) && dataManager.getCalculator().hasData()) {
            int days = dataManager.getCalculator().getDaysUntilNextPeriod();
            if (days >= 0 && days <= getDaysBefore(dataManager)) {
                String when = (days == 0) ? "today" : (days == 1) ? "tomorrow" : "in " + days + " days";
                reminders.add(discreet ? "A calendar reminder is due " + when + "."
                        : "Your period is expected " + when + " (estimate).");
            }
        }
        return reminders;
    }

    /** All due reminders joined with line breaks (ready to put inside an HTML label). */
    public static String getReminderText(DataManager dataManager) {
        StringBuilder sb = new StringBuilder();
        for (String reminder : getReminders(dataManager)) {
            if (sb.length() > 0) {
                sb.append("<br>");
            }
            sb.append(reminder);
        }
        return sb.toString();
    }

    private ReminderService() {
    }
}
