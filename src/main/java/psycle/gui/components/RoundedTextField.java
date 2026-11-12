package psycle.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import psycle.utils.ThemeManager;

/**
 * A JTextField with rounded corners that matches the Psycle theme.
 */
public class RoundedTextField extends JTextField {

    public RoundedTextField(int columns) {
        super(columns);
        setOpaque(false);
        setFont(ThemeManager.FONT_BODY);
        setForeground(ThemeManager.TEXT_DARK);
        setCaretColor(ThemeManager.ACCENT);
        setSelectionColor(ThemeManager.PRIMARY);
        setBorder(new EmptyBorder(10, 18, 10, 18));

        // Repaint when focus changes so the border colour updates
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    /** Draws the rounded white box and border. Also used by RoundedPasswordField. */
    public static void paintFieldBackground(Graphics g, JComponent field) {
        if (g == null || field == null) {
            return;
        }

        int w = field.getWidth();
        int h = field.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        ThemeManager.enableAntialiasing(g2);

        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);

        boolean focused = field.isFocusOwner();
        g2.setColor(focused ? ThemeManager.ACCENT : ThemeManager.PRIMARY);
        g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintFieldBackground(g, this);
        super.paintComponent(g); // draws the text
    }
}
