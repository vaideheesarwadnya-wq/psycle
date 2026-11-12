package psycle.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import psycle.events.CardMouseListener;
import psycle.gui.components.ColorDot;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.InfoCard;
import psycle.gui.components.RoundedButton;
import psycle.interfaces.PageNavigator;
import psycle.model.DailyEntry;
import psycle.service.CycleCalculator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * Multilevel inheritance: AbstractPage -> BasePage -> AuthenticatedPage -> DashboardPage.
 * Shows a summary of the cycle. All predictions are estimates.
 */
public class DashboardPage extends AuthenticatedPage {

    private static final DateTimeFormatter SHORT_DATE =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final int RECENT_COUNT = 5;

    // ---------- Header ----------
    private final JLabel greetingLabel =
            createLabel("", ThemeManager.FONT_TITLE, ThemeManager.TEXT_DARK);
    private final JLabel dateLabel =
            createLabel("", ThemeManager.FONT_BODY, ThemeManager.TEXT_MUTED);

    // ---------- Information cards ----------
    private final InfoCard nextPeriodCard = new InfoCard("Next Period");
    private final InfoCard cycleDayCard = new InfoCard("Current Cycle Day");
    private final InfoCard phaseCard = new InfoCard("Cycle Phase");
    private final InfoCard averageCard = new InfoCard("Average Cycle Length");

    // ---------- Countdown banner ----------
    private final JLabel countdownLabel =
            createLabel("", ThemeManager.FONT_HEADING.deriveFont(22f), ThemeManager.ACCENT);
    private final JLabel noteLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    // ---------- Quick log and overview ----------
    private final HashMap<String, GlassPanel> moodChips = new HashMap<>();
    private final CycleBar cycleBar = new CycleBar();
    private final JLabel overviewLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    // ---------- Recent entries ----------
    private final JPanel recentPanel = createVerticalPanel();

    public DashboardPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_DASHBOARD, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page (called once)
    // =====================================================

    @Override
    protected void initializePage() {
        getContentPanel().add(buildHeader(), BorderLayout.NORTH);

        JPanel column = createVerticalPanel();
        column.add(buildCardRow());
        column.add(createGap(8));
        column.add(buildCountdownCard());
        column.add(createGap(8));
        column.add(buildMiddleRow());
        column.add(createGap(8));
        column.add(buildRecentCard());
        column.add(createGap(8));
        column.add(alignLeft(createLabel(
                "All predictions are estimates based on your logged cycles. "
                + "They are not medical advice.",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        column.add(createGap(6));

        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = createTransparentPanel(new BorderLayout(12, 0));

        JPanel texts = createVerticalPanel();
        texts.add(alignLeft(greetingLabel));
        texts.add(alignLeft(dateLabel));
        header.add(texts, BorderLayout.WEST);

        // Quick Log button (ActionListener) opens the Daily Log page
        RoundedButton quickLogButton = new RoundedButton("Quick Log");
        quickLogButton.addActionListener(e -> navigateTo(AppConstants.PAGE_LOG));
        JPanel buttonHolder = createTransparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonHolder.add(quickLogButton);
        header.add(buttonHolder, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCardRow() {
        nextPeriodCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(28f));
        cycleDayCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(30f));
        phaseCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(24f));
        averageCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(28f));

        JPanel row = createTransparentPanel(new GridLayout(1, 4, 0, 0));
        row.add(nextPeriodCard);
        row.add(cycleDayCard);
        row.add(phaseCard);
        row.add(averageCard);
        return alignLeft(row);
    }

    private GlassPanel buildCountdownCard() {
        GlassPanel card = new GlassPanel(28, 18);
        card.setFillColor(ThemeManager.withAlpha(ThemeManager.SECONDARY, 210));

        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(countdownLabel));
        inner.add(createGap(4));
        inner.add(alignLeft(noteLabel));
        card.add(inner, BorderLayout.CENTER);
        return alignLeft(card);
    }

    private JPanel buildMiddleRow() {
        JPanel row = createTransparentPanel(new GridLayout(1, 2, 0, 0));
        row.add(buildQuickLogCard());
        row.add(buildOverviewCard());
        return alignLeft(row);
    }

    private GlassPanel buildQuickLogCard() {
        GlassPanel card = new GlassPanel(28, 18);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("Quick Log",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(createLabel("How are you feeling today?",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(8));

        JPanel chips = createTransparentPanel(new GridLayout(2, 3, 0, 0));
        for (String mood : AppConstants.MOOD_OPTIONS) {
            chips.add(createMoodChip(mood));
        }
        inner.add(alignLeft(chips));
        inner.add(createGap(10));

        RoundedButton periodButton = new RoundedButton("Period started today", false);
        periodButton.addActionListener(e -> handlePeriodStartedToday());
        inner.add(alignLeft(periodButton));

        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    /** A clickable mood card. MouseListener (CardMouseListener) handles hover and click. */
    private GlassPanel createMoodChip(String mood) {
        GlassPanel chip = new GlassPanel(16, 4);
        JLabel label = new JLabel(mood, SwingConstants.CENTER);
        label.setFont(ThemeManager.FONT_SMALL);
        label.setForeground(ThemeManager.TEXT_DARK);
        chip.add(label, BorderLayout.CENTER);

        chip.addMouseListener(new CardMouseListener(chip, () -> quickLogMood(mood)));
        moodChips.put(mood, chip);
        return chip;
    }

    private GlassPanel buildOverviewCard() {
        GlassPanel card = new GlassPanel(28, 18);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("Cycle Overview",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(createGap(4));
        inner.add(alignLeft(overviewLabel));
        inner.add(createGap(10));
        inner.add(alignLeft(cycleBar));
        inner.add(createGap(8));

        JPanel legend = createTransparentPanel(new GridLayout(2, 2, 0, 0));
        legend.add(ColorDot.legendItem(ThemeManager.MENSTRUAL_COLOR, "Menstrual"));
        legend.add(ColorDot.legendItem(ThemeManager.FOLLICULAR_COLOR, "Follicular"));
        legend.add(ColorDot.legendItem(ThemeManager.OVULATORY_COLOR, "Ovulatory"));
        legend.add(ColorDot.legendItem(ThemeManager.LUTEAL_COLOR, "Luteal"));
        inner.add(alignLeft(legend));

        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    private GlassPanel buildRecentCard() {
        GlassPanel card = new GlassPanel(28, 18);
        JPanel inner = createVerticalPanel();

        inner.add(alignLeft(createLabel("Recent Entries",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(createLabel("Click an entry to view or edit that day.",
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        inner.add(createGap(8));
        inner.add(alignLeft(recentPanel));

        card.add(inner, BorderLayout.NORTH);
        return alignLeft(card);
    }

    // =====================================================
    //  Refreshing the data (called every time the page is shown)
    // =====================================================

    @Override
    protected void refreshContent() {
        String name = getDataManager().getUser().getName();
        greetingLabel.setText(name.isEmpty() ? "Hello, Welcome to Psycle!"
                : "Hello, " + name + "!");
        dateLabel.setText("Today is " + DateUtils.formatNice(DateUtils.today()) + ".");

        updateCards();
        updateOverview();
        updateMoodChips();
        updateRecentEntries();
    }

    private void updateCards() {
        CycleCalculator calc = getDataManager().getCalculator();

        if (!calc.hasData()) {
            // Nothing recorded yet: show friendly "start tracking" messages
            nextPeriodCard.setValue("-");
            nextPeriodCard.setSubtitle("Start tracking");
            cycleDayCard.setValue("-");
            cycleDayCard.setSubtitle("Log your period");
            phaseCard.setValue("-");
            phaseCard.setSubtitle("No data yet");
            phaseCard.setFillColor(ThemeManager.GLASS_FILL);
            averageCard.setValue(AppConstants.DEFAULT_CYCLE_LENGTH + " days");
            averageCard.setSubtitle("default value");

            countdownLabel.setText("Start tracking your cycle.");
            noteLabel.setText(wrapHtml("Mark the first day of your period on the Calendar "
                    + "to get your first estimate.", 560));
            return;
        }

        LocalDate next = calc.calculateNextPeriod();
        int daysUntil = calc.getDaysUntilNextPeriod();
        int averageLength = calc.getAverageCycleLength();
        int recorded = calc.getCycleLengths().size();
        String phase = calc.getCurrentPhase();

        nextPeriodCard.setValue(next.format(SHORT_DATE));
        nextPeriodCard.setSubtitle(describeDays(daysUntil));
        cycleDayCard.setValue("Day " + calc.getCurrentCycleDay());
        cycleDayCard.setSubtitle("of about " + averageLength);
        phaseCard.setValue(phase);
        phaseCard.setSubtitle("Estimate");
        phaseCard.setFillColor(ThemeManager.withAlpha(ThemeManager.getPhaseColor(phase), 190));
        averageCard.setValue(averageLength + " days");
        averageCard.setSubtitle(recorded == 0 ? "default estimate"
                : "from " + recorded + (recorded == 1 ? " cycle" : " cycles"));

        countdownLabel.setText(countdownMessage(daysUntil));
        noteLabel.setText(wrapHtml(calc.getPredictionNote(), 560));
    }

    private String describeDays(int days) {
        if (days > 1) {
            return "in " + days + " days";
        } else if (days == 1) {
            return "tomorrow";
        } else if (days == 0) {
            return "today";
        } else if (days == -1) {
            return "1 day late";
        }
        return (-days) + " days late";
    }

    private String countdownMessage(int days) {
        if (days > 1) {
            return "Your next period is expected in " + days + " days.";
        } else if (days == 1) {
            return "Your next period is expected tomorrow.";
        } else if (days == 0) {
            return "Your next period is expected today.";
        }
        int late = -days;
        return "Your period is " + late + (late == 1 ? " day" : " days")
                + " later than predicted.";
    }

    private void updateOverview() {
        CycleCalculator calc = getDataManager().getCalculator();
        if (!calc.hasData()) {
            cycleBar.update(AppConstants.DEFAULT_CYCLE_LENGTH,
                    AppConstants.DEFAULT_PERIOD_LENGTH, 0);
            overviewLabel.setText(wrapHtml(
                    "Log your first period to see where you are in your cycle.", 320));
            return;
        }
        int length = calc.getAverageCycleLength();
        int day = calc.getCurrentCycleDay();
        cycleBar.update(length, calc.getAveragePeriodLength(), day);
        overviewLabel.setText(wrapHtml("Day " + day + " of about " + length + ". "
                + calc.getCurrentPhase() + " phase (estimate).", 320));
    }

    /** Highlights the mood card that matches today's saved entry. */
    private void updateMoodChips() {
        DailyEntry today = getDataManager().getEntryForDate(DateUtils.format(DateUtils.today()));
        String todayMood = (today == null) ? "" : today.getMood();

        Iterator<Map.Entry<String, GlassPanel>> it = moodChips.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, GlassPanel> pair = it.next();
            pair.getValue().setSelected(pair.getKey().equals(todayMood));
        }
    }

    private void updateRecentEntries() {
        recentPanel.removeAll();
        ArrayList<DailyEntry> recent = getDataManager().getRecentEntries(RECENT_COUNT);

        if (recent.isEmpty()) {
            recentPanel.add(alignLeft(createLabel(
                    "No entries yet. Press Quick Log to add your first one.",
                    ThemeManager.FONT_BODY, ThemeManager.TEXT_MUTED)));
        } else {
            Iterator<DailyEntry> it = recent.iterator();
            while (it.hasNext()) {
                recentPanel.add(alignLeft(createEntryRow(it.next())));
            }
        }
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    /** One clickable row for a saved entry. */
    private GlassPanel createEntryRow(DailyEntry entry) {
        LocalDate date = DateUtils.parse(entry.getDate());

        GlassPanel row = new GlassPanel(18, 4);
        row.setLayout(new BorderLayout(14, 0));

        JLabel dayLabel = createLabel(DateUtils.formatNice(date),
                ThemeManager.FONT_BODY.deriveFont(Font.BOLD), ThemeManager.ACCENT);
        dayLabel.setPreferredSize(new Dimension(110, dayLabel.getPreferredSize().height));
        JLabel summary = createLabel(summarize(entry),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK);

        row.add(dayLabel, BorderLayout.WEST);
        row.add(summary, BorderLayout.CENTER);

        // MouseListener: clicking the row opens that day in the Daily Log
        row.addMouseListener(new CardMouseListener(row, () -> openDay(date)));
        return row;
    }

    private String summarize(DailyEntry entry) {
        String text = "Mood: " + entry.getMood() + " | Flow: " + entry.getFlow()
                + " | Pain: " + entry.getPainLevel() + "/5";
        if (!entry.getSymptomsAsText().isEmpty()) {
            text += " | " + entry.getSymptomsAsText().replace(";", ", ");
        }
        if (text.length() > 85) {
            text = text.substring(0, 82) + "...";
        }
        return text;
    }

    // =====================================================
    //  Actions
    // =====================================================

    private void openDay(LocalDate date) {
        getDataManager().requestLogDate(date);
        navigateTo(AppConstants.PAGE_LOG);
    }

    /** Saves the clicked mood into today's entry (creating the entry if needed). */
    private void quickLogMood(String mood) {
        try {
            String today = DateUtils.format(DateUtils.today());
            DailyEntry entry = getDataManager().getEntryForDate(today);
            if (entry == null) {
                entry = new DailyEntry(today);
            }
            entry.setMood(mood);
            getDataManager().saveEntry(entry);
            updateMoodChips();
            updateRecentEntries();
        } catch (IOException e) {
            showError("Your mood could not be saved.\n" + e.getMessage());
        }
    }

    private void handlePeriodStartedToday() {
        try {
            getDataManager().addPeriodStart(DateUtils.today());
            refreshContent();
            showInfo("Your period start was saved for today.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Your period could not be saved.\n" + e.getMessage());
        }
    }

    // =====================================================
    //  Cycle bar: a custom-painted bar showing the four phases
    // =====================================================

    private static class CycleBar extends JComponent {

        private int cycleLength = AppConstants.DEFAULT_CYCLE_LENGTH;
        private int periodLength = AppConstants.DEFAULT_PERIOD_LENGTH;
        private int currentDay = 0; // 0 = unknown

        CycleBar() {
            setPreferredSize(new Dimension(300, 46));
            setMinimumSize(new Dimension(100, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        }

        void update(int cycleLength, int periodLength, int currentDay) {
            this.cycleLength = cycleLength;
            this.periodLength = periodLength;
            this.currentDay = currentDay;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ThemeManager.enableAntialiasing(g2);

            int w = getWidth();
            int barY = 12;
            int barH = 16;

            // 1. Colour every cycle day by its phase, clipped to a rounded bar
            Shape oldClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(0, barY, w, barH, barH, barH));
            for (int day = 1; day <= cycleLength; day++) {
                int x1 = (day - 1) * w / cycleLength;
                int x2 = day * w / cycleLength;
                String phase = CycleCalculator.phaseForDay(day, cycleLength, periodLength);
                g2.setColor(ThemeManager.getPhaseColor(phase));
                g2.fillRect(x1, barY, x2 - x1 + 1, barH);
            }
            g2.setClip(oldClip);

            // 2. Marker for the current day
            if (currentDay >= 1) {
                int day = Math.min(currentDay, cycleLength);
                int cx = (int) ((day - 0.5) * w / cycleLength);
                int cy = barY + barH / 2;

                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 9, cy - 9, 18, 18);
                g2.setColor(ThemeManager.ACCENT);
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.drawOval(cx - 9, cy - 9, 18, 18);

                // 3. "Day N" text under the marker
                g2.setFont(ThemeManager.FONT_SMALL);
                g2.setColor(ThemeManager.TEXT_DARK);
                FontMetrics fm = g2.getFontMetrics();
                String text = "Day " + currentDay;
                int textX = Math.max(0, Math.min(cx - fm.stringWidth(text) / 2,
                        w - fm.stringWidth(text)));
                g2.drawString(text, textX, 42);
            }
            g2.dispose();
        }
    }
}