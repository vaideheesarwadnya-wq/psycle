package psycle.utils;

/**
 * Helper methods for writing and reading simple CSV lines.
 * A comma inside a value is saved as "\c", a new line as "\n", a backslash as "\\".
 */
public class CsvUtils {

    /** Makes a single value safe to store inside a CSV line. */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == '\\') {
                sb.append("\\\\");
            } else if (ch == ',') {
                sb.append("\\c");
            } else if (ch == '\n') {
                sb.append("\\n");
            } else if (ch != '\r') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Reverses escape(). */
    public static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\\' && i + 1 < text.length()) {
                i++;
                char next = text.charAt(i);
                if (next == 'c') {
                    sb.append(',');
                } else if (next == 'n') {
                    sb.append('\n');
                } else {
                    sb.append(next);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Joins values into one CSV line, escaping each value. */
    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    /** Splits one CSV line into its (unescaped) values. */
    public static String[] splitLine(String line) {
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unescape(parts[i]);
        }
        return parts;
    }

    private CsvUtils() {
    }
}
