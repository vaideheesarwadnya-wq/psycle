package psycle.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import psycle.events.CalendarMouseListener;
import psycle.events.DateSelectionListener;
import psycle.gui.components.ColorDot;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.interfaces.PageNavigator;
import psycle.model.Cycle;
import psycle.model.DailyEntry;
import psycle.service.CycleCalculator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * Period calendar built from simple Swing components (no calendar library).
 */
public class CalendarPage extends AuthenticatedPage {

    private static final String[] WEEKDAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final int DETAILS_WIDTH = 310;
    private static final int TEXT_WIDTH = 245;

    private YearMonth displayedMonth = YearMonth.now();
    private LocalDate selectedDate = DateUtils.today();

    // ---------- Calendar side ----------
    private final JLabel monthLabel =
            createLabel("", ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK);
    private final JPanel dayGrid = createTransparentPanel(new GridLayout(0, 7, 4, 4));

    // ---------- Details side ----------
    private final JLabel dateTitleLabel =
            createLabel("", ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK);
    private final JLabel statusLabel =
            createLabel("", ThemeManager.FONT_BODY.deriveFont(Font.BOLD), ThemeManager.ACCENT);
    private final JLabel cycleInfoLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
    private final JLabel entryLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK);
    private final JLabel nextPeriodLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    // The mouse listener calls this when a day is clicked (interface DateSelectionListener)
    private final DateSelectionListener dateListener = this::selectDate;

    public CalendarPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_CALENDAR, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setPageHeader("Period Calendar",
                "Click a date to see details, then mark when your period starts or ends.");

        JPanel body = createTransparentPanel(new BorderLayout(10, 0));
        body.add(buildCalendarCard(), BorderLayout.CENTER);
        body.add(buildDetailsCard(), BorderLayout.EAST);
        getContentPanel().add(body, BorderLayout.CENTER);

        buildGrid();
        updateDetails();
    }

    private GlassPanel buildCalendarCard() {
        GlassPanel card = new GlassPanel(28, 16);
        card.setLayout(new BorderLayout(0, 8));

        // --- Top row: Previous / Next, month name, Today ---
        JPanel nav = createTransparentPanel(new BorderLayout(10, 0));

        RoundedButton previousButton = new RoundedButton("Previous", false);
        previousButton.addActionListener(e -> changeMonth(-1)); // ActionListener
        RoundedButton nextButton = new RoundedButton("Next", false);
        nextButton.addActionListener(e -> changeMonth(1));
        JPanel arrows = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        arrows.add(previousButton);
        arrows.add(nextButton);

        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        RoundedButton todayButton = new RoundedButton("Today", false);
        todayButton.addActionListener(e -> goToToday());

        nav.add(arrows, BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(todayButton, BorderLayout.EAST);

        // --- Middle: weekday names + the grid of days ---
        JPanel weekdays = createTransparentPanel(new GridLayout(1, 7, 4, 0));
        for (String name : WEEKDAYS) {
            JLabel label = createLabel(name,
                    ThemeManager.FONT_SMALL.deriveFont(Font.BOLD), ThemeManager.TEXT_MUTED);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            weekdays.add(label);
        }
        JPanel gridArea = createTransparentPanel(new BorderLayout(0, 4));
        gridArea.add(weekdays, BorderLayout.NORTH);
        gridArea.add(dayGrid, BorderLayout.CENTER);

        // --- Bottom: legend ---
        JPanel legend = createTransparentPanel(new GridLayout(2, 2, 0, 0));
        legend.add(ColorDot.legendItem(ThemeManager.PERIOD_DAY, "Recorded period"));
        legend.add(ColorDot.legendItem(ThemeManager.PREDICTED_DAY, "Predicted (estimate)"));
        legend.add(ColorDot.legendItem(ThemeManager.ACCENT, "Day has a health log"));
        legend.add(ColorDot.legendItem(Color.WHITE, "Today (outlined)"));

        card.add(nav, BorderLayout.NORTH);
        card.add(gridArea, BorderLayout.CENTER);
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private GlassPanel buildDetailsCard() {
        // Anonymous subclass: fixed width, height follows the content
        GlassPanel card = new GlassPanel(28, 16) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(DETAILS_WIDTH, super.getPreferredSize().height);
            }
        };

        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(dateTitleLabel));
        inner.add(createGap(6));
        inner.add(alignLeft(statusLabel));
        inner.add(createGap(4));
        inner.add(alignLeft(cycleInfoLabel));
        inner.add(createGap(10));
        inner.add(alignLeft(entryLabel));
        inner.add(createGap(10));
        inner.add(alignLeft(nextPeriodLabel));
        inner.add(createGap(16));

        RoundedButton startButton = new RoundedButton("Mark period start");
        startButton.addActionListener(e -> handleMarkStart());
        RoundedButton endButton = new RoundedButton("Mark period end");
        endButton.addActionListener(e -> handleMarkEnd());
        RoundedButton removeButton = new RoundedButton("Remove period record", false);
        removeButton.addActionListener(e -> handleRemove());
        RoundedButton logButton = new RoundedButton("Log this day", false);
        logButton.addActionListener(e -> handleLogDay());

        JComponent[] buttons = {startButton, endButton, removeButton, logButton};
        for (JComponent button : buttons) {
            stretchWidth(button);
            inner.add(button);
            inner.add(createGap(8));
        }

        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    // =====================================================
    //  The calendar grid
    // =====================================================

    /** Rebuilds all day cells for the displayed month. */
    private void buildGrid() {
        dayGrid.removeAll();
        monthLabel.setText(displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + displayedMonth.getYear());

        DataManager dm = getDataManager();
        CycleCalculator calc = dm.getCalculator();
        LocalDate today = DateUtils.today();

        // Monday = 1 ... Sunday = 7, so Monday needs 0 blank cells before the 1st
        int blanks = displayedMonth.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < blanks; i++) {
            dayGrid.add(createBlankCell());
        }

        int daysInMonth = displayedMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = displayedMonth.atDay(day);

            boolean period = calc.isRecordedPeriodDay(date);
            // Only show predictions from today onwards
            boolean predicted = !period && !date.isBefore(today)
                    && calc.isPredictedPeriodDay(date);
            boolean hasEntry = dm.getEntryForDate(DateUtils.format(date)) != null;

            DayCell cell = new DayCell(day, period, predicted, hasEntry,
                    date.equals(today), date.equals(selectedDate));

            // MouseListener: separate class (psycle.events.CalendarMouseListener)
            cell.addMouseListener(new CalendarMouseListener(date, cell, dateListener));
            dayGrid.add(cell);
        }

        // Always fill up to 6 rows (42 cells) so the grid does not change size
        for (int i = blanks + daysInMonth; i < 42; i++) {
            dayGrid.add(createBlankCell());
        }
        dayGrid.revalidate();
        dayGrid.repaint();
    }

    private JPanel createBlankCell() {
        JPanel blank = new JPanel();
        blank.setOpaque(false);
        return blank;
    }

    private void changeMonth(int delta) {
        displayedMonth = displayedMonth.plusMonths(delta);
        buildGrid();
    }

    private void goToToday() {
        displayedMonth = YearMonth.now();
        selectedDate = DateUtils.today();
        buildGrid();
        updateDetails();
    }

    /** Called by CalendarMouseListener (through DateSelectionListener) when a day is clicked. */
    private void selectDate(LocalDate date) {
        selectedDate = date;
        buildGrid();
        updateDetails();
    }

    @Override
    protected void refreshContent() {
        buildGrid();
        updateDetails();
    }

    // =====================================================
    //  The details panel
    // =====================================================

    private Cycle findPeriodContaining(LocalDate date) {
        Iterator<Cycle> it = getDataManager().getCycles().iterator();
        while (it.hasNext()) {
            Cycle cycle = it.next();
            if (cycle.isPeriodDay(date)) {
                return cycle;
            }
        }
        return null;
    }

    private void updateDetails() {
        DataManager dm = getDataManager();
        CycleCalculator calc = dm.getCalculator();
        LocalDate today = DateUtils.today();

        dateTitleLabel.setText(wrapHtml(longDate(selectedDate), TEXT_WIDTH));

        // 1. Period status
        Cycle period = findPeriodContaining(selectedDate);
        String status;
        if (period != null) {
            int dayNumber = (int) DateUtils.daysBetween(period.getStartDate(), selectedDate) + 1;
            status = "Recorded period day (day " + dayNumber
                    + (period.isOngoing() ? ", no end date yet)" : " of " + period.getPeriodLength() + ")");
        } else if (!selectedDate.isBefore(today) && calc.isPredictedPeriodDay(selectedDate)) {
            status = "Predicted period day (estimate)";
        } else {
            status = "No period recorded for this day.";
        }
        statusLabel.setText(wrapHtml(status, TEXT_WIDTH));

        // 2. Cycle day and phase (only for today and earlier days)
        String cycleText = "";
        if (!DateUtils.isFuture(selectedDate)) {
            int cycleDay = calc.getCycleDayFor(selectedDate);
            if (cycleDay >= 1 && cycleDay <= AppConstants.MAX_CYCLE_LENGTH) {
                String phase = CycleCalculator.phaseForDay(cycleDay,
                        calc.getAverageCycleLength(), calc.getAveragePeriodLength());
                cycleText = "Cycle day " + cycleDay + ", " + phase + " phase (estimate).";
            }
        }
        cycleInfoLabel.setText(wrapHtml(cycleText, TEXT_WIDTH));

        // 3. Health entry of that day
        DailyEntry entry = dm.getEntryForDate(DateUtils.format(selectedDate));
        String entryText;
        if (entry == null) {
            entryText = "No health entry for this day.";
        } else {
            entryText = "Logged: mood " + entry.getMood().toLowerCase()
                    + ", flow " + entry.getFlow().toLowerCase()
                    + ", pain " + entry.getPainLevel() + "/5.";
            if (!entry.getSymptomsAsText().isEmpty()) {
                entryText += " Symptoms: " + entry.getSymptomsAsText().replace(";", ", ") + ".";
            }
        }
        entryLabel.setText(wrapHtml(entryText, TEXT_WIDTH));

        // 4. Next predicted period
        LocalDate next = calc.calculateNextPeriod();
        nextPeriodLabel.setText(wrapHtml(next == null ? "Start tracking your cycle."
                : "Next predicted start: " + DateUtils.formatNice(next) + " (estimate).",
                TEXT_WIDTH));
    }

    // =====================================================
    //  Button actions
    // =====================================================

    private void handleMarkStart() {
        try {
            getDataManager().addPeriodStart(selectedDate);
            refreshContent();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("The period could not be saved.\n" + e.getMessage());
        }
    }

    private void handleMarkEnd() {
        try {
            getDataManager().setPeriodEnd(selectedDate);
            refreshContent();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("The period could not be saved.\n" + e.getMessage());
        }
    }

    private void handleRemove() {
        if (findPeriodContaining(selectedDate) == null) {
            showInfo("There is no recorded period on this date.");
            return;
        }
        if (!confirm("Remove the period record that includes "
                + DateUtils.formatNice(selectedDate) + "?", "Remove period")) {
            return;
        }
        try {
            getDataManager().removePeriodContaining(selectedDate);
            refreshContent();
        } catch (IOException e) {
            showError("The change could not be saved.\n" + e.getMessage());
        }
    }

    private void handleLogDay() {
        if (DateUtils.isFuture(selectedDate)) {
            showError("You can only log health entries for today or earlier days.");
            return;
        }
        getDataManager().requestLogDate(selectedDate);
        navigateTo(AppConstants.PAGE_LOG);
    }

    // =====================================================
    //  One day of the calendar (custom painted)
    // =====================================================

    private static class DayCell extends JComponent {

        private static final Color SELECTED_ON_PERIOD = new Color(0x8E3B62);

        private final int dayNumber;
        private final boolean period;
        private final boolean predicted;
        private final boolean hasEntry;
        private final boolean today;
        private final boolean selected;

        DayCell(int dayNumber, boolean period, boolean predicted, boolean hasEntry,
                boolean today, boolean selected) {
            this.dayNumber = dayNumber;
            this.period = period;
            this.predicted = predicted;
            this.hasEntry = hasEntry;
            this.today = today;
            this.selected = selected;
            setPreferredSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ThemeManager.enableAntialiasing(g2);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            int arc = Math.max(12, size / 2);

            // CalendarMouseListener sets this property in mouseEntered / mouseExited
            boolean hover = Boolean.TRUE.equals(
                    getClientProperty(CalendarMouseListener.HOVER_PROPERTY));

            // 1. Background
            if (period) {
                g2.setColor(ThemeManager.PERIOD_DAY);
            } else if (predicted) {
                g2.setColor(ThemeManager.PREDICTED_DAY);
            } else {
                g2.setColor(new Color(255, 255, 255, 150));
            }
            g2.fillRoundRect(x, y, size, size, arc, arc);

            if (hover) {
                g2.setColor(ThemeManager.withAlpha(ThemeManager.PRIMARY, 110));
                g2.fillRoundRect(x, y, size, size, arc, arc);
            }

            // 2. Border: selected > today > hover
            if (selected) {
                g2.setColor(period ? SELECTED_ON_PERIOD : ThemeManager.ACCENT);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, arc, arc);
            } else if (today) {
                g2.setColor(ThemeManager.TEXT_MUTED);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(x, y, size - 1, size - 1, arc, arc);
            } else if (hover) {
                g2.setColor(ThemeManager.withAlpha(ThemeManager.ACCENT, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, size - 1, size - 1, arc, arc);
            }

            // 3. Day number
            g2.setFont(ThemeManager.FONT_BODY.deriveFont((today || selected) ? Font.BOLD : Font.PLAIN));
            g2.setColor(period ? Color.WHITE : ThemeManager.TEXT_DARK);
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(dayNumber);
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - (hasEntry ? 3 : 0);
            g2.drawString(text, textX, textY);

            // 4. Small dot if the day has a health entry
            if (hasEntry) {
                g2.setColor(period ? Color.WHITE : ThemeManager.ACCENT);
                g2.fillOval(getWidth() / 2 - 2, y + size - 10, 5, 5);
            }
            g2.dispose();
        }
    }
}