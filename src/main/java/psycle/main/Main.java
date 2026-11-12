package psycle.main;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import psycle.gui.AbstractPage;
import psycle.gui.CalendarPage;
import psycle.gui.CyclePhasesPage;
import psycle.gui.DailyLogPage;
import psycle.gui.DashboardPage;
import psycle.gui.InsightsPage;
import psycle.gui.ReportPage;
import psycle.gui.SettingsPage;
import psycle.gui.WelcomePage;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.GradientPanel;
import psycle.interfaces.PageNavigator;
import psycle.model.User;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * The application window. It holds all pages in a CardLayout and shows one at a time.
 * Implements PageNavigator so every page can ask Main to switch pages.
 */
public class Main extends JFrame implements PageNavigator {

    private static final String PAGE_LOADING = "loading";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final DataManager dataManager = new DataManager();
    private final HashMap<String, AbstractPage> pages = new HashMap<>();

    public Main() {
        super(AppConstants.APP_NAME + " - " + AppConstants.TAGLINE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null); // centre on screen

        cards.add(createLoadingPanel(), PAGE_LOADING);
        setContentPane(cards);
        cardLayout.show(cards, PAGE_LOADING);
    }

    /** Program starts here. All Swing work is started on the Event Dispatch Thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyTheme();
            Main window = new Main();
            window.setVisible(true);
            window.startLoading();
        });
    }

    /** Colours and fonts for dialogs (JOptionPane). Must run before any window is created. */
    private static void applyTheme() {
        UIManager.put("Panel.background", ThemeManager.BACKGROUND);
        UIManager.put("OptionPane.background", ThemeManager.BACKGROUND);
        UIManager.put("OptionPane.messageForeground", ThemeManager.TEXT_DARK);
        UIManager.put("OptionPane.messageFont", ThemeManager.FONT_BODY);
        UIManager.put("OptionPane.buttonFont", ThemeManager.FONT_BODY);
    }

    // =====================================================
    //  Start-up (background loading)
    // =====================================================

    /** Loads saved data in a background thread; onDataLoaded() runs when it is finished. */
    private void startLoading() {
        dataManager.loadAllInBackground(this::onDataLoaded);
    }

    /** Runs on the Event Dispatch Thread after loading. */
    private void onDataLoaded() {
        String warning = dataManager.getLoadWarning();
        if (warning != null && !warning.isEmpty()) {
            JOptionPane.showMessageDialog(this, warning, AppConstants.APP_NAME,
                    JOptionPane.WARNING_MESSAGE);
        }

        createPages();

        // Skip the login screen only if a PIN exists and the app lock is switched off.
        // Some first-run states may not have a saved user yet.
        User user = dataManager.getUser();
        if (user != null && user.hasPin() && !user.isLockEnabled()) {
            showPage(AppConstants.PAGE_DASHBOARD);
        } else {
            showPage(AppConstants.PAGE_WELCOME);
        }
    }

    // =====================================================
    //  Pages and navigation
    // =====================================================

    private void createPages() {
        addPage(new WelcomePage(dataManager, this));
        addPage(new DashboardPage(dataManager, this));
        addPage(new CalendarPage(dataManager, this));
        addPage(new DailyLogPage(dataManager, this));
        addPage(new InsightsPage(dataManager, this));
        addPage(new CyclePhasesPage(dataManager, this));
        addPage(new SettingsPage(dataManager, this));
        addPage(new ReportPage(dataManager, this));
    }

    private void addPage(AbstractPage page) {
        pages.put(page.getPageName(), page);
        cards.add(page, page.getPageName());
    }

    /** From the PageNavigator interface: switch to the page with this name. */
    @Override
    public void showPage(String pageName) {
        // Going back to the welcome page ends guest mode and reloads the real saved data
        if (AppConstants.PAGE_WELCOME.equals(pageName) && dataManager.isGuestMode()) {
            try {
                dataManager.endGuestSession();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Your saved data could not be reloaded.\n" + e.getMessage(),
                        AppConstants.APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }

        AbstractPage page = pages.get(pageName);
        if (page == null) {
            JOptionPane.showMessageDialog(this, "This page does not exist: " + pageName,
                    AppConstants.APP_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            page.onShow(); // let the page refresh its data first
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Something went wrong while opening this page.\n" + e.getMessage(),
                    AppConstants.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
        cardLayout.show(cards, pageName);
    }

    // =====================================================
    //  Loading screen
    // =====================================================

    private JPanel createLoadingPanel() {
        GradientPanel background = new GradientPanel();
        background.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel title = new JLabel(AppConstants.APP_NAME);
        title.setFont(ThemeManager.FONT_TITLE.deriveFont(44f));
        title.setForeground(ThemeManager.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel("Loading your data...");
        status.setFont(ThemeManager.FONT_BODY);
        status.setForeground(ThemeManager.TEXT_MUTED);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true); // loading indicator while the background thread works
        bar.setForeground(ThemeManager.ACCENT);
        bar.setBackground(ThemeManager.SECONDARY);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(240, 8));
        bar.setMaximumSize(new Dimension(240, 8));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(status);
        inner.add(Box.createVerticalStrut(16));
        inner.add(bar);

        GlassPanel card = new GlassPanel(28, 34);
        card.add(inner, java.awt.BorderLayout.CENTER);
        background.add(card);
        return background;
    }
}
