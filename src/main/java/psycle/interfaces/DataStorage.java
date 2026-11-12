package psycle.interfaces;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface: a contract for storing data.
 * Any class that implements it must provide all three methods.
 * Each file is handled as a list of text lines.
 */
public interface DataStorage {

    /** Saves the lines into the named file (replacing old content). */
    void saveData(String fileName, ArrayList<String> lines) throws IOException;

    /** Loads all lines of the named file. Returns an empty list if the file does not exist. */
    ArrayList<String> loadData(String fileName) throws IOException;

    /** Deletes the named file. */
    void deleteData(String fileName) throws IOException;
}
