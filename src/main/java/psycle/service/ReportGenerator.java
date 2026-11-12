package psycle.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import psycle.model.Cycle;
import psycle.model.DailyEntry;
import psycle.storage.DataManager;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * Creates the PDF report using the OpenPDF library.
 * The PDF is written in a background thread (SwingWorker) so the GUI stays responsive.
 */
public class ReportGenerator {

    /** The page gives this to the generator; it is called (on the GUI thread) when finished. */
    public interface ReportListener {
        void onFinished(boolean success, String message);
    }

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 28, Font.BOLD, ThemeManager.ACCENT);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 13, Font.NORMAL, ThemeManager.TEXT_MUTED);
    private static final Font HEADING_FONT = new Font(Font.HELVETICA, 15, Font.BOLD, ThemeManager.ACCENT);
    private static final Font TEXT_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, ThemeManager.TEXT_DARK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, ThemeManager.TEXT_DARK);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, ThemeManager.TEXT_MUTED);
    private static final Font TABLE_HEAD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

    private final DataManager dataManager;

    public ReportGenerator(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // =====================================================
    //  Multithreading: background export
    // =====================================================

    /** Starts the export in a background thread and returns immediately. */
    public void generateInBackground(File target, LocalDate from, LocalDate to,
                                     boolean includeNotes, ReportListener listener) {
        // Copy the data now (on the GUI thread), so the background thread has its own lists
        final ArrayList<Cycle> cycleCopy = new ArrayList<>(dataManager.getCycles());
        final ArrayList<DailyEntry> entryCopy = new ArrayList<>(dataManager.getEntries());
        final String userName = dataManager.getUser().getName();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Runs in a background thread
                createReport(target, from, to, includeNotes, userName, cycleCopy, entryCopy);
                return null;
            }

            @Override
            protected void done() {
                // Runs on the Event Dispatch Thread
                try {
                    get(); // re-throws any exception from doInBackground
                    listener.onFinished(true, "Your report was saved to:\n" + target.getAbsolutePath());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    listener.onFinished(false, "The export was interrupted.");
                } catch (ExecutionException e) {
                    listener.onFinished(false, "The PDF report could not be created.\n"
                            + describe(e.getCause()));
                }
            }
        };
        worker.execute();
    }

    private String describe(Throwable cause) {
        if (cause instanceof FileNotFoundException) {
            return "The file could not be written. Close it if it is open in another program, "
                    + "or choose a different folder.\n(" + cause.getMessage() + ")";
        }
        String message = cause.getMessage();
        return (message == null || message.isEmpty()) ? cause.getClass().getSimpleName() : message;
    }

    // =====================================================
    //  Building the PDF
    // =====================================================

    private void createReport(File target, LocalDate from, LocalDate to, boolean includeNotes,
                              String userName, ArrayList<Cycle> allCycles,
                              ArrayList<DailyEntry> allEntries)
            throws IOException, DocumentException {

        // ---- 1. Pick the data inside the date range ----
        ArrayList<Cycle> sorted = new ArrayList<>(allCycles);
        sorted.sort((a, b) -> a.getStartDate().compareTo(b.getStartDate()));

        ArrayList<Cycle> periods = new ArrayList<>();
        ArrayList<Integer> gaps = new ArrayList<>();         // days until the next period (or null)
        ArrayList<Integer> cycleLengths = new ArrayList<>(); // only valid gaps
        for (int i = 0; i < sorted.size(); i++) {
            Cycle c = sorted.get(i);
            if (!isInRange(c.getStartDate(), from, to)) {
                continue;
            }
            periods.add(c);
            Integer gap = null;
            if (i + 1 < sorted.size()) {
                int days = (int) DateUtils.daysBetween(c.getStartDate(), sorted.get(i + 1).getStartDate());
                if (CycleCalculator.isValidCycleLength(days)) {
                    gap = days;
                    cycleLengths.add(days);
                }
            }
            gaps.add(gap);
        }

        ArrayList<DailyEntry> entries = new ArrayList<>();
        Iterator<DailyEntry> entryIt = allEntries.iterator();
        while (entryIt.hasNext()) {
            DailyEntry entry = entryIt.next();
            if (isInRange(DateUtils.parse(entry.getDate()), from, to)) {
                entries.add(entry);
            }
        }
        entries.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        CycleCalculator calculator = new CycleCalculator(sorted);
        InsightsService insights = new InsightsService(entries, calculator);

        int periodTotal = 0;
        int periodCount = 0;
        for (Cycle c : periods) {
            if (!c.isOngoing()) {
                periodTotal += c.getPeriodLength();
                periodCount++;
            }
        }

        // ---- 2. Write the document ----
        Document document = new Document(PageSize.A4, 48, 48, 54, 54);
        try (FileOutputStream out = new FileOutputStream(target)) {
            PdfWriter.getInstance(document, out);
            document.open();

            addBanner(document, from, to, userName);

            addHeading(document, "Summary");
            PdfPTable summary = createTable(2.2f, 3f);
            addSummaryRow(summary, "Periods recorded in this range", String.valueOf(periods.size()), false);
            addSummaryRow(summary, "Average cycle length",
                    cycleLengths.isEmpty() ? "Not enough data"
                            : CycleCalculator.averageOf(cycleLengths) + " days", true);
            addSummaryRow(summary, "Average period length",
                    periodCount == 0 ? "Not enough data"
                            : Math.round((float) periodTotal / periodCount) + " days", false);
            LocalDate next = calculator.calculateNextPeriod();
            addSummaryRow(summary, "Next period (estimate)",
                    next == null ? "Not enough data" : DateUtils.formatNice(next), true);
            addSummaryRow(summary, "Daily entries in this range", String.valueOf(entries.size()), false);
            addSummaryRow(summary, "Average pain level",
                    entries.isEmpty() ? "-" : String.format("%.1f / 5", insights.getAveragePain()), true);
            document.add(summary);

            addHeading(document, "Period history and cycle lengths");
            if (periods.isEmpty()) {
                addText(document, "No periods were recorded in this date range.");
            } else {
                PdfPTable table = createTable(0.6f, 2f, 2f, 1.6f, 1.8f);
                table.setHeaderRows(1);
                table.addCell(headerCell("#"));
                table.addCell(headerCell("Start"));
                table.addCell(headerCell("End"));
                table.addCell(headerCell("Period days"));
                table.addCell(headerCell("Cycle length"));
                for (int i = 0; i < periods.size(); i++) {
                    Cycle c = periods.get(i);
                    boolean shade = (i % 2 == 1);
                    table.addCell(bodyCell(String.valueOf(i + 1), shade));
                    table.addCell(bodyCell(DateUtils.formatNice(c.getStartDate()), shade));
                    table.addCell(bodyCell(c.isOngoing() ? "-" : DateUtils.formatNice(c.getEndDate()), shade));
                    table.addCell(bodyCell(c.isOngoing() ? "-" : String.valueOf(c.getPeriodLength()), shade));
                    table.addCell(bodyCell(gaps.get(i) == null ? "-" : gaps.get(i) + " days", shade));
                }
                document.add(table);
            }

            addHeading(document, "Logged symptoms");
            addFrequencyTable(document, "Symptom", "Times logged",
                    insights.sortByCount(insights.getSymptomFrequency()),
                    "No symptoms were logged in this date range.");

            addHeading(document, "Mood information");
            addFrequencyTable(document, "Mood", "Days",
                    insights.sortByCount(insights.getMoodFrequency()),
                    "No moods were logged in this date range.");

            addHeading(document, "Flow information");
            addFrequencyTable(document, "Flow", "Days",
                    insights.sortByCount(insights.getFlowFrequency()),
                    "No flow was logged in this date range.");

            addHeading(document, "Daily log");
            if (entries.isEmpty()) {
                addText(document, "No daily entries were saved in this date range.");
            } else {
                PdfPTable table = createTable(1.7f, 1f, 0.8f, 1.2f, 1.1f, 3f);
                table.setHeaderRows(1);
                table.addCell(headerCell("Date"));
                table.addCell(headerCell("Flow"));
                table.addCell(headerCell("Pain"));
                table.addCell(headerCell("Mood"));
                table.addCell(headerCell("Energy"));
                table.addCell(headerCell("Symptoms"));
                for (int i = 0; i < entries.size(); i++) {
                    DailyEntry e = entries.get(i);
                    boolean shade = (i % 2 == 1);
                    table.addCell(bodyCell(DateUtils.formatNice(DateUtils.parse(e.getDate())), shade));
                    table.addCell(bodyCell(e.getFlow(), shade));
                    table.addCell(bodyCell(e.getPainLevel() + "/5", shade));
                    table.addCell(bodyCell(e.getMood(), shade));
                    table.addCell(bodyCell(e.getEnergy(), shade));
                    table.addCell(bodyCell(e.getSymptomsAsText().replace(";", ", "), shade));
                }
                document.add(table);
            }

            if (includeNotes) {
                addHeading(document, "Personal notes");
                boolean anyNotes = false;
                for (DailyEntry e : entries) {
                    if (!e.getNotes().trim().isEmpty()) {
                        anyNotes = true;
                        Paragraph p = new Paragraph();
                        p.add(new Phrase(DateUtils.formatNice(DateUtils.parse(e.getDate())) + ": ", BOLD_FONT));
                        p.add(new Phrase(e.getNotes().trim(), TEXT_FONT));
                        p.setSpacingAfter(4);
                        document.add(p);
                    }
                }
                if (!anyNotes) {
                    addText(document, "No notes were written in this date range.");
                }
            }

            Paragraph disclaimer = new Paragraph("Psycle gives estimates based only on the information you "
                    + "logged. This report is not medical advice. It contains personal health "
                    + "information, so share it carefully. Generated on "
                    + DateUtils.formatNice(DateUtils.today()) + ".", SMALL_FONT);
            disclaimer.setSpacingBefore(16);
            document.add(disclaimer);

            document.close();
        }
    }

    // =====================================================
    //  Small PDF helper methods
    // =====================================================

    private boolean isInRange(LocalDate date, LocalDate from, LocalDate to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private void addBanner(Document document, LocalDate from, LocalDate to, String userName)
            throws DocumentException {
        PdfPTable banner = createTable(1f);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(ThemeManager.SECONDARY);
        cell.setBorderColor(ThemeManager.PRIMARY);
        cell.setPadding(14);
        cell.addElement(new Paragraph("Psycle", TITLE_FONT));
        cell.addElement(new Paragraph("Cycle and Health Report - Your Cycle, Your Space.", SUBTITLE_FONT));
        Paragraph range = new Paragraph("Report period: " + DateUtils.formatNice(from)
                + " to " + DateUtils.formatNice(to), BOLD_FONT);
        range.setSpacingBefore(8);
        cell.addElement(range);
        if (!userName.isEmpty()) {
            cell.addElement(new Paragraph("Prepared for: " + userName, TEXT_FONT));
        }
        banner.addCell(cell);
        document.add(banner);
    }

    private void addHeading(Document document, String text) throws DocumentException {
        Paragraph heading = new Paragraph(text, HEADING_FONT);
        heading.setSpacingBefore(10);
        heading.setSpacingAfter(4);
        document.add(heading);
    }

    private void addText(Document document, String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, TEXT_FONT);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private PdfPTable createTable(float... widths) {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(10);
        return table;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEAD_FONT));
        cell.setBackgroundColor(ThemeManager.ACCENT);
        cell.setBorderColor(ThemeManager.ACCENT);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell bodyCell(String text, boolean shaded) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TEXT_FONT));
        cell.setBackgroundColor(shaded ? ThemeManager.SECONDARY : Color.WHITE);
        cell.setBorderColor(ThemeManager.PRIMARY);
        cell.setPadding(5);
        return cell;
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean shaded) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBackgroundColor(shaded ? ThemeManager.SECONDARY : Color.WHITE);
        labelCell.setBorderColor(ThemeManager.PRIMARY);
        labelCell.setPadding(6);
        table.addCell(labelCell);
        table.addCell(bodyCell(value, shaded));
    }

    /** A two-column table for symptom, mood or flow counts. */
    private void addFrequencyTable(Document document, String firstHeader, String secondHeader,
                                   ArrayList<Map.Entry<String, Integer>> rows, String emptyText)
            throws DocumentException {
        if (rows.isEmpty()) {
            addText(document, emptyText);
            return;
        }
        PdfPTable table = createTable(3f, 1.5f);
        table.addCell(headerCell(firstHeader));
        table.addCell(headerCell(secondHeader));
        for (int i = 0; i < rows.size(); i++) {
            boolean shade = (i % 2 == 1);
            table.addCell(bodyCell(rows.get(i).getKey(), shade));
            table.addCell(bodyCell(String.valueOf(rows.get(i).getValue()), shade));
        }
        document.add(table);
    }
}