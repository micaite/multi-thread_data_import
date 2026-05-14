module org.example.thread_data_import {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.compiler;


    opens org.example.thread_data_import to javafx.fxml;
    exports org.example.thread_data_import;
}