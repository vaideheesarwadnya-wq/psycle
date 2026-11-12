package psycle.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.gui.components.RoundedTextField;
import psycle.interfaces.PageNavigator;
import psycle.model.Cycle;
import psycle.model.DailyEntry;
import psycle.service.ReportGenerator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * PDF report page. The PDF itself is created by ReportGenerator in a background thread.
 */
public class ReportPage extends AuthenticatedPage {

    private final RoundedTextField fromField = new RoundedTextField(9);
    private final RoundedTextField toField = new RoundedTextField(9);
    private final JCheckBox notesBox = new JCheckBox("Include my personal notes", true);
    private final JLabel summaryLabel =
            createLabel("", ThemeManager.FONT_BODY, ThemeManager.TEXT_DARK);
    private final JLabel statusLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
    private final JProgressBar progressBar = new JProgressBar();
    private final RoundedButton exportButton = new RoundedButton("Export PDF");
    private final ReportGenerator generator;

    public ReportPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_REPORT, dataManager, navigator);
        this.generator = new ReportGenerator(dataManager);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setPageHeader("PDF Report",
                "Create a clean report of your cycles and health logs to keep or share.");

        JPanel column = createVerticalPanel();
        column.add(alignLeft(buildRangeCard()));
        column.add(createGap(8));
        column.add(alignLeft(buildContentCard()));
        column.add(createGap(8));
        column.add(alignLeft(buildExportCard()));
        column.add(createGap(6));
        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);
    }

    private GlassPanel buildRangeCard() {
        // Each button is an ActionListener (lambda) that sets the dates
        RoundedButton last30 = new RoundedButton("Last 30 days", false);
        last30.addActionListener(e -> setLastDays(30));
        RoundedButton last90 = new RoundedButton("Last 90 days", false);
        last90.addActionListener(e -> setLastDays(90));
        RoundedButton allData = new RoundedButton("All recorded data", false);
        allData.addActionListener(e -> setAllData());

        RoundedButton updateButton = new RoundedButton("Update summary", false);
        updateButton.addActionListener(e -> updateSummary());
        fromField.addActionListener(e -> updateSummary()); // Enter key
        toField.addActionListener(e -> updateSummary());

        JPanel dateRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        dateRow.add(createLabel("From (yyyy-MM-dd):", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED));
        dateRow.add(fromField);
        dateRow.add(createLabel("To:", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED));
        dateRow.add(toField);
        dateRow.add(updateButton);

        JPanel quickRow = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        quickRow.add(last30);
        quickRow.add(last90);
        quickRow.add(allData);

        GlassPanel card = new GlassPanel(28, 16);
        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(createLabel("1. Choose a date range",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(createGap(6));
        inner.add(alignLeft(dateRow));
        inner.add(alignLeft(quickRow));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private GlassPanel buildContentCard() {
        notesBox.setOpaque(false);
        notesBox.setFont(ThemeManager.FONT_BODY);
        notesBox.setForeground(ThemeManager.TEXT_DARK);
        notesBox.setFocusPainted(false);

        GlassPanel card = new GlassPanel(28, 16);
        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(createLabel("2. What the report contains",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(createGap(4));
        inner.add(alignLeft(createLabel(wrapHtml("Period history, cycle lengths and averages, "
                + "logged symptoms, mood and flow information, a daily log table and "
                + "(optionally) your personal notes.", 560),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(6));
        inner.add(alignLeft(notesBox));
        inner.add(createGap(6));
        inner.add(alignLeft(summaryLabel));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private GlassPanel buildExportCard() {
        exportButton.addActionListener(e -> handleExport());

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setForeground(ThemeManager.ACCENT);
        progressBar.setBackground(ThemeManager.SECONDARY);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new java.awt.Dimension(200, 8));

        JPanel row = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 12, 3));
        row.add(exportButton);
        row.add(progressBar);
        row.add(statusLabel);

        GlassPanel card = new GlassPanel(28, 16);
        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(createLabel("3. Export",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(createGap(4));
        inner.add(alignLeft(createLabel(wrapHtml("The PDF contains personal health information. "
                + "It is saved only where you choose and is never uploaded.", 560),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(8));
        inner.add(alignLeft(row));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // =====================================================
    //  Showing the page
    // =====================================================

    @Override
    protected void refreshContent() {
        setLastDays(90);
        statusLabel.setText("");
        if (getDataManager().isGuestMode()) {
            statusLabel.setText("Guest mode: the report only contains this session's data.");
        }
    }

    // =====================================================
    //  Date range helpers
    // =====================================================

    private void setLastDays(int days) {
        toField.setText(DateUtils.format(DateUtils.today()));
        fromField.setText(DateUtils.format(DateUtils.today().minusDays(days - 1)));
        updateSummary();
    }

    private void setAllData() {
        LocalDate earliest = findEarliestDate();
        toField.setText(DateUtils.format(DateUtils.today()));
        fromField.setText(DateUtils.format(earliest != null ? earliest
                : DateUtils.today().minusDays(89)));
        updateSummary();
    }

    /** The oldest period start or entry date, or null if nothing is recorded. */
    private LocalDate findEarliestDate() {
        LocalDate earliest = null;
        Iterator<Cycle> cycleIt = getDataManager().getCycles().iterator();
        while (cycleIt.hasNext()) {
            LocalDate start = cycleIt.next().getStartDate();
            if (earliest == null || start.isBefore(earliest)) {
                earliest = start;
            }
        }
        Iterator<DailyEntry> entryIt = getDataManager().getEntries().iterator();
        while (entryIt.hasNext()) {
            LocalDate date = DateUtils.parse(entryIt.next().getDate());
            if (earliest == null || date.isBefore(earliest)) {
                earliest = date;
            }
        }
        return earliest;
    }

    private LocalDate readDate(RoundedTextField field, String name) {
        try {
            return DateUtils.parse(field.getText());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(name + ": " + e.getMessage());
        }
    }

    /** Reads and checks both dates. Throws IllegalArgumentException with a friendly message. */
    private LocalDate[] readRange() {
        LocalDate from = readDate(fromField, "Start date");
        LocalDate to = readDate(toField, "End date");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The start date must be on or before the end date.");
        }
        return new LocalDate[]{from, to};
    }

    /** Shows how many records fall inside the chosen range. */
    private void updateSummary() {
        try {
            LocalDate[] range = readRange();
            int periods = 0;
            int entries = 0;

            Iterator<Cycle> cycleIt = getDataManager().getCycles().iterator();
            while (cycleIt.hasNext()) {
                LocalDate start = cycleIt.next().getStartDate();
                if (!start.isBefore(range[0]) && !start.isAfter(range[1])) {
                    periods++;
                }
            }
            Iterator<DailyEntry> entryIt = getDataManager().getEntries().iterator();
            while (entryIt.hasNext()) {
                LocalDate date = DateUtils.parse(entryIt.next().getDate());
                if (!date.isBefore(range[0]) && !date.isAfter(range[1])) {
                    entries++;
                }
            }
            summaryLabel.setText("This report will include " + periods
                    + (periods == 1 ? " period" : " periods") + " and " + entries
                    + (entries == 1 ? " daily entry." : " daily entries."));
        } catch (IllegalArgumentException e) {
            summaryLabel.setText("Check the dates: " + e.getMessage());
        }
    }

    // =====================================================
    //  Exporting
    // =====================================================

    private void handleExport() {
        LocalDate[] range;
        try {
            range = readRange();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save your Psycle report");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("Psycle_Report_" + DateUtils.format(DateUtils.today()) + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = withPdfExtension(chooser.getSelectedFile());
        if (target.exists() && !confirm("The file already exists:\n" + target.getName()
                + "\n\nReplace it?", "Replace file")) {
            return;
        }

        setRunning(true);
        // The generator works in a background thread and calls this lambda on the GUI thread
        generator.generateInBackground(target, range[0], range[1], notesBox.isSelected(),
                (success, message) -> {
                    setRunning(false);
                    if (success) {
                        statusLabel.setText("Report saved.");
                        showInfo(message);
                    } else {
                        statusLabel.setText("Export failed.");
                        showError(message);
                    }
                });
    }

    private File withPdfExtension(File file) {
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".pdf");
    }

    /** Shows or hides the loading indicator and blocks a second export while one is running. */
    private void setRunning(boolean running) {
        exportButton.setEnabled(!running);
        progressBar.setVisible(running);
        statusLabel.setText(running ? "Creating your report in the background..." : "");
    }
}