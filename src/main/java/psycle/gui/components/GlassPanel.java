package psycle.gui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import psycle.utils.ThemeManager;

/**
 * Glassmorphism card: semi-transparent white, rounded corners, soft shadow.
 * Swing has no CSS blur, so we imitate it with transparent colours and custom painting.
 */
public class GlassPanel extends JPanel {

    private static final int SHADOW_SIZE = 10; // space reserved around the card for the shadow

    private final int radius;
    private Color fillColor = ThemeManager.GLASS_FILL;
    private boolean hovered = false;
    private boolean selected = false;

    public GlassPanel() {
        this(24, 16);
    }

    public GlassPanel(int radius) {
        this(radius, 16);
    }

    /** radius = corner roundness, padding = space between the card edge and its content. */
    public GlassPanel(int radius, int padding) {
        this.radius = radius;
        setOpaque(false); // we paint our own transparent background
        setLayout(new BorderLayout());
        setPadding(padding);
    }

    /** Sets the inner padding (the shadow space is added automatically). */
    public void setPadding(int padding) {
        int total = padding + SHADOW_SIZE;
        setBorder(new EmptyBorder(total, total, total, total));
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
        repaint();
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ThemeManager.enableAntialiasing(g2);

        int x = SHADOW_SIZE;
        int y = SHADOW_SIZE;
        int w = getWidth() - 2 * SHADOW_SIZE;
        int h = getHeight() - 2 * SHADOW_SIZE;
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        RoundRectangle2D.Double card = new RoundRectangle2D.Double(x, y, w, h, radius, radius);
        g2.setClip(card);

        // 1. Soft shadow: several faint rounded rectangles, each a little bigger
        int shadowAlpha = hovered ? 9 : 6;
        for (int i = SHADOW_SIZE - 2; i > 0; i--) {
            g2.setColor(ThemeManager.withAlpha(ThemeManager.ACCENT, shadowAlpha));
            g2.fillRoundRect(x - i, y - i + 2, w + 2 * i, h + 2 * i,
                    radius + i * 2, radius + i * 2);
        }

        // 2. Frosted fill: slightly lighter at the top
        int boost = hovered ? 70 : 40;
        Color top = ThemeManager.withAlpha(fillColor, Math.min(255, fillColor.getAlpha() + boost));
        g2.setPaint(new GradientPaint(0, y, top, 0, y + h, fillColor));
        g2.fill(card);

        // 3. Pink tint when the card is selected
        if (selected) {
            g2.setColor(ThemeManager.withAlpha(ThemeManager.PRIMARY, 90));
            g2.fill(card);
        }

        // 4. Thin border
        g2.setStroke(new BasicStroke((selected || hovered) ? 2f : 1.5f));
        if (selected) {
            g2.setColor(ThemeManager.ACCENT);
        } else if (hovered) {
            g2.setColor(ThemeManager.withAlpha(ThemeManager.ACCENT, 150));
        } else {
            g2.setColor(ThemeManager.GLASS_BORDER);
        }
        g2.draw(card);

        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ThemeManager.enableAntialiasing(g2);

        int x = SHADOW_SIZE;
        int y = SHADOW_SIZE;
        int w = getWidth() - 2 * SHADOW_SIZE;
        int h = getHeight() - 2 * SHADOW_SIZE;
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        g2.setClip(new RoundRectangle2D.Double(x, y, w, h, radius, radius));
        super.paintChildren(g2);
        g2.dispose();
    }
}
