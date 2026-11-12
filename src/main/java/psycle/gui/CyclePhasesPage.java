package psycle.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import psycle.events.CardMouseListener;
import psycle.gui.components.GlassPanel;
import psycle.interfaces.PageNavigator;
import psycle.service.CycleCalculator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * Explains the four menstrual cycle phases. Everything here is an estimate.
 */
public class CyclePhasesPage extends AuthenticatedPage {

    private static final int TEXT_WIDTH = 270;

    private static final String[] PHASES = {
        AppConstants.PHASE_MENSTRUAL, AppConstants.PHASE_FOLLICULAR,
        AppConstants.PHASE_OVULATORY, AppConstants.PHASE_LUTEAL
    };

    private static final String[] EXPLANATIONS = {
        "Bleeding happens as the body sheds the uterine lining. Estrogen and progesterone are "
                + "at their lowest, and a period usually lasts about 3 to 7 days.",
        "The brain signals the ovaries to grow follicles, each holding an egg. Estrogen slowly "
                + "rises and the uterine lining rebuilds, so energy and mood often lift.",
        "A surge in luteinizing hormone triggers an egg to be released. This usually happens "
                + "about 14 days before the next period, and the egg survives only 12 to 24 hours.",
        "After ovulation, progesterone rises to prepare the body for a possible pregnancy. "
                + "If there is none, hormone levels drop and the next period begins."
    };

    private static final String[] EXPERIENCES = {
        "Cramps, fatigue, lower back pain, headaches, low mood.",
        "Rising energy, clearer skin, better focus, improved mood.",
        "Mild one-sided pelvic twinge, higher energy, clearer cervical mucus.",
        "Bloating, breast tenderness, mood changes, tiredness, cravings, acne."
    };

    private final GlassPanel[] cards = new GlassPanel[4];
    private final JLabel[] rangeLabels = new JLabel[4];
    private final JLabel[] badgeLabels = new JLabel[4];
    private final JLabel summaryLabel =
            createLabel("", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

    public CyclePhasesPage(DataManager dataManager, PageNavigator navigator) {
        super(AppConstants.PAGE_PHASES, dataManager, navigator);
        initializePage();
    }

    @Override
    protected void initializePage() {
        setPageHeader("Cycle Phases",
                "A simple guide to the four phases. Every body and every cycle is different.");

        JPanel grid = createTransparentPanel(new GridLayout(2, 2, 0, 0));
        for (int i = 0; i < PHASES.length; i++) {
            cards[i] = buildPhaseCard(i);
            grid.add(cards[i]);
        }

        JPanel column = createVerticalPanel();
        column.add(alignLeft(summaryLabel));
        column.add(createGap(8));
        column.add(alignLeft(grid));
        column.add(createGap(8));
        column.add(alignLeft(createLabel("Phase days are estimates based on your logged cycles, "
                + "not medically confirmed results. See a doctor if your cycles are very painful "
                + "or very irregular.", ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED)));
        column.add(createGap(6));

        getContentPanel().add(createScrollPane(column), BorderLayout.CENTER);
    }

    /** One card for one phase, in its own shade of pink. */
    private GlassPanel buildPhaseCard(int i) {
        GlassPanel card = new GlassPanel(28, 16);
        card.setFillColor(ThemeManager.withAlpha(ThemeManager.getPhaseColor(PHASES[i]), 205));

        rangeLabels[i] = createLabel("", ThemeManager.FONT_BODY.deriveFont(Font.BOLD),
                ThemeManager.ACCENT);
        badgeLabels[i] = createLabel("You are probably here (estimate)",
                ThemeManager.FONT_SMALL.deriveFont(Font.BOLD), ThemeManager.TEXT_DARK);
        badgeLabels[i].setVisible(false);

        JPanel inner = createVerticalPanel();
        inner.add(alignLeft(createLabel(PHASES[i] + " Phase",
                ThemeManager.FONT_HEADING, ThemeManager.TEXT_DARK)));
        inner.add(alignLeft(rangeLabels[i]));
        inner.add(alignLeft(badgeLabels[i]));
        inner.add(createGap(8));
        inner.add(alignLeft(createLabel(wrapHtml(EXPLANATIONS[i], TEXT_WIDTH),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK)));
        inner.add(createGap(8));
        inner.add(alignLeft(createLabel("Common experiences",
                ThemeManager.FONT_SMALL.deriveFont(Font.BOLD), ThemeManager.TEXT_MUTED)));
        inner.add(alignLeft(createLabel(wrapHtml(EXPERIENCES[i], TEXT_WIDTH),
                ThemeManager.FONT_SMALL, ThemeManager.TEXT_DARK)));

        card.add(inner, BorderLayout.NORTH);

        // MouseListener: hover effect on every phase card
        card.addMouseListener(new CardMouseListener(card));
        return card;
    }

    /** Recalculates the day ranges and the highlighted phase. */
    @Override
    protected void refreshContent() {
        CycleCalculator calc = getDataManager().getCalculator();
        int cycleLength = calc.getAverageCycleLength();
        int periodLength = calc.getAveragePeriodLength();
        String current = calc.getCurrentPhase();

        for (int i = 0; i < PHASES.length; i++) {
            rangeLabels[i].setText(CycleCalculator.phaseDayRange(PHASES[i], cycleLength, periodLength));
            boolean isCurrent = PHASES[i].equals(current);
            badgeLabels[i].setVisible(isCurrent);
            cards[i].setSelected(isCurrent);
        }

        if (!calc.hasData()) {
            summaryLabel.setText(wrapHtml("Showing a typical " + cycleLength + "-day cycle. "
                    + "Log your period on the Calendar to personalise this page.", 650));
        } else {
            summaryLabel.setText(wrapHtml("Based on your average cycle of " + cycleLength
                    + " days and period of " + periodLength + " days. You are on cycle day "
                    + calc.getCurrentCycleDay() + ", probably in the " + current
                    + " phase (estimate).", 650));
        }
    }
}