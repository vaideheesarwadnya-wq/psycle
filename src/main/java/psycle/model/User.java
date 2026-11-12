package psycle.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single inheritance: User extends Person.
 * Stores a SHA-256 hash of the PIN instead of the PIN itself.
 */
public class User extends Person {

    private static final String SALT = "psycle-salt:";

    private String pinHash;
    private boolean lockEnabled;

    /** Constructor 1: empty user (no name, no PIN). */
    public User() {
        super();
        this.pinHash = "";
        this.lockEnabled = false;
    }

    /** Constructor 2: user with a name only. */
    public User(String name) {
        super(name);
        this.pinHash = "";
        this.lockEnabled = false;
    }

    /** Constructor 3: full user (used when loading from file). */
    public User(String name, String pinHash, boolean lockEnabled) {
        super(name);
        this.pinHash = (pinHash == null) ? "" : pinHash;
        this.lockEnabled = lockEnabled;
    }

    /** Static method: turns a PIN into a SHA-256 hash (hex text). */
    public static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + pin).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this computer.", e);
        }
    }

    /** A valid PIN has only digits and is 4 to 6 digits long. */
    public static boolean isValidPin(String pin) {
        if (pin == null) {
            return false;
        }
        return pin.matches("\\d{4,6}");
    }

    /** Sets a new PIN (stored as a hash). Throws an error if the PIN is invalid. */
    public void setPin(String pin) {
        if (!isValidPin(pin)) {
                throw new IllegalArgumentException("PIN must be 4 to 6 digits.");
        }
        this.pinHash = hashPin(pin);
    }

    /** Checks if the typed PIN matches the saved hash. */
    public boolean checkPin(String pin) {
        return hasPin() && pin != null && hashPin(pin).equals(pinHash);
    }

    public boolean hasPin() {
        return !pinHash.isEmpty();
    }

    public String getPinHash() {
        return pinHash;
    }

    public boolean isLockEnabled() {
        return lockEnabled;
    }

    public void setLockEnabled(boolean lockEnabled) {
        this.lockEnabled = lockEnabled;
    }

    /** Method overriding: replaces Person.describe(). */
    @Override
    public String describe() {
        return "User: " + getDisplayName() + (hasPin() ? " (PIN set)" : " (no PIN)");
    }
}
