package psycle.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import psycle.utils.ThemeManager;

/**
 * A rounded (pill-shaped) button. It is a normal JButton, so addActionListener() works as usual.
 */
public class RoundedButton extends JButton {

    private static final Color HOVER_COLOR = new Color(0xC96590);
    private static final Color PRESSED_COLOR = new Color(0xB5527D);

    private final boolean primary;
    private boolean hover = false;

    /** Filled (primary) button. */
    public RoundedButton(String text) {
        this(text, true);
    }

    /** primary = true: filled rose button. primary = false: white outlined button. */
    public RoundedButton(String text, boolean primary) {
        super(text);
        this.primary = primary;

        setFont(ThemeManager.FONT_BODY.deriveFont(java.awt.Font.BOLD));
        setForeground(primary ? Color.WHITE : HOVER_COLOR);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setBorder(new EmptyBorder(11, 26, 11, 26));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
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
        int w = getWidth();
        int h = getHeight();

        if (primary) {
            Color fill = ThemeManager.ACCENT;
            if (!isEnabled()) {
                fill = ThemeManager.withAlpha(fill, 110);
            } else if (getModel().isPressed()) {
                fill = PRESSED_COLOR;
            } else if (hover) {
                fill = HOVER_COLOR;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, h, h);
        } else {
            Color fill = new Color(255, 255, 255, 200);
            if (getModel().isPressed()) {
                fill = ThemeManager.PRIMARY;
            } else if (hover) {
                fill = ThemeManager.SECONDARY;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(ThemeManager.PRIMARY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
        }
        g2.dispose();

        super.paintComponent(g); // draws the button text on top
    }
}
