package psycle.gui;
import psycle.service.ReminderService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import psycle.gui.components.GlassPanel;
import psycle.gui.components.RoundedButton;
import psycle.interfaces.PageNavigator;
import psycle.storage.DataManager;
import psycle.utils.AppConstants;
import psycle.utils.ThemeManager;

/**
 * Sidebar navigation shown on every page after login.
 */
public class NavigationPanel extends GlassPanel {

    // {page name, button text}
    private static final String[][] MENU_ITEMS = {
        {AppConstants.PAGE_DASHBOARD, "Dashboard"},
        {AppConstants.PAGE_CALENDAR, "Calendar"},
        {AppConstants.PAGE_LOG, "Daily Log"},
        {AppConstants.PAGE_INSIGHTS, "Insights"},
        {AppConstants.PAGE_PHASES, "Cycle Phases"},
        {AppConstants.PAGE_REPORT, "PDF Report"},
        {AppConstants.PAGE_SETTINGS, "Settings & Privacy"}
    };

    private final DataManager dataManager;
        
    private final JLabel statusLabel = new JLabel();
    private final JLabel reminderLabel = new JLabel();
    private final RoundedButton logoutButton = new RoundedButton("Log out", false);

    public NavigationPanel(DataManager dataManager, PageNavigator navigator, String activePage) {
        super(28, 14);
        this.dataManager = dataManager;
        setPreferredSize(new Dimension(236, 0));
        setLayout(new BorderLayout(0, 12));

        add(createBrand(), BorderLayout.NORTH);
        add(createMenu(navigator, activePage), BorderLayout.CENTER);
        add(createBottom(navigator), BorderLayout.SOUTH);
        refresh();
                String reminder = ReminderService.getReminderText(dataManager);
        reminderLabel.setText(reminder.isEmpty() ? ""
                : "<html><div style='width:170px'>" + reminder + "</div></html>");
        reminderLabel.setVisible(!reminder.isEmpty());
    }

    private JPanel createBrand() {
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(6, 8, 10, 0));

        JLabel name = new JLabel(AppConstants.APP_NAME);
        name.setFont(ThemeManager.FONT_TITLE);
        name.setForeground(ThemeManager.ACCENT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel(AppConstants.TAGLINE);
        tagline.setFont(ThemeManager.FONT_SMALL);
        tagline.setForeground(ThemeManager.TEXT_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(name);
        brand.add(tagline);
        return brand;
    }

    private JPanel createMenu(PageNavigator navigator, String activePage) {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);

        for (String[] item : MENU_ITEMS) {
            String pageName = item[0];
            NavButton button = new NavButton(item[1], Objects.equals(pageName, activePage));

            // ActionListener (lambda): go to the page when the button is clicked
            button.addActionListener(e -> navigator.showPage(pageName));

            menu.add(button);
            menu.add(Box.createVerticalStrut(6));
        }
        return menu;
    }

    private JPanel createBottom(PageNavigator navigator) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.TEXT_MUTED);
        statusLabel.setBorder(new EmptyBorder(0, 8, 8, 0));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, logoutButton.getPreferredSize().height));
        logoutButton.addActionListener(e -> navigator.showPage(AppConstants.PAGE_WELCOME));

                reminderLabel.setFont(ThemeManager.FONT_SMALL);
        reminderLabel.setForeground(ThemeManager.ACCENT);
        reminderLabel.setBorder(new EmptyBorder(0, 8, 10, 0));
        reminderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottom.add(reminderLabel);
        bottom.add(statusLabel);
        bottom.add(logoutButton);
        return bottom;
    }

    /** Updates the status text and the logout button text (guest mode or normal). */
    public void refresh() {
        boolean guest = dataManager.isGuestMode();
        statusLabel.setText(guest ? "Guest mode (not saved)" : "Saved on this device");
        logoutButton.setText(guest ? "Leave guest mode" : "Log out");
    }

    /**
     * One sidebar button. The active page is shown filled in rose,
     * and other buttons light up when the mouse is over them.
     */
    private static class NavButton extends JButton {

        private final boolean active;
        private boolean hover = false;

        NavButton(String text, boolean active) {
            super(text);
            this.active = active;

            setFont(active ? ThemeManager.FONT_BODY.deriveFont(Font.BOLD) : ThemeManager.FONT_BODY);
            setForeground(active ? Color.WHITE : ThemeManager.TEXT_DARK);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, 18, 11, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            ThemeManager.enableAntialiasing(g2);
            if (active) {
                g2.setColor(ThemeManager.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            } else if (hover) {
                g2.setColor(ThemeManager.SECONDARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
