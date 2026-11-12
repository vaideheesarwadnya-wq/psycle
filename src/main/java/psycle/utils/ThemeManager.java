package psycle.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Holds the pink theme of Psycle (colours and fonts).
 * Everything is static so any class can use ThemeManager.PRIMARY, etc.
 */
public class ThemeManager {

    // ---------- Colours ----------
    public static final Color PRIMARY = new Color(0xF5B8CE);      // soft pastel pink
    public static final Color SECONDARY = new Color(0xFCE4EC);    // light blush pink
    public static final Color BACKGROUND = new Color(0xFFF9FB);   // off-white
    public static final Color ACCENT = new Color(0xD879A2);       // rose pink
    public static final Color TEXT_DARK = new Color(0x4A2C3F);    // dark plum
    public static final Color TEXT_MUTED = new Color(0x9C7A8E);   // muted mauve

    public static final Color GLASS_FILL = new Color(255, 255, 255, 175);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 230);
    public static final Color SHADOW = new Color(216, 121, 162, 45);

    public static final Color GRADIENT_TOP = new Color(0xFFE0EC);
    public static final Color GRADIENT_BOTTOM = new Color(0xFFF9FB);

    // Calendar colours
    public static final Color PERIOD_DAY = new Color(0xD879A2);
    public static final Color PREDICTED_DAY = new Color(0xF9D3E2);

    // One shade of pink for each cycle phase
    public static final Color MENSTRUAL_COLOR = new Color(0xF2A0BC);
    public static final Color FOLLICULAR_COLOR = new Color(0xF7BFD3);
    public static final Color OVULATORY_COLOR = new Color(0xFAD4E1);
    public static final Color LUTEAL_COLOR = new Color(0xFDE6EE);

    // ---------- Fonts ----------
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 34);

    /** Returns the same colour with a different transparency (0 = invisible, 255 = solid). */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Turns on smooth edges for shapes and text. Call at the start of paintComponent. */
    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Picks the colour for a phase name. */
    public static Color getPhaseColor(String phase) {
        if (AppConstants.PHASE_MENSTRUAL.equals(phase)) {
            return MENSTRUAL_COLOR;
        } else if (AppConstants.PHASE_FOLLICULAR.equals(phase)) {
            return FOLLICULAR_COLOR;
        } else if (AppConstants.PHASE_OVULATORY.equals(phase)) {
            return OVULATORY_COLOR;
        } else if (AppConstants.PHASE_LUTEAL.equals(phase)) {
            return LUTEAL_COLOR;
        }
        return SECONDARY;
    }

    private ThemeManager() {
    }
}
