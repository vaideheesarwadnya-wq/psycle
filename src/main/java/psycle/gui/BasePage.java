package psycle.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import psycle.gui.components.GradientPanel;
import psycle.interfaces.PageNavigator;
import psycle.storage.DataManager;
import psycle.utils.DateUtils;
import psycle.utils.ThemeManager;

/**
 * Inheritance level 2: AbstractPage -> BasePage.
 * Adds the shared background and common layout helper methods.
 */
public abstract class BasePage extends AbstractPage {

    public BasePage(String pageName, DataManager dataManager, PageNavigator navigator) {
        super(pageName, dataManager, navigator);
        setOpaque(true);
        setLayout(new BorderLayout());
    }

    /** Paints the pink gradient behind everything on the page. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        GradientPanel.paintBackground(g2, getWidth(), getHeight(),
                ThemeManager.GRADIENT_TOP, ThemeManager.GRADIENT_BOTTOM);
        g2.dispose();
    }

    // ---------- Layout helpers shared by all pages ----------

    protected JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    protected JLabel createTitleLabel(String text) {
        return createLabel(text, ThemeManager.FONT_TITLE, ThemeManager.TEXT_DARK);
    }

    /** A panel with no background, so the gradient shows through. */
    protected JPanel createTransparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    /** A transparent panel that stacks its children from top to bottom. */
    protected JPanel createVerticalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    /** Big title with a muted subtitle under it. */
    protected JPanel createHeader(String title, String subtitle) {
        JPanel header = createTransparentPanel(new BorderLayout(0, 4));
        header.add(createTitleLabel(title), BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            header.add(createLabel(subtitle, ThemeManager.FONT_BODY, ThemeManager.TEXT_MUTED),
                    BorderLayout.CENTER);
        }
        return header;
    }

    /** Empty vertical space for BoxLayout columns (aligned left like everything else). */
    protected JComponent createGap(int height) {
        JPanel gap = new JPanel();
        gap.setOpaque(false);
        gap.setPreferredSize(new Dimension(0, height));
        gap.setMinimumSize(new Dimension(0, height));
        gap.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        gap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return gap;
    }

    /** Aligns a component to the left inside a vertical BoxLayout, and returns it. */
    protected <T extends JComponent> T alignLeft(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    /** Aligns left and makes the component as wide as its parent (good for buttons). */
    protected void stretchWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    /**
     * Puts a vertical column of cards into a transparent scroll pane.
     * The column keeps its natural height; the page scrolls if the window is too small.
     */
    protected JScrollPane createScrollPane(JComponent column) {
        ScrollableHolder holder = new ScrollableHolder();
        holder.add(column, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(holder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // SIMPLE mode repaints the gradient while scrolling (avoids ghost images)
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    /** Turns plain text into a wrapped HTML label text (special characters are escaped). */
    protected static String wrapHtml(String text, int widthPixels) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>");
        return "<html><div style='width:" + widthPixels + "px'>" + safe + "</div></html>";
    }

    /** Example: "Tuesday, 10 Nov 2026". */
    protected static String longDate(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + ", " + DateUtils.formatNice(date);
    }

    /** Panel used inside the scroll pane: it always matches the width of the window. */
    private static class ScrollableHolder extends JPanel implements Scrollable {

        ScrollableHolder() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
                                              int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation,
                                               int direction) {
            return Math.max(visibleRect.height - 40, 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // no sideways scrolling
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}