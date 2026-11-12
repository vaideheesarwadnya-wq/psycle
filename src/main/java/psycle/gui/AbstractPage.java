package psycle.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import psycle.interfaces.PageNavigator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;

/**
 * Abstract class: the common parent of every page.
 * It cannot be created directly because initializePage() has no body here.
 * Each concrete page must write its own initializePage().
 */
public abstract class AbstractPage extends JPanel {

    private final String pageName;
    private final DataManager dataManager;
    private final PageNavigator navigator;

    public AbstractPage(String pageName, DataManager dataManager, PageNavigator navigator) {
        this.pageName = pageName;
        this.dataManager = dataManager;
        this.navigator = navigator;
    }

    /** Builds the components of the page. Called once, at the end of the page constructor. */
    protected abstract void initializePage();

    /** Called every time the page is shown. Pages override it to reload their data. */
    public void onShow() {
        // Default: nothing to refresh.
    }

    public String getPageName() {
        return pageName;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    /** Switches to another page. */
    protected void navigateTo(String targetPage) {
        navigator.showPage(targetPage);
    }

    // ---------- Friendly dialogs (used instead of letting the app crash) ----------

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, AppConstants.APP_NAME,
                JOptionPane.ERROR_MESSAGE);
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, AppConstants.APP_NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Asks a Yes/No question. Returns true if the user clicked Yes. */
    protected boolean confirm(String message, String title) {
        int choice = JOptionPane.showConfirmDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }
}