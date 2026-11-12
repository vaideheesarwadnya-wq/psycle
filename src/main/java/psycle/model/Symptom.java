package psycle.model;

/**
 * Represents one symptom (built-in or custom tag).
 */
public class Symptom {

    private String name;
    private boolean custom;

    /** Built-in symptom. */
    public Symptom(String name) {
        this(name, false);
    }

    /** Symptom that can be marked as a custom tag. */
    public Symptom(String name, boolean custom) {
        // ";" is used as a separator when saving, so remove it from names
        this.name = (name == null) ? "" : name.trim().replace(";", " ");
        this.custom = custom;
    }

    public String getName() {
        return name;
    }

    public boolean isCustom() {
        return custom;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Symptom)) {
            return false;
        }
        return name.equalsIgnoreCase(((Symptom) other).name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
