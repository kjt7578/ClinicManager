package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;

public class ClinicManagerController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private ComboBox<String> myComboBox;

    @FXML
    public void initialize() {
        myComboBox.getItems().addAll("X RAY", "ULTRA SOUND", "CATSCAN");
    }
}