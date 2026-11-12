package psycle.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import psycle.model.DailyEntry;
import psycle.model.Symptom;
import psycle.utils.AppConstants;
import psycle.utils.DateUtils;

/**
 * Calculates simple statistics for the Insights page and the PDF report.
 * Uses HashMap for counting and Iterator for looping.
 */
public class InsightsService {

    private final ArrayList<DailyEntry> entries;
    private final CycleCalculator calculator;

    public InsightsService(ArrayList<DailyEntry> entries, CycleCalculator calculator) {
        this.entries = entries;
        this.calculator = calculator;
    }

    /** Adds 1 to the count of a key in the map. */
    private void addCount(HashMap<String, Integer> map, String key) {
        if (key == null) {
            return;
        }

        String normalizedKey = key.trim();
        if (normalizedKey.isEmpty()) {
            return;
        }

        if (map.containsKey(normalizedKey)) {
            map.put(normalizedKey, map.get(normalizedKey) + 1);
        } else {
            map.put(normalizedKey, 1);
        }
    }

    /** How many times each symptom was logged, e.g. {"Cramps"=4, "Headache"=2}. */
    public HashMap<String, Integer> getSymptomFrequency() {
        HashMap<String, Integer> symptomCount = new HashMap<>();
        Iterator<DailyEntry> entryIt = entries.iterator();
        while (entryIt.hasNext()) {
            DailyEntry entry = entryIt.next();
            Iterator<Symptom> symptomIt = entry.getSymptoms().iterator();
            while (symptomIt.hasNext()) {
                addCount(symptomCount, symptomIt.next().getName());
            }
        }
        return symptomCount;
    }

    /** How many times each mood was logged. */
    public HashMap<String, Integer> getMoodFrequency() {
        HashMap<String, Integer> moodCount = new HashMap<>();
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            addCount(moodCount, it.next().getMood());
        }
        return moodCount;
    }

    /** How many days had each flow level (days with flow "None" are skipped). */
    public HashMap<String, Integer> getFlowFrequency() {
        HashMap<String, Integer> flowCount = new HashMap<>();
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            String flow = it.next().getFlow();
            if (!"None".equals(flow)) {
                addCount(flowCount, flow);
            }
        }
        return flowCount;
    }

    /** Total number of symptoms logged on each cycle day (cycle day -> count). */
    public HashMap<Integer, Integer> getSymptomCountByCycleDay() {
        HashMap<Integer, Integer> countByDay = new HashMap<>();
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            DailyEntry entry = it.next();
            int day = calculator.getCycleDayFor(DateUtils.parse(entry.getDate()));
            if (day >= 1 && day <= AppConstants.MAX_CYCLE_LENGTH) {
                int old = countByDay.containsKey(day) ? countByDay.get(day) : 0;
                countByDay.put(day, old + entry.getSymptoms().size());
            }
        }
        return countByDay;
    }

    /** Returns the key with the highest count, or "None" if the map is empty. */
    public String getMostCommon(HashMap<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return "None";
        }

        String best = "None";
        int bestCount = Integer.MIN_VALUE;
        Iterator<Map.Entry<String, Integer>> it = counts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = it.next();
            String key = pair.getKey();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }

            int value = pair.getValue() == null ? 0 : pair.getValue();
            if (value > bestCount || (value == bestCount && (best.equals("None") || key.compareTo(best) < 0))) {
                best = key;
                bestCount = value;
            }
        }
        return best;
    }

    /** Converts a map into a list sorted from highest count to lowest (good for charts). */
    public ArrayList<Map.Entry<String, Integer>> sortByCount(HashMap<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> {
            int compare = b.getValue().compareTo(a.getValue());
            if (compare != 0) {
                return compare;
            }

            String aKey = a.getKey() == null ? "" : a.getKey();
            String bKey = b.getKey() == null ? "" : b.getKey();
            return aKey.compareTo(bKey);
        });
        return list;
    }

    /** Average pain level across all entries (0 if there are none). */
    public double getAveragePain() {
        if (entries.isEmpty()) {
            return 0;
        }
        int total = 0;
        Iterator<DailyEntry> it = entries.iterator();
        while (it.hasNext()) {
            total += it.next().getPainLevel();
        }
        return (double) total / entries.size();
    }

    public int getEntryCount() {
        return entries.size();
    }

    // ---------- Cycle statistics (come from CycleCalculator) ----------

    public ArrayList<Integer> getCycleLengths() {
        return calculator.getCycleLengths();
    }

    public int getAverageCycleLength() {
        return calculator.getAverageCycleLength();
    }

    public int getAveragePeriodLength() {
        return calculator.getAveragePeriodLength();
    }
}
