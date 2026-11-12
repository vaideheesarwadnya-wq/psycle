package psycle.storage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

import psycle.interfaces.DataStorage;
import psycle.model.Cycle;
import psycle.model.DailyEntry;
import psycle.model.Symptom;
import psycle.model.User;
import psycle.service.CycleCalculator;
import psycle.service.InsightsService;
import psycle.utils.AppConstants;
import psycle.utils.CsvUtils;
import psycle.utils.DateUtils;

/**
 * Keeps all app data in memory (ArrayLists) and saves it through a DataStorage.
 * Pages never touch files directly: they only call this class.
 *
 * Methods that save data throw IOException, so the page that calls them can show
 * a friendly JOptionPane message. Invalid input throws IllegalArgumentException.
 */
public class DataManager {

    private final DataStorage storage; // interface type: any DataStorage will work
    private User user;
    private final ArrayList<Cycle> cycles = new ArrayList<>();
    private final ArrayList<DailyEntry> entries = new ArrayList<>();
    private final HashMap<String, String> settings = new HashMap<>();
    private final CycleCalculator calculator;
    private final InsightsService insights;

    private boolean guestMode = false;
    private String loadWarning = "";

    /** Uses FileStorage (saves into the data folder). */
    public DataManager() {
        this(new FileStorage());
    }

    /** Uses any DataStorage implementation. */
    public DataManager(DataStorage storage) {
        this.storage = storage;
        this.user = new User();
        this.calculator = new CycleCalculator(cycles);
        this.insights = new InsightsService(entries, calculator);
    }

    // =====================================================
    //  Loading (with a background thread)
    // =====================================================

    /** Loads everything from the files. Runs on whatever thread calls it. */
    public void loadAll() throws IOException {
        loadWarning = "";
        loadUser();
        loadCycles();
        loadEntries();
        loadSettings();
    }

    /**
     * Multithreading: loads the files in a background thread (SwingWorker) so the window
     * never freezes. onFinished runs afterwards on the Event Dispatch Thread.
     * If loading fails, getLoadWarning() contains the reason.
     */
    public void loadAllInBackground(Runnable onFinished) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadAll(); // runs in the background thread
                return null;
            }

            @Override
            protected void done() { // runs on the Event Dispatch Thread
                try {
                    get(); // re-throws any exception from doInBackground
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    loadWarning = "Loading was interrupted.";
                } catch (ExecutionException e) {
                    loadWarning = "Could not load saved data: " + e.getCause().getMessage();
                }
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        };
        worker.execute();
    }

    private void loadUser() throws IOException {
        ArrayList<String> lines = storage.loadData(AppConstants.USER_FILE);
        user = new User();
        if (lines.isEmpty()) {
            return;
        }
        String[] f = CsvUtils.splitLine(lines.get(0));
        String name = field(f, 0);
        String hash = field(f, 1);
        boolean lock = Boolean.parseBoolean(field(f, 2));
        user = new User(name, hash, lock);
    }

    private void loadCycles() throws IOException {
        cycles.clear();
        int skipped = 0;
        Iterator<String> it = storage.loadData(AppConstants.CYCLES_FILE).iterator();
        while (it.hasNext()) {
            try {
                String[] f = CsvUtils.splitLine(it.next());
                LocalDate start = DateUtils.parse(field(f, 0));
                LocalDate end = field(f, 1).isEmpty() ? null : DateUtils.parse(field(f, 1));
                cycles.add(new Cycle(start, end));
            } catch (RuntimeException e) {
                skipped++; // a damaged line must not stop the app
            }
        }
        sortCycles();
        addSkippedWarning(skipped);
    }

    private void loadEntries() throws IOException {
        entries.clear();
        int skipped = 0;
        Iterator<String> it = storage.loadData(AppConstants.ENTRIES_FILE).iterator();
        while (it.hasNext()) {
            try {
                String[] f = CsvUtils.splitLine(it.next());
                // date, flow, pain, mood, energy, skin, sleep, symptoms, notes
                DailyEntry entry = new DailyEntry(field(f, 0), field(f, 3), field(f, 1),
                        Integer.parseInt(field(f, 2)));
                entry.setEnergy(field(f, 4));
                entry.setSkin(field(f, 5));
                entry.setSleep(field(f, 6));
                String symptomText = field(f, 7);
                if (!symptomText.isEmpty()) {
                    for (String name : symptomText.split(";")) {
                        boolean custom = !isBuiltInSymptom(name);
                        entry.addSymptom(new Symptom(name, custom));
                    }
                }
                entry.setNotes(field(f, 8));
                entries.add(entry);
            } catch (RuntimeException e) {
                skipped++;
            }
        }
        sortEntries();
        addSkippedWarning(skipped);
    }

    private void loadSettings() throws IOException {
        settings.clear();
        Iterator<String> it = storage.loadData(AppConstants.SETTINGS_FILE).iterator();
        while (it.hasNext()) {
            String line = it.next();
            int equalsAt = line.indexOf('=');
            if (equalsAt > 0) {
                settings.put(line.substring(0, equalsAt),
                        CsvUtils.unescape(line.substring(equalsAt + 1)));
            }
        }
    }

    /** Returns field i, or an empty string if the line is shorter than expected. */
    private String field(String[] fields, int index) {
        return (index < fields.length) ? fields[index] : "";
    }

    private boolean isBuiltInSymptom(String name) {
        for (String option : AppConstants.SYMPTOM_OPTIONS) {
            if (option.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void addSkippedWarning(int skipped) {
        if (skipped > 0) {
            loadWarning += "Some saved records were damaged and were skipped. ";
        }
    }

    // =====================================================
    //  Saving
    // =====================================================

    public void saveUser() throws IOException {
        if (guestMode) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        lines.add(CsvUtils.join(user.getName(), user.getPinHash(),
                String.valueOf(user.isLockEnabled())));
        storage.saveData(AppConstants.USER_FILE, lines);
    }

    public void saveCycles() throws IOException {
        if (guestMode) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            String end = c.isOngoing() ? "" : DateUtils.format(c.getEndDate());
            lines.add(CsvUtils.join(DateUtils.format(c.getStartDate()), end));
        }
        storage.saveData(AppConstants.CYCLES_FILE, lines);
    }

    public void saveEntries() throws IOException {
        if (guestMode) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            DailyEntry e = it.next();
            lines.add(CsvUtils.join(e.getDate(), e.getFlow(), String.valueOf(e.getPainLevel()),
                    e.getMood(), e.getEnergy(), e.getSkin(), e.getSleep(),
                    e.getSymptomsAsText(), e.getNotes()));
        }
        storage.saveData(AppConstants.ENTRIES_FILE, lines);
    }

    public void saveSettings() throws IOException {
        if (guestMode) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        Iterator<Map.Entry<String, String>> it = settings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            lines.add(pair.getKey() + "=" + CsvUtils.escape(pair.getValue()));
        }
        storage.saveData(AppConstants.SETTINGS_FILE, lines);
    }

    // =====================================================
    //  Period records
    // =====================================================

    /** Records a new period start date. */
    public void addPeriodStart(LocalDate date) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Please select a date first.");
        }
        if (DateUtils.isFuture(date)) {
            throw new IllegalArgumentException("A period start date cannot be in the future.");
        }
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            if (c.isPeriodDay(date)) {
                throw new IllegalArgumentException(
                        "This date is already part of a recorded period.");
            }
        }
        cycles.add(new Cycle(date));
        sortCycles();
        saveCycles();
    }

    /** Sets the end date of the latest period that started on or before the given date. */
    public void setPeriodEnd(LocalDate date) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Please select a date first.");
        }
        if (DateUtils.isFuture(date)) {
            throw new IllegalArgumentException("A period end date cannot be in the future.");
        }
        Cycle target = null;
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            boolean startedBefore = !c.getStartDate().isAfter(date);
            if (startedBefore && (target == null
                    || c.getStartDate().isAfter(target.getStartDate()))) {
                target = c;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Mark a period start before this date first.");
        }
        long length = DateUtils.daysBetween(target.getStartDate(), date) + 1;
        if (length > AppConstants.MAX_PERIOD_LENGTH) {
            throw new IllegalArgumentException("A period longer than "
                    + AppConstants.MAX_PERIOD_LENGTH + " days looks wrong. Check the dates.");
        }
        target.setEndDate(date);
        saveCycles();
    }

    /** Removes the period record that contains the date. Returns true if one was removed. */
    public boolean removePeriodContaining(LocalDate date) throws IOException {
        boolean removed = false;
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            if (it.next().isPeriodDay(date)) {
                it.remove(); // safe removal while looping
                removed = true;
            }
        }
        if (removed) {
            saveCycles();
        }
        return removed;
    }

    private void sortCycles() {
        cycles.sort((a, b) -> a.getStartDate().compareTo(b.getStartDate()));
    }

    // =====================================================
    //  Daily entries
    // =====================================================

    /** Saves an entry. If an entry for the same date exists, it is replaced. */
    public void saveEntry(DailyEntry entry) throws IOException {
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            if (it.next().getDate().equals(entry.getDate())) {
                it.remove();
            }
        }
        entries.add(entry);
        sortEntries();
        saveEntries();
    }

    /** Finds the entry for a date (yyyy-MM-dd), or null if there is none. */
    public DailyEntry getEntryForDate(String date) {
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            DailyEntry e = it.next();
            if (e.getDate().equals(date)) {
                return e;
            }
        }
        return null;
    }

    /** Removes the entry for a date. Returns true if one was removed. */
    public boolean removeEntry(String date) throws IOException {
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            if (it.next().getDate().equals(date)) {
                it.remove();
                saveEntries();
                return true;
            }
        }
        return false;
    }

    /** The latest 'count' entries, newest first. */
    public ArrayList<DailyEntry> getRecentEntries(int count) {
        ArrayList<DailyEntry> recent = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0 && recent.size() < count; i--) {
            recent.add(entries.get(i));
        }
        return recent;
    }

    private void sortEntries() {
        entries.sort((a, b) -> a.getDate().compareTo(b.getDate()));
    }

    // =====================================================
    //  Settings
    // =====================================================

    public String getSetting(String key, String defaultValue) {
        return settings.containsKey(key) ? settings.get(key) : defaultValue;
    }

    public void setSetting(String key, String value) throws IOException {
        settings.put(key, value);
        saveSettings();
    }

    // =====================================================
    //  Delete everything
    // =====================================================

    /** Deletes all saved files and clears everything in memory. */
    public void deleteAllData() throws IOException {
        storage.deleteData(AppConstants.USER_FILE);
        storage.deleteData(AppConstants.CYCLES_FILE);
        storage.deleteData(AppConstants.ENTRIES_FILE);
        storage.deleteData(AppConstants.SETTINGS_FILE);
        cycles.clear();
        entries.clear();
        settings.clear();
        user = new User();
    }
    // =====================================================
    //  Date requested by another page (Calendar -> Daily Log)
    // =====================================================

    private LocalDate requestedLogDate = null;

    /** Another page asks the Daily Log page to open on this date. */
    public void requestLogDate(LocalDate date) {
        this.requestedLogDate = date;
    }

    /** Returns the requested date once, then forgets it (null if nothing was requested). */
    public LocalDate takeRequestedLogDate() {
        LocalDate date = requestedLogDate;
        requestedLogDate = null;
        return date;
    }
    // =====================================================
    //  Getters
    // =====================================================

    public User getUser() {
        return user;
    }

    public ArrayList<Cycle> getCycles() {
        return cycles;
    }

    public ArrayList<DailyEntry> getEntries() {
        return entries;
    }

    public CycleCalculator getCalculator() {
        return calculator;
    }

    public InsightsService getInsights() {
        return insights;
    }

    public boolean isGuestMode() {
        return guestMode;
    }

    /** In guest mode nothing is written to disk. */
    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
    }

    /** Text describing problems found while loading (empty if all went well). */
    public String getLoadWarning() {
        return loadWarning;
    }


    // =====================================================
    //  Guest session
    // =====================================================

    /** Starts guest mode with an empty, in-memory session. Nothing is saved to disk. */
    public void startGuestSession() {
        guestMode = true;
        cycles.clear();
        entries.clear();
        settings.clear();
        user = new User("Guest");
    }

    /** Leaves guest mode and reloads the real saved data from the files. */
    public void endGuestSession() throws IOException {
        guestMode = false;
        loadAll();
    }
}
