package psycle.events;

import java.time.LocalDate;

/**
 * Interface: "someone clicked a date, do something".
 * The calendar page gives the mouse listener an object of this type.
 */
public interface DateSelectionListener {

    void onDateSelected(LocalDate date);
}
