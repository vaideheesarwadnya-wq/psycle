package psycle.events;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * MouseListener for glass cards.
 * - mouseEntered / mouseExited : card lights up while the mouse is over it
 * - mouseClicked : runs the action given by the page (can be null for hover-only cards)
 */
public class CardMouseListener implements MouseListener {

    private final Object card;
    private final Runnable onClick; // may be null

    /** Hover-only card. */
    public CardMouseListener(Object card) {
        this(card, null);
    }

    /** Hoverable and clickable card. */
    public CardMouseListener(Object card, Runnable onClick) {
        this.card = card;
        this.onClick = onClick;
        if (onClick != null) {
            invokeMethod("setCursor", Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void invokeMethod(String methodName, Object... args) {
        if (card == null) {
            return;
        }

        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = toParameterType(args[i]);
        }

        try {
            Method method = card.getClass().getMethod(methodName, parameterTypes);
            method.invoke(card, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Ignore when the backing card implementation is not a GlassPanel.
        }
    }

    private Class<?> toParameterType(Object value) {
        if (value instanceof Boolean) {
            return boolean.class;
        }
        if (value instanceof Byte) {
            return byte.class;
        }
        if (value instanceof Short) {
            return short.class;
        }
        if (value instanceof Integer) {
            return int.class;
        }
        if (value instanceof Long) {
            return long.class;
        }
        if (value instanceof Float) {
            return float.class;
        }
        if (value instanceof Double) {
            return double.class;
        }
        if (value instanceof Character) {
            return char.class;
        }
        if (value instanceof Void) {
            return void.class;
        }
        return value.getClass();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Nothing to do: the click action runs in mouseClicked.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Nothing to do: the click action runs in mouseClicked.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        invokeMethod("setHovered", true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        invokeMethod("setHovered", false);
    }
}
