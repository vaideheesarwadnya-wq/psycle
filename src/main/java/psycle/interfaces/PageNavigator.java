package psycle.interfaces;

/**
 * Interface for switching between pages.
 * Main implements it using CardLayout; pages only call showPage().
 */
public interface PageNavigator {

    /** Shows the page with the given name (see the PAGE_ constants in AppConstants). */
    void showPage(String pageName);
}
