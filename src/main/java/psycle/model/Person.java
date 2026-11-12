package psycle.model;

/**
 * Parent class for single inheritance: Person -> User.
 */
public class Person {

    private String name;

    /** Constructor with no name. */
    public Person() {
        this("");
    }

    /** Constructor with a name. */
    public Person(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null) ? "" : name.trim();
    }

    /** Name to show on screen. */
    public String getDisplayName() {
        return name.isEmpty() ? "Friend" : name;
    }

    /** Short description. User overrides this (method overriding). */
    public String describe() {
        return "Person: " + getDisplayName();
    }
}
