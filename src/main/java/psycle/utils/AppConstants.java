package psycle.utils;

/**
 * Application-wide constants.
 * Everything here is static, so we never create an AppConstants object.
 */
public class AppConstants {

    // ---------- Application info ----------
    public static final String APP_NAME = "Psycle";
    public static final String TAGLINE = "Your Cycle, Your Space.";
    public static final String VERSION = "1.0";

    // ---------- Local storage ----------
    public static final String DATA_FOLDER = "data";
    public static final String USER_FILE = "user.txt";
    public static final String CYCLES_FILE = "cycles.txt";
    public static final String ENTRIES_FILE = "daily_entries.txt";
    public static final String SETTINGS_FILE = "settings.txt";

    // ---------- Cycle rules (used by CycleCalculator) ----------
    public static final int DEFAULT_CYCLE_LENGTH = 28;
    public static final int DEFAULT_PERIOD_LENGTH = 5;
    public static final int MIN_CYCLE_LENGTH = 15;
    public static final int MAX_CYCLE_LENGTH = 60;
    public static final int MAX_PERIOD_LENGTH = 15;
    public static final int LUTEAL_PHASE_LENGTH = 14;

    // ---------- PIN rules ----------
    public static final int MIN_PIN_LENGTH = 4;
    public static final int MAX_PIN_LENGTH = 6;

    // ---------- Page names (used by CardLayout navigation) ----------
    public static final String PAGE_WELCOME = "welcome";
    public static final String PAGE_DASHBOARD = "dashboard";
    public static final String PAGE_CALENDAR = "calendar";
    public static final String PAGE_LOG = "log";
    public static final String PAGE_INSIGHTS = "insights";
    public static final String PAGE_PHASES = "phases";
    public static final String PAGE_SETTINGS = "settings";
    public static final String PAGE_REPORT = "report";

    // ---------- Phase names ----------
    public static final String PHASE_MENSTRUAL = "Menstrual";
    public static final String PHASE_FOLLICULAR = "Follicular";
    public static final String PHASE_OVULATORY = "Ovulatory";
    public static final String PHASE_LUTEAL = "Luteal";
    public static final String PHASE_UNKNOWN = "Unknown";

    // ---------- Options for the Daily Log form ----------
    public static final String[] FLOW_OPTIONS = {"None", "Light", "Medium", "Heavy"};
    public static final String[] MOOD_OPTIONS =
            {"Happy", "Sad", "Irritated", "Anxious", "Calm", "Emotional"};
    public static final String[] ENERGY_OPTIONS = {"Low", "Medium", "High"};
    public static final String[] SKIN_OPTIONS = {"Clear", "Acne", "Oily", "Dry"};
    public static final String[] SLEEP_OPTIONS = {"Poor", "Average", "Good"};
    public static final String[] SYMPTOM_OPTIONS =
            {"Cramps", "Headache", "Bloating", "Fatigue", "Breast tenderness", "Back pain"};

    // Private constructor: nobody should create an AppConstants object.
    private AppConstants() {
    }
}
