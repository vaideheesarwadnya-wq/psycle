package psycle.gui.components;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import psycle.utils.ThemeManager;

/**
 * A panel that paints a soft pink gradient as its background.
 */
public class GradientPanel extends JPanel {

    private final Color topColor;
    private final Color bottomColor;

    /** Default pink-to-white gradient. */
    public GradientPanel() {
        this(ThemeManager.GRADIENT_TOP, ThemeManager.GRADIENT_BOTTOM);
    }

    /** Custom gradient colours. */
    public GradientPanel(Color topColor, Color bottomColor) {
        this.topColor = topColor;
        this.bottomColor = bottomColor;
        setOpaque(true);
    }

    /** Static method so other classes (like BasePage) can paint the same background. */
    public static void paintBackground(Graphics2D g2, int width, int height,
                                       Color top, Color bottom) {
        ThemeManager.enableAntialiasing(g2);

        // Vertical gradient
        g2.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
        g2.fillRect(0, 0, width, height);

        // Two big soft circles for a modern look
        g2.setColor(ThemeManager.withAlpha(ThemeManager.PRIMARY, 60));
        g2.fillOval(width - 380, -150, 500, 500);
        g2.setColor(ThemeManager.withAlpha(ThemeManager.ACCENT, 30));
        g2.fillOval(-170, height - 300, 440, 440);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        paintBackground(g2, getWidth(), getHeight(), topColor, bottomColor);
        g2.dispose();
    }
}
