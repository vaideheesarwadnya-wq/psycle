package psycle.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import psycle.model.Cycle;
import psycle.model.DailyEntry;
import psycle.utils.DateUtils;

/**
 * Exports the data as normal CSV files (readable in Excel or any text editor).
 */
public class DataExporter {

    public static final String PERIODS_FILE = "psycle_periods.csv";
    public static final String ENTRIES_FILE = "psycle_daily_entries.csv";

    /** Writes both files into the chosen folder. */
    public static void exportAll(DataManager dataManager, File folder) throws IOException {
        if (folder == null || !folder.isDirectory()) {
            throw new IOException("Please choose a valid folder.");
        }
        writePeriods(dataManager.getCycles(), new File(folder, PERIODS_FILE));
        writeEntries(dataManager.getEntries(), new File(folder, ENTRIES_FILE));
    }

    private static void writePeriods(ArrayList<Cycle> cycles, File file) throws IOException {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF'); // byte order mark so Excel shows all characters correctly
            writer.write(row("start_date", "end_date", "period_length_days"));
            writer.newLine();

            Iterator<Cycle> it = cycles.iterator();
            while (it.hasNext()) {
                Cycle c = it.next();
                writer.write(row(DateUtils.format(c.getStartDate()),
                        c.isOngoing() ? "" : DateUtils.format(c.getEndDate()),
                        c.isOngoing() ? "" : String.valueOf(c.getPeriodLength())));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException("Could not write " + file.getName() + ": " + e.getMessage(), e);
        }
    }

    private static void writeEntries(ArrayList<DailyEntry> entries, File file) throws IOException {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write(row("date", "flow", "pain_level", "mood", "energy", "skin", "sleep",
                    "symptoms", "notes"));
            writer.newLine();

            Iterator<DailyEntry> it = entries.iterator();
            while (it.hasNext()) {
                DailyEntry e = it.next();
                writer.write(row(e.getDate(), e.getFlow(), String.valueOf(e.getPainLevel()),
                        e.getMood(), e.getEnergy(), e.getSkin(), e.getSleep(),
                        e.getSymptomsAsText().replace(";", "; "), e.getNotes()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException("Could not write " + file.getName() + ": " + e.getMessage(), e);
        }
    }

    /** Joins cells into one CSV line. */
    private static String row(String... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cell(cells[i]));
        }
        return sb.toString();
    }

    /** Puts a value in quotes if it contains a comma, a quote or a line break. */
    private static String cell(String text) {
        if (text == null) {
            return "";
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private DataExporter() {
    }
}
