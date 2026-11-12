package psycle.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import psycle.utils.ThemeManager;

/**
 * Lightweight bar chart used by the insights page. It renders a simple set of
 * labelled bars without depending on any external chart library.
 */
public class BarChart extends JPanel {

    private final boolean showValues;
    private final Color accent;
    private String[] labels = new String[0];
    private double[] values = new double[0];
    private String emptyMessage = "";

    public BarChart(boolean showValues, Color accent) {
        this.showValues = showValues;
        this.accent = accent;
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 0, 0));
        setLayout(new BorderLayout());
    }

    public void setData(String[] labels, double[] values) {
        this.labels = labels == null ? new String[0] : labels;
        this.values = values == null ? new double[0] : values;
        repaint();
    }

    public void setData(ArrayList<String> labels, double[] values) {
        if (labels == null) {
            setData(new String[0], values);
            return;
        }
        String[] labelArray = labels.toArray(new String[0]);
        setData(labelArray, values);
    }

    public void setData(ArrayList<String> labels, ArrayList<Integer> values) {
        if (values == null) {
            setData(labels, new double[0]);
            return;
        }
        double[] numeric = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            numeric[i] = values.get(i);
        }
        setData(labels, numeric);
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage == null ? "" : emptyMessage;
        if (values.length == 0) {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }
        if (values.length == 0) {
            if (!emptyMessage.isEmpty()) {
                g2.setColor(ThemeManager.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                int x = (width - g2.getFontMetrics().stringWidth(emptyMessage)) / 2;
                int y = height / 2;
                g2.drawString(emptyMessage, x, y);
            }
            g2.dispose();
            return;
        }

        int maxBars = Math.min(labels.length, values.length);
        int chartHeight = Math.max(90, height - 20);
        int leftInset = 10;
        int barSpacing = 10;
        int barWidth = Math.max(14, (width - leftInset * 2 - (maxBars - 1) * barSpacing) / Math.max(1, maxBars));
        double maxValue = 0;
        for (int i = 0; i < values.length; i++) {
            maxValue = Math.max(maxValue, Math.abs(values[i]));
        }
        if (maxValue <= 0) {
            maxValue = 1;
        }

        for (int i = 0; i < maxBars; i++) {
            int x = leftInset + i * (barWidth + barSpacing);
            int barValue = (int) Math.round((values[i] / maxValue) * (chartHeight - 18));
            int y = height - 14 - barValue;
            g2.setColor(accent);
            g2.fillRoundRect(x, y, barWidth, barValue, 8, 8);
            if (showValues) {
                g2.setColor(ThemeManager.TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(String.format("%.0f", values[i]), x, y - 4);
            }
            if (i < labels.length) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(ThemeManager.TEXT_MUTED);
                g2.drawString(labels[i], x - 2, height - 2);
            }
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(220, 140);
    }
}
