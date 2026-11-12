package psycle.model;

import java.time.LocalDate;


/**
 * One recorded period. The "cycle length" is the gap between two period start dates.
 */
public class Cycle {

    private LocalDate startDate;
    private LocalDate endDate; // null while the period has no recorded end

    /** Constructor 1: period starting today. */
    public Cycle() {
        this(LocalDate.now());
    }

    /** Constructor 2: period with a start date only. */
    public Cycle(LocalDate startDate) {
        this(startDate, null);
    }

    /** Constructor 3: period with start and end dates. */
    public Cycle(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Period start date is required.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Period end cannot be before its start.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Period end cannot be before its start.");
        }
        this.endDate = endDate;
    }

    /** True if no end date has been recorded yet. */
    public boolean isOngoing() {
        return endDate == null;
    }

    /** Number of period days (start and end both counted). 0 if there is no end date. */
    public int getPeriodLength() {
        if (endDate == null) {
            return 0;
        }
        return (int) (java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    /** True if the given date is one of the recorded period days. */
    public boolean isPeriodDay(LocalDate date) {
        if (endDate == null) {
            return date.equals(startDate);
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
