package psycle.events;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * MouseListener for one calendar day cell.
 * - mouseClicked : tells the page which date was clicked
 * - mouseEntered / mouseExited : simple hover effect
 */
public class CalendarMouseListener implements MouseListener {

    /** The day cell checks this client property when it paints, to draw a hover ring. */
    public static final String HOVER_PROPERTY = "psycle.hover";

    private final LocalDate date;
    private final JComponent cell;
    private final DateSelectionListener listener;

    public CalendarMouseListener(LocalDate date, JComponent cell, DateSelectionListener listener) {
        this.date = date;
        this.cell = cell;
        this.listener = listener;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            listener.onDateSelected(date);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Nothing to do: selection happens in mouseClicked.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Nothing to do: selection happens in mouseClicked.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        cell.putClientProperty(HOVER_PROPERTY, Boolean.TRUE);
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        cell.putClientProperty(HOVER_PROPERTY, Boolean.FALSE);
        cell.setCursor(Cursor.getDefaultCursor());
        cell.repaint();
    }
}
