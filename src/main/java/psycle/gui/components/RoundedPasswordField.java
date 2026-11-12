package psycle.gui.components;

import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import psycle.utils.ThemeManager;

/**
 * A JPasswordField with rounded corners that matches the Psycle theme.
 */
public class RoundedPasswordField extends JPasswordField {

    public RoundedPasswordField(int columns) {
        super(columns);
        setOpaque(false);
        setFont(ThemeManager.FONT_BODY);
        setForeground(ThemeManager.TEXT_DARK);
        setCaretColor(ThemeManager.ACCENT);
        setSelectionColor(ThemeManager.PRIMARY);
        setBorder(new EmptyBorder(10, 18, 10, 18));

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

    @Override
    protected void paintComponent(Graphics g) {
        RoundedTextField.paintFieldBackground(g, this);
        super.paintComponent(g);
    }
}
