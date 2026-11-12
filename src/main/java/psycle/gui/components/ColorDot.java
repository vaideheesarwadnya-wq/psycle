package psycle.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import psycle.utils.ThemeManager;

/**
 * A small round colour dot used in legends.
 */
public class ColorDot extends JComponent {

    private final Color color;

    public ColorDot(Color color) {
        this.color = color;
        Dimension size = new Dimension(14, 14);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    /** Static helper: a dot followed by a text label. */
    public static JPanel legendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        item.setOpaque(false);
        item.add(new ColorDot(color));

        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_SMALL);
        label.setForeground(ThemeManager.TEXT_MUTED);
        item.add(label);
        return item;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ThemeManager.enableAntialiasing(g2);
        g2.setColor(color);
        g2.fillOval(1, 1, 11, 11);
        g2.setColor(ThemeManager.withAlpha(ThemeManager.ACCENT, 120)); // thin border for pale dots
        g2.drawOval(1, 1, 11, 11);
        g2.dispose();
    }
}