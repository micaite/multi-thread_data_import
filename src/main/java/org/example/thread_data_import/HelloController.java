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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloController {

    @FXML private TableView<Person>       tableView;
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

    @FXML private TextField          searchField;
    @FXML private DatePicker         dateFrom;
    @FXML private DatePicker         dateTo;
    @FXML private ComboBox<String>   sortFieldCombo;
    @FXML private ComboBox<String>   sortOrderCombo;

    @FXML private Label statusLabel;
    @FXML private Label shownCountLabel;
    @FXML private Label totalCountLabel;

    private final ObservableList<Person> allData = FXCollections.observableArrayList();
    private final ObservableList<Person> filteredData = FXCollections.observableArrayList();

    private final FilterService filterService = new FilterService();
    private ExecutorService executor;
    private final List<CsvLoaderTask> tasks = new ArrayList<>();

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

        ///

        sortFieldCombo.setItems(FXCollections.observableArrayList(
                "None", "First Name", "Last Name", "Email", "Country", "ID", "Birth Date"));
        sortFieldCombo.setValue("— None —");

        sortOrderCombo.setItems(FXCollections.observableArrayList(
                "Ascending  (A → Z)", "Descending  (Z → A)"));
        sortOrderCombo.setValue("Ascending  (A → Z)");

        searchField.textProperty().addListener((o, old, val) -> applyFilters());
        dateFrom.valueProperty().addListener((o, old, val) -> applyFilters());
        dateTo.valueProperty().addListener((o, old, val) -> applyFilters());
        sortFieldCombo.valueProperty().addListener((o, old, val) -> applyFilters());
        sortOrderCombo.valueProperty().addListener((o, old, val) -> applyFilters());

        ///

        executor = Executors.newFixedThreadPool(3);
    }

    @FXML
    protected void onLoadClicked() {
       //TODO

        stopTasks();
        allData.clear();
        filteredData.clear();
        tasks.clear();

        resetProgress();
        setStatus("Loading data with 3 threads...");

        ProgressBar[] bars = {progressBar1, progressBar2, progressBar3};
        Label[] statuses = {threadStatus1, threadStatus2, threadStatus3};

        for (int i = 0; i < 3; i++){
            final int idx = 1;

            CsvLoaderTask task = new CsvLoaderTask(FILE_PATHS[i], FILE_PATHS[i], i + 1 );
            tasks.add(task);
             bars[idx].progressProperty().bind(task.progressProperty().divide(100.0));

             task.messageProperty().addListener((o, old, msg) ->
                     Platform.runLater(() -> statuses[idx].setText(msg)));

            task.setOnSucceeded(e -> {
                List<Person> loaded = task.getValue();
                Platform.runLater(() -> {
                    allData.addAll(loaded);
                    applyFilters();
                    statuses[idx].setStyle("-fx-text-fill: green;");
                    checkIfAllDone();
                });
            });

            task.setOnFailed(e -> {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Platform.runLater(() -> {
                    statuses[idx].setText("✗ " + error);
                    statuses[idx].setStyle("-fx-text-fill: red");
                    bars[idx].progressProperty().unbind();
                    setStatus("Error in thread" + (idx + 1) + ": " + error);
                });
            });

            executor.submit(task);

        }
    }

    @FXML
    protected void onStopClicked() {
        stopTasks();
        setStatus("Stopped");
    }

    @FXML
    protected void onClearFilters() {
        searchField.clear();
        dateFrom.setValue(null);
        dateTo.setValue(null);
        sortFieldCombo.setValue("None");
        sortOrderCombo.setValue("AScending (A → Z)");
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
        resetProgress();
    }

    private void resetProgress() {
        ProgressBar[] bars     = { progressBar1, progressBar2, progressBar3 };
        Label[]       statuses = { threadStatus1, threadStatus2, threadStatus3 };
        for (int i = 0; i < 3; i++) {
            bars[i].progressProperty().unbind();
            bars[i].setProgress(0);
            statuses[i].setText("Waiting...");
            statuses[i].setStyle("");
        }
    }

    private void checkIfAllDone () {
        boolean allDone = tasks.stream().allMatch(CsvLoaderTask::isDone);
        if (allDone) {
            setStatus("Done! Loaded " + allData.size() + " records ");
            totalCountLabel.setText("Total: " + allData.size() + " records");
        }
    }

    private void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
