package org.example.thread_data_import;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onLoadClicked() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onStopClicked() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onClearFilters() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
