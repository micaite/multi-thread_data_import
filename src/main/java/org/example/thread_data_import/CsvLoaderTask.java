package org.example.thread_data_import;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvLoaderTask extends Task<List<Person>> {

    private final String filePath;
    private final String fileName;
    private final int threadNumber;

    public CsvLoaderTask (String filePath, String fileName, int threadNumber){
        this.filePath = filePath;
        this.fileName = fileName;
        this.threadNumber = threadNumber;

    }

    @Override
    protected List<Person> call() throws Exception {

        updateMessage("Thread " + threadNumber + " (" + fileName + "): starting...");
        updateProgress(0, 100);

        Thread.sleep(500L);

        long totalLines;
        try {
            totalLines = Files.lines(Paths.get(filePath)).count() - 1;
        } catch (Exception e) {
            throw new Exception("Failed to open the file: " + fileName);
        }

        if (totalLines <= 0) totalLines = 1;

        List<Person> people = new ArrayList<>();
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String header = reader.readLine(); // praleidziam antraste
            if (header == null) {
                throw new Exception("File is empty: " + fileName);

            }

            String line;
            while((line = reader.readLine()) != null) {

                if (isCancelled()) {
                    updateMessage("Thread " + threadNumber + " (" + fileName + "): is cancelled.");
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                Person person = parseLine(line);
                if (person != null) {
                    people.add(person);
                }

                lineCount++;

                // atnaujinam pazanga
                double progress = (double) lineCount / totalLines * 100.0;
                updateProgress(progress, 100);
                updateMessage(String.format("Thread %d (%s): %d / %d lines (%.0f%%)",
                        threadNumber, fileName, lineCount, totalLines, progress));

                if (lineCount % 50 == 0) {
                    Thread.sleep(20);

                }
            }
        } catch (Exception e) {
            throw new Exception("Mistake reading " + fileName + ": " + e.getMessage());
        }

        updateProgress(100, 100);
        updateMessage("Thread " + threadNumber + " (" + fileName + "): ✓ finished! Updated " + people.size() + " records.");

        return people;
    }

    // skaito po viena eil ir grazina Person obj

    private Person parseLine(String line) {
        try {
            String[] parts = splitCsv(line);
            if (parts.length < 8) return null;

            return new Person(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    parts[6].trim(),
                    parts[7].trim()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String[] splitCsv (String line){
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());

        return tokens.toArray(new String[0]);
    }
}
