package org.example.thread_data_import;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloController {

    @FXML private TableView<Person>           tableView;
    @FXML private TableColumn<Person, String> colId;
    @FXML private TableColumn<Person, String> colFirstName;
    @FXML private TableColumn<Person, String> colLastName;
    @FXML private TableColumn<Person, String> colEmail;
    @FXML private TableColumn<Person, String> colGender;
    @FXML private TableColumn<Person, String> colCountry;
    @FXML private TableColumn<Person, String> colDomain;
    @FXML private TableColumn<Person, String> colBirthDate;

    @FXML private ProgressBar progressBar1, progressBar2, progressBar3;
    @FXML private Label       threadStatus1, threadStatus2, threadStatus3;

    @FXML private TextField        searchField;
    @FXML private DatePicker       dateFrom;
    @FXML private DatePicker       dateTo;
    @FXML private ComboBox<String> sortFieldCombo;
    @FXML private ComboBox<String> sortOrderCombo;

    @FXML private Label statusLabel;
    @FXML private Label shownCountLabel;
    @FXML private Label totalCountLabel;

    private final ObservableList<Person> allData = FXCollections.synchronizedObservableList(
            FXCollections.observableList(new CopyOnWriteArrayList<>())
    );
    private final ObservableList<Person> filteredData = FXCollections.observableArrayList();

    private final FilterService       filterService = new FilterService();
    private final List<CsvLoaderTask> tasks         = new ArrayList<>();
    private final List<Thread>        threads       = new ArrayList<>();

    private final AtomicInteger finishedCount = new AtomicInteger(0);

    private static final String[] FILE_PATHS = {
            "MOCK_DATA1.csv",
            "MOCK_DATA2.csv",
            "MOCK_DATA3.csv"
    };

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colDomain.setCellValueFactory(new PropertyValueFactory<>("domainName"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        tableView.setItems(filteredData);

        sortFieldCombo.setItems(FXCollections.observableArrayList(
                "None", "First Name", "Last Name", "Email", "Country", "ID", "Birth Date"));
        sortFieldCombo.setValue("None");

        sortOrderCombo.setItems(FXCollections.observableArrayList(
                "Ascending", "Descending"));
        sortOrderCombo.setValue("Ascending");

        searchField.textProperty().addListener((o, old, val) -> applyFilters());
        dateFrom.valueProperty().addListener((o, old, val) -> applyFilters());
        dateTo.valueProperty().addListener((o, old, val) -> applyFilters());
        sortFieldCombo.valueProperty().addListener((o, old, val) -> applyFilters());
        sortOrderCombo.valueProperty().addListener((o, old, val) -> applyFilters());
    }

    @FXML
    protected void onLoadClicked() {
        stopTasks();
        allData.clear();
        filteredData.clear();
        tasks.clear();
        threads.clear();
        finishedCount.set(0);

        resetProgress();
        setStatus("Loading data with 3 threads...");

        ProgressBar[] bars     = { progressBar1, progressBar2, progressBar3 };
        Label[]       statuses = { threadStatus1, threadStatus2, threadStatus3 };

        for (int i = 0; i < 3; i++) {
            final ProgressBar bar    = bars[i];
            final Label       status = statuses[i];
            final int         idx    = i;

            CsvLoaderTask task = new CsvLoaderTask(
                    FILE_PATHS[i],
                    FILE_PATHS[i],
                    i + 1,
                    person -> {
                        allData.add(person);
                        applyFilters();
                    },
                    progress -> bar.setProgress(progress / 100.0),
                    msg -> status.setText(msg),
                    () -> {
                        status.setStyle("-fx-text-fill: green;");
                        checkIfAllDone();
                    }
            );

            tasks.add(task);

            Thread thread = new Thread(task);
            thread.setName("csv-loader-" + (idx + 1));
            thread.setDaemon(true);
            thread.start();

            threads.add(thread);
        }
    }

    @FXML
    protected void onStopClicked() {
        stopTasks();
        setStatus("Stopped.");
    }

    @FXML
    protected void onClearFilters() {
        searchField.clear();
        dateFrom.setValue(null);
        dateTo.setValue(null);
        sortFieldCombo.setValue("None");
        sortOrderCombo.setValue("Ascending");
    }

    private void applyFilters() {
        String sortField = sortFieldCombo.getValue();
        if ("None".equals(sortField)) sortField = null;

        boolean ascending = sortOrderCombo.getValue().startsWith("Ascending");

        List<Person> result = filterService.apply(
                new ArrayList<>(allData),
                searchField.getText(),
                dateFrom.getValue(),
                dateTo.getValue(),
                sortField,
                ascending
        );

        filteredData.setAll(result);
        shownCountLabel.setText("Showing: " + result.size());
        totalCountLabel.setText("Total: " + allData.size() + " records");
    }

    private void stopTasks() {
        tasks.forEach(CsvLoaderTask::cancel);
        threads.forEach(Thread::interrupt);
        resetProgress();
    }

    private void resetProgress() {
        ProgressBar[] bars     = { progressBar1, progressBar2, progressBar3 };
        Label[]       statuses = { threadStatus1, threadStatus2, threadStatus3 };
        for (int i = 0; i < 3; i++) {
            bars[i].setProgress(0);
            statuses[i].setText("Waiting...");
            statuses[i].setStyle("");
        }
    }

    private void checkIfAllDone() {
        if (finishedCount.incrementAndGet() == 3) {
            setStatus("Done! Loaded " + allData.size() + " records.");
            totalCountLabel.setText("Total: " + allData.size() + " records");
        }
    }

    private void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}