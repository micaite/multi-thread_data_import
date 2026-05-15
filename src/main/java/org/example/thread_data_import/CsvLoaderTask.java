package org.example.thread_data_import;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class CsvLoaderTask implements Runnable {

    private final String filePath;
    private final String fileName;
    private final int threadNumber;

    private final Consumer<Person>  onPersonLoaded;
    private final Consumer<Double>  onProgress;
    private final Consumer<String>  onMessage;
    private final Runnable          onFinished;

    // volatile - ensures visibility across threads
    private volatile boolean cancelled = false;

    public CsvLoaderTask(String filePath, String fileName, int threadNumber,
                         Consumer<Person> onPersonLoaded,
                         Consumer<Double> onProgress,
                         Consumer<String> onMessage,
                         Runnable         onFinished) {
        this.filePath       = filePath;
        this.fileName       = fileName;
        this.threadNumber   = threadNumber;
        this.onPersonLoaded = onPersonLoaded;
        this.onProgress     = onProgress;
        this.onMessage      = onMessage;
        this.onFinished     = onFinished;
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run() {
        try {
            sendMessage("Thread " + threadNumber + " (" + fileName + "): starting...");
            sendProgress(0.0);

            Thread.sleep(200L);

            long totalLines;
            try {
                totalLines = Files.lines(Paths.get(filePath)).count() - 1;
            } catch (Exception e) {
                sendMessage("Thread " + threadNumber + ": failed to open " + fileName);
                return;
            }

            if (totalLines <= 0) totalLines = 1;

            int lineCount = 0;
            int[] count = {0};

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

                String header = reader.readLine();
                if (header == null) {
                    sendMessage("Thread " + threadNumber + ": file is empty: " + fileName);
                    return;
                }

                String line;
                while ((line = reader.readLine()) != null) {

                    if (cancelled) {
                        sendMessage("Thread " + threadNumber + " (" + fileName + "): cancelled.");
                        return;
                    }

                    line = line.trim();
                    if (line.isEmpty()) continue;

                    Person person = parseLine(line);
                    if (person != null) {
                        count[0]++;
                        Platform.runLater(() -> onPersonLoaded.accept(person));
                    }

                    lineCount++;
                    double progress = (double) lineCount / totalLines * 100.0;
                    sendProgress(progress);
                    sendMessage(String.format("Thread %d (%s): %d / %d lines (%.0f%%)",
                            threadNumber, fileName, lineCount, totalLines, progress));

                    if (lineCount % 50 == 0) {
                        Thread.sleep(20);
                    }
                }

            } catch (Exception e) {
                sendMessage("Thread " + threadNumber + ": error reading " + fileName + ": " + e.getMessage());
                return;
            }

            sendProgress(100.0);
            sendMessage("Thread " + threadNumber + " (" + fileName + "): ✓ finished! Loaded " + count[0] + " records.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendMessage("Thread " + threadNumber + " (" + fileName + "): interrupted.");
        } finally {
            Platform.runLater(onFinished);
        }
    }

    private void sendMessage(String msg) {
        Platform.runLater(() -> onMessage.accept(msg));
    }

    private void sendProgress(double progress) {
        Platform.runLater(() -> onProgress.accept(progress));
    }

    private Person parseLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 8) return null;
            return new Person(
                    parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
                    parts[4].trim(), parts[5].trim(), parts[6].trim(), parts[7].trim()
            );
        } catch (Exception e) {
            return null;
        }
    }
}