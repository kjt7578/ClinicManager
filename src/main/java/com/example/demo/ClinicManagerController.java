package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;

public class ClinicManagerController {
    @FXML
    private Label welcomeText;

    @FXML
    private ComboBox<String> myComboBox;

    @FXML
    private ComboBox<String> display_selector;

    @FXML
    public void initialize() {
        myComboBox.getItems().addAll("X RAY", "ULTRA SOUND", "CATSCAN");
        display_selector.getItems().addAll("Display office appointments", "Display imaging appointments", "Display all appointments by date", "Display all appointments by patient", "Display all appointments by county", "Display billing statements for all patients", "Display all credit amounts for providers");

        display_selector.setOnAction(e -> {
            String selectedOption = display_selector.getValue();
        });
    }

}