package psycle.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import psycle.utils.DateUtils;

/**
 * One day of health information.
 * Demonstrates constructor overloading and encapsulation (private fields + getters/setters).
 */
public class DailyEntry {

    private String date;                 // stored as yyyy-MM-dd
    private String flow;
    private int painLevel;               // 0 to 5
    private String mood;
    private String energy;
    private String skin;
    private String sleep;
    private ArrayList<Symptom> symptoms;
    private String notes;

    /** Constructor 1: entry for today with default values. */
    public DailyEntry() {
        this(DateUtils.format(LocalDate.now()));
    }

    /** Constructor 2: entry for a given date with default values. */
    public DailyEntry(String date) {
        this(date, "Calm", "None", 0);
    }

    /** Constructor 3: entry with the main fields. */
    public DailyEntry(String date, String mood, String flow, int painLevel) {
        // parse + format makes sure the date is valid and in yyyy-MM-dd form
        this.date = DateUtils.format(DateUtils.parse(date));
        this.mood = mood;
        this.flow = flow;
        setPainLevel(painLevel);
        this.energy = "Medium";
        this.skin = "Clear";
        this.sleep = "Average";
        this.symptoms = new ArrayList<>();
        this.notes = "";
    }

    // ---------- Symptom helpers ----------

    /** Adds a symptom if it is not already in the list. */
    public void addSymptom(Symptom symptom) {
        if (!symptoms.contains(symptom)) {
            symptoms.add(symptom);
        }
    }

    public boolean hasSymptom(String name) {
        return symptoms.contains(new Symptom(name));
    }

    /** Removes a symptom by name using an Iterator. */
    public boolean removeSymptom(String name) {
        Iterator<Symptom> it = symptoms.iterator();
        while (it.hasNext()) {
            if (it.next().getName().equalsIgnoreCase(name)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /** Symptom names joined with ";" (used for saving and for the report). */
    public String getSymptomsAsText() {
        StringBuilder sb = new StringBuilder();
        for (Symptom s : symptoms) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(s.getName());
        }
        return sb.toString();
    }

    // ---------- Getters and setters ----------

    public String getDate() {
        return date;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public int getPainLevel() {
        return painLevel;
    }

    public void setPainLevel(int painLevel) {
        if (painLevel < 0 || painLevel > 5) {
            throw new IllegalArgumentException("Pain level must be between 0 and 5.");
        }
        this.painLevel = painLevel;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public ArrayList<Symptom> getSymptoms() {
        return symptoms;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = (notes == null) ? "" : notes;
    }
}
