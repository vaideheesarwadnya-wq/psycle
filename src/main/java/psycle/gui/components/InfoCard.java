package psycle.gui.components;
import psycle.utils.ThemeManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import psycle.events.CardMouseListener;

/**
 * Small information card: title, big value, subtitle.
 */
public class InfoCard extends GlassPanel {

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font VALUE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 28);
    private static final Color MUTED_COLOR = new Color(126, 138, 153);
    private static final Color ACCENT_COLOR = new Color(92, 132, 255);

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;

    /** Card with only a title (value shows "-"). */
    public InfoCard(String title) {
        this(title, "-", "");
    }

    /** Full card. */
    public InfoCard(String title, String value, String subtitle) {
        super(24, 18);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        titleLabel = makeLabel(title, ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);
        valueLabel = makeLabel(value, ThemeManager.FONT_BIG_NUMBER, ThemeManager.ACCENT);
        subtitleLabel = makeLabel(subtitle, ThemeManager.FONT_SMALL, ThemeManager.TEXT_MUTED);

        add(titleLabel);
        add(Box.createVerticalStrut(4));
        add(valueLabel);
        add(Box.createVerticalStrut(2));
        add(subtitleLabel);

        // Hover effect for every info card
        addMouseListener(new CardMouseListener(this));
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    /** Use a smaller font for long values such as "Follicular". */
    public void setValueFont(Font font) {
        valueLabel.setFont(font);
    }
}
