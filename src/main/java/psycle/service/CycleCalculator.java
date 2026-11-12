

package psycle.service;
import psycle.model.Cycle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

import psycle.utils.AppConstants;
import psycle.utils.DateUtils;

/**
 * Basic cycle prediction. All results are ESTIMATES, not medical advice.
 */
public class CycleCalculator {

    private final ArrayList<Cycle> cycles;

    /** Constructor 1: calculator with no data. */
    public CycleCalculator() {
        this(new ArrayList<Cycle>());
    }

    /** Constructor 2: calculator that reads the given list of cycles. */
    public CycleCalculator(ArrayList<Cycle> cycles) {
        this.cycles = cycles;
    }

    // =====================================================
    //  Static helper methods (no object needed)
    // =====================================================

    /** A realistic cycle length is between 15 and 60 days. */
    public static boolean isValidCycleLength(int length) {
        return length >= AppConstants.MIN_CYCLE_LENGTH && length <= AppConstants.MAX_CYCLE_LENGTH;
    }

    /** Average of a list of cycle lengths. Uses 28 days if the list is empty. */
    public static int averageOf(ArrayList<Integer> lengths) {
        if (lengths.isEmpty()) {
            return 28;
        }
        int total = 0;
        Iterator<Integer> it = lengths.iterator();
        while (it.hasNext()) {
            total += it.next();
        }
        return Math.round((float) total / lengths.size());
    }

    /** Estimates the phase from the cycle day, cycle length and period length. */
    public static String phaseForDay(int cycleDay, int cycleLength, int periodLength) {
        if (cycleDay <= 0) {
            return "Unknown";
        }
        if (cycleDay <= periodLength) {
            return AppConstants.PHASE_MENSTRUAL;
        }
        // Ovulation usually happens about 14 days before the next period.
        int ovulationDay = cycleLength - AppConstants.LUTEAL_PHASE_LENGTH;
        if (cycleDay < ovulationDay - 1) {
            return "Follicular";
        }
        if (cycleDay <= ovulationDay + 1) {
            return AppConstants.PHASE_OVULATORY;
        }
        return AppConstants.PHASE_LUTEAL;
    }

    /** Returns text like "Days 1-5" for a phase (used on the Cycle Phases page). */
    public static String phaseDayRange(String phase, int cycleLength, int periodLength) {
        int ovulationDay = cycleLength - AppConstants.LUTEAL_PHASE_LENGTH;
        int from;
        int to;
        if (AppConstants.PHASE_MENSTRUAL.equals(phase)) {
            from = 1;
            to = periodLength;
        } else if (AppConstants.PHASE_FOLLICULAR.equals(phase)) {
            from = periodLength + 1;
            to = ovulationDay - 2;
        } else if (AppConstants.PHASE_OVULATORY.equals(phase)) {
            from = ovulationDay - 1;
            to = ovulationDay + 1;
        } else {
            from = ovulationDay + 2;
            to = cycleLength;
        }
        if (to < from) {
            to = from;
        }
        return "Days " + from + "-" + to;
    }

    // =====================================================
    //  Cycle lengths and averages
    // =====================================================

    /** Returns a copy of the cycles sorted by start date (oldest first). */
    private ArrayList<Cycle> getSortedCycles() {
        ArrayList<Cycle> sorted = new ArrayList<>(cycles);
        sorted.sort((a, b) -> a.getStartDate().compareTo(b.getStartDate()));
        return sorted;
    }

    public boolean hasData() {
        return !cycles.isEmpty();
    }

    /** Most recent period start date, or null if nothing is recorded. */
    public LocalDate getLastPeriodStart() {
        ArrayList<Cycle> sorted = getSortedCycles();
        if (sorted.isEmpty()) {
            return null;
        }
        return sorted.get(sorted.size() - 1).getStartDate();
    }

    /** Days between each pair of consecutive period starts (unrealistic gaps are skipped). */
    public ArrayList<Integer> getCycleLengths() {
        ArrayList<Integer> lengths = new ArrayList<>();
        ArrayList<Cycle> sorted = getSortedCycles();
        for (int i = 1; i < sorted.size(); i++) {
            int length = (int) DateUtils.daysBetween(
                    sorted.get(i - 1).getStartDate(), sorted.get(i).getStartDate());
            if (isValidCycleLength(length)) {
                lengths.add(length);
            }
        }
        return lengths;
    }

    public int getAverageCycleLength() {
        return averageOf(getCycleLengths());
    }

    /** Average period duration from periods that have an end date (default 5). */
    public int getAveragePeriodLength() {
        int total = 0;
        int count = 0;
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            if (!c.isOngoing()) {
                total += c.getPeriodLength();
                count++;
            }
        }
        if (count == 0) {
            return AppConstants.DEFAULT_PERIOD_LENGTH;
        }
        return Math.round((float) total / count);
    }

    /** True if the longest and shortest cycle differ by more than 7 days. */
    public boolean isInconsistent() {
        ArrayList<Integer> lengths = getCycleLengths();
        if (lengths.size() < 2) {
            return false;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Iterator<Integer> it = lengths.iterator();
        while (it.hasNext()) {
            int value = it.next();
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return (max - min) > 7;
    }

    // =====================================================
    //  Method overloading: three versions of calculateNextPeriod
    // =====================================================

    /** Version 1: uses the average of this calculator's recorded cycles. */
    public LocalDate calculateNextPeriod() {
        return calculateNextPeriod(getCycleLengths());
    }

    /** Version 2: uses a given average cycle length. */
    public LocalDate calculateNextPeriod(int averageCycleLength) {
        if (!isValidCycleLength(averageCycleLength)) {
            throw new IllegalArgumentException("Invalid cycle length: " + averageCycleLength
                    + " days (expected " + AppConstants.MIN_CYCLE_LENGTH + "-"
                    + AppConstants.MAX_CYCLE_LENGTH + ").");
        }
        LocalDate lastStart = getLastPeriodStart();
        if (lastStart == null) {
            return null; // no period recorded yet
        }
        return lastStart.plusDays(averageCycleLength);
    }

    /** Version 3: uses the average of a list of cycle lengths. */
    public LocalDate calculateNextPeriod(ArrayList<Integer> cycleLengths) {
        return calculateNextPeriod(averageOf(cycleLengths));
    }

    // =====================================================
    //  Current status (used by the dashboard)
    // =====================================================

    /** Cycle day for any date (day 1 = most recent period start on or before that date). */
    public int getCycleDayFor(LocalDate date) {
        LocalDate bestStart = null;
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            LocalDate start = it.next().getStartDate();
            if (!start.isAfter(date) && (bestStart == null || start.isAfter(bestStart))) {
                bestStart = start;
            }
        }
        if (bestStart == null) {
            return 0;
        }
        return (int) DateUtils.daysBetween(bestStart, date) + 1;
    }

    public int getCurrentCycleDay() {
        return getCycleDayFor(LocalDate.now());
    }

    /** Days until the next period (negative = the period is late). Check hasData() first. */
    public int getDaysUntilNextPeriod() {
        LocalDate next = calculateNextPeriod();
        if (next == null) {
            return 0;
        }
        return (int) DateUtils.daysBetween(LocalDate.now(), next);
    }

    public String getCurrentPhase() {
        if (!hasData()) {
            return AppConstants.PHASE_UNKNOWN;
        }
        return phaseForDay(getCurrentCycleDay(), getAverageCycleLength(), getAveragePeriodLength());
    }

    /** Message that explains how reliable the prediction is. */
    public String getPredictionNote() {
        if (!hasData()) {
            return "Start tracking your cycle.";
        }
        int recordedCycles = getCycleLengths().size();
        if (recordedCycles == 0) {
            return "Preliminary estimate based on a default "
                    + AppConstants.DEFAULT_CYCLE_LENGTH
                    + "-day cycle. Log more periods for a better prediction.";
        }
        if (isInconsistent()) {
            return "Your cycle lengths vary quite a bit, so this prediction may be less reliable."
                    + " This is an estimate, not medical advice.";
        }
        return "Estimate based on " + recordedCycles + " recorded cycle(s)."
                + " This is not medical advice.";
    }

    // =====================================================
    //  Calendar helpers
    // =====================================================

    /** True if the date is inside a recorded period. */
    public boolean isRecordedPeriodDay(LocalDate date) {
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            if (it.next().isPeriodDay(date)) {
                return true;
            }
        }
        return false;
    }

    /** True if the date falls in the predicted next period. */
    public boolean isPredictedPeriodDay(LocalDate date) {
        LocalDate next = calculateNextPeriod();
        if (next == null) {
            return false;
        }
        LocalDate last = next.plusDays(getAveragePeriodLength() - 1);
        return !date.isBefore(next) && !date.isAfter(last);
    }

    /** Lists every recorded period day, using an Iterator over the cycles. */
    public ArrayList<LocalDate> getRecordedPeriodDays() {
        ArrayList<LocalDate> days = new ArrayList<>();
        Iterator<Cycle> it = cycles.iterator();
        while (it.hasNext()) {
            Cycle c = it.next();
            LocalDate day = c.getStartDate();
            LocalDate lastDay = c.isOngoing() ? c.getStartDate() : c.getEndDate();
            while (!day.isAfter(lastDay)) {
                days.add(day);
                day = day.plusDays(1);
            }
        }
        return days;
    }
}
