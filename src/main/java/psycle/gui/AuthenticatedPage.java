package psycle.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import psycle.interfaces.PageNavigator;
import psycle.storage.DataManager;

/**
 * Inheritance level 3: AbstractPage -> BasePage -> AuthenticatedPage.
 * Every page shown after login extends this class and gets the sidebar for free.
 */
public abstract class AuthenticatedPage extends BasePage {

    private final NavigationPanel navigationPanel;
    private final JPanel contentPanel;

    public AuthenticatedPage(String pageName, DataManager dataManager, PageNavigator navigator) {
        super(pageName, dataManager, navigator);

        // Sidebar on the left
        navigationPanel = new NavigationPanel(dataManager, navigator, pageName);
        JPanel sidebarHolder = createTransparentPanel(new BorderLayout());
        sidebarHolder.setBorder(new EmptyBorder(18, 18, 18, 6));
        sidebarHolder.add(navigationPanel, BorderLayout.CENTER);
        add(sidebarHolder, BorderLayout.WEST);

        // Content area on the right (child pages add their components here)
        contentPanel = createTransparentPanel(new BorderLayout(0, 14));
        contentPanel.setBorder(new EmptyBorder(26, 20, 20, 28));
        add(contentPanel, BorderLayout.CENTER);
    }

    /** The area where a page puts its own components. */
    protected JPanel getContentPanel() {
        return contentPanel;
    }

    /** Puts a title and subtitle at the top of the content area. */
    protected void setPageHeader(String title, String subtitle) {
        contentPanel.add(createHeader(title, subtitle), BorderLayout.NORTH);
    }

    /** Template method: refresh the sidebar, then let the page reload its own data. */
        /** Lets a page refresh its own sidebar (for example after reminder settings change). */
    protected void refreshSidebar() {
        navigationPanel.refresh();
    }
    @Override
    public final void onShow() {
        navigationPanel.refresh();
        refreshContent();
    }

    /** Pages override this to reload their data each time they are shown. */
    protected void refreshContent() {
        // Default: nothing to refresh.
    }
}