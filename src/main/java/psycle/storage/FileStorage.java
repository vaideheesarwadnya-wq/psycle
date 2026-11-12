package psycle.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import psycle.interfaces.DataStorage;
import psycle.utils.AppConstants;

/**
 * Saves and loads text files inside the data folder.
 * Implements the DataStorage interface.
 */
public class FileStorage implements DataStorage {

    private final String folderPath;

    /** Uses the default "data" folder. */
    public FileStorage() {
        this(AppConstants.DATA_FOLDER);
    }

    /** Uses a custom folder. */
    public FileStorage(String folderPath) {
        this.folderPath = folderPath;
    }

    /** Returns the File object for a file name, creating the folder if needed. */
    private File getFile(String fileName) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not create the data folder: " + folder.getPath());
        }
        return new File(folder, fileName);
    }

    @Override
    public void saveData(String fileName, ArrayList<String> lines) throws IOException {
        File file = getFile(fileName);
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException("Could not save " + fileName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<String> loadData(String fileName) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        File file = getFile(fileName);
        if (!file.exists()) {
            return lines; // first run: nothing saved yet
        }
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            return lines; // file disappeared: treat as empty
        } catch (IOException e) {
            throw new IOException("Could not read " + fileName + ": " + e.getMessage(), e);
        }
        return lines;
    }

    @Override
    public void deleteData(String fileName) throws IOException {
        File file = getFile(fileName);
        if (file.exists() && !file.delete()) {
            throw new IOException("Could not delete " + fileName);
        }
    }
}
