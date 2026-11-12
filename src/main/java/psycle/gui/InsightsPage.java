package psycle.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import psycle.gui.components.BarChart;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.InfoCard;
import psycle.interfaces.PageNavigator;
import psycle.model.Cycle;
import psycle.service.CycleCalculator;
import psycle.service.InsightsService;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * Insights and analytics: simple statistics and Java2D charts.
 */
public class InsightsPage extends AuthenticatedPage {

    private final InfoCard avgCycleCard = new InfoCard("Average Cycle Length");
    private final InfoCard avgPeriodCard = new InfoCard("Average Period Duration");
    private final InfoCard periodsCard = new InfoCard("Periods Recorded");
    private final InfoCard topSymptomCard = new InfoCard("Most Logged Symptom");

    private final BarChart cycleLengthChart = new BarChart(false, ThemeManager.ACCENT);
    private final BarChart symptomChart = new BarChart(true, ThemeManager.ACCENT);
    private final BarChart moodChart = new BarChart(true, ThemeManager.PRIMARY);
    private final BarChart flowChart = new BarChart(false, ThemeManager.PRIMARY);
    private final BarChart trendChart = new BarChart(false, ThemeManager.ACCENT);

    private final JLabel noteLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    public InsightsPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_INSIGHTS, dataManager, navigator);
        initializePage();
    }

    // =====================================================
    //  Building the page
    // =====================================================

    @Override
    protected void initializePage() {
        setPageHeader("Insights & Analytics",
                "Simple statistics from your own entries. They are not medical predictions.");

        avgCycleCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(28f));
        avgPeriodCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(28f));
        periodsCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(28f));
        topSymptomCard.setValueFont(ThemeManager.FONT_BIG_NUMBER.deriveFont(22f));

        cycleLengthChart.setEmptyMessage("Record at least two periods.");
        symptomChart.setEmptyMessage("No symptoms logged yet.");
        moodChart.setEmptyMessage("No moods logged yet.");
        flowChart.setEmptyMessage("No flow logged yet.");
        trendChart.setEmptyMessage("Log symptoms and a period to see this chart.");

        JPanel cardRow = createTransparentPanel(new GridLayout(1, 4, 0, 0));
        cardRow.add(avgCycleCard);
        cardRow.add(avgPeriodCard);
        cardRow.add(periodsCard);
        cardRow.add(topSymptomCard);

        JPanel row1 = createTransparentPanel(new GridLayout(1, 2, 0, 0));
        row1.add(createChartCard("Previous cycle lengths",
                "Days between period starts (latest 12).", cycleLengthChart));
        row1.add(createChartCard("Most frequent symptoms",
                "How often each symptom was logged.", symptomChart));

        JPanel row2 = createTransparentPanel(new GridLayout(1, 2, 0, 0));
        row2.add(createChartCard("Mood trends", "Days logged with each mood.", moodChart));
        row2.add(createChartCard("Flow patterns", "Days logged with each flow level.", flowChart));

        JPanel column = createVerticalPanel();
        column.add(alignLeft(cardRow));
        column.add(createGap(8));
        column.add(alignLeft(row1));
        column.add(createGap(8));
        column.add(alignLeft(row2));
        column.add(createGap(8));
        column.add(alignLeft(createChartCard("Symptom trend by cycle day",
                "Total symptoms logged on each day of your cycle (day 1 = first day of a period).",
                trendChart)));
        column.add(createGap(8));
        column.add(alignLeft(noteLabel));
        column.add(createGap(6));

        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);
    }

    private GlassPanel createChartCard(String title, String subtitle, BarChart chart) {
        GlassPanel card = new GlassPanel(28, 16);
        JPanel head = createVerticalPanel();
        head.add(alignLeft(createLabel(title, ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        head.add(alignLeft(createLabel(subtitle, ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        head.add(createGap(6));
        card.add(head, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // =====================================================
    //  Refreshing the numbers (called every time the page is shown)
    // =====================================================

    @Override
    protected void refreshContent() {
        DataManager dm = getDataManager();
        CycleCalculator calc = dm.getCalculator();
        InsightsService insights = dm.getInsights();

        updateCards(dm, insights);
        updateCycleLengthChart(insights);
        updateSymptomChart(insights);
        updateMoodChart(insights);
        updateFlowChart(insights);
        updateTrendChart(insights, calc);

        noteLabel.setText(wrapHtml((calc.hasData() ? calc.getPredictionNote() + " " : "")
                + "Insights are simple counts from your own logs, not medical advice.", 650));
    }

    private void updateCards(DataManager dm, InsightsService insights) {
        ArrayList<Integer> lengths = insights.getCycleLengths();
        if (lengths.isEmpty()) {
            avgCycleCard.setValue(AppConstants.DEFAULT_CYCLE_LENGTH + " days");
            avgCycleCard.setSubtitle("default (log 2+ periods)");
        } else {
            avgCycleCard.setValue(insights.getAverageCycleLength() + " days");
            avgCycleCard.setSubtitle("from " + lengths.size()
                    + (lengths.size() == 1 ? " cycle" : " cycles"));
        }

        // Average period duration: only periods that have an end date
        int total = 0;
        int count = 0;
        Iterator<Cycle> it = dm.getCycles().iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            if (!c.isOngoing()) {
                total += c.getPeriodLength();
                count++;
            }
        }
        if (count == 0) {
            avgPeriodCard.setValue("-");
            avgPeriodCard.setSubtitle("mark period end dates");
        } else {
            avgPeriodCard.setValue(Math.round((float) total / count) + " days");
            avgPeriodCard.setSubtitle("from " + count + (count == 1 ? " period" : " periods"));
        }

        periodsCard.setValue(String.valueOf(dm.getCycles().size()));
        periodsCard.setSubtitle(insights.getEntryCount() + " daily entries");

        HashMap<String, Integer> symptoms = insights.getSymptomFrequency();
        String top = insights.getMostCommon(symptoms);
        topSymptomCard.setValue(top);
        topSymptomCard.setSubtitle(symptoms.isEmpty() ? "nothing logged yet"
                : symptoms.get(top) + (symptoms.get(top) == 1 ? " time" : " times"));
    }

    private void updateCycleLengthChart(InsightsService insights) {
        ArrayList<Integer> lengths = insights.getCycleLengths();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (int i = Math.max(0, lengths.size() - 12); i < lengths.size(); i++) {
            labels.add("C" + (i + 1));
            values.add(lengths.get(i));
        }
        cycleLengthChart.setData(labels, values);
    }

    private void updateSymptomChart(InsightsService insights) {
        ArrayList<Map.Entry<String, Integer>> sorted =
                insights.sortByCount(insights.getSymptomFrequency());
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (int i = 0; i < sorted.size() && i < 6; i++) {
            labels.add(sorted.get(i).getKey());
            values.add(sorted.get(i).getValue());
        }
        symptomChart.setData(labels, values);
    }

    private void updateMoodChart(InsightsService insights) {
        HashMap<String, Integer> moods = insights.getMoodFrequency();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (String mood : AppConstants.MOOD_OPTIONS) {
            labels.add(mood);
            values.add(moods.containsKey(mood) ? moods.get(mood) : 0);
        }
        moodChart.setData(labels, values);
    }

    private void updateFlowChart(InsightsService insights) {
        HashMap<String, Integer> flows = insights.getFlowFrequency();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (int i = 1; i < AppConstants.FLOW_OPTIONS.length; i++) { // skip "None"
            String flow = AppConstants.FLOW_OPTIONS[i];
            labels.add(flow);
            values.add(flows.containsKey(flow) ? flows.get(flow) : 0);
        }
        flowChart.setData(labels, values);
    }

    private void updateTrendChart(InsightsService insights, CycleCalculator calc) {
        HashMap<Integer, Integer> byDay = insights.getSymptomCountByCycleDay();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();

        if (!byDay.isEmpty()) {
            int lastDay = calc.getAverageCycleLength();
            Iterator<Integer> it = byDay.keySet().iterator();
            while (it.hasNext()) {
                lastDay = Math.max(lastDay, it.next());
            }
            lastDay = Math.min(lastDay, AppConstants.MAX_CYCLE_LENGTH);

            for (int day = 1; day <= lastDay; day++) {
                labels.add(String.valueOf(day));
                values.add(byDay.containsKey(day) ? byDay.get(day) : 0);
            }
        }
        trendChart.setData(labels, values);
    }
}