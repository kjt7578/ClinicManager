package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.project1.Timeslot;

public class ClinicManagerController {
    @FXML
    private Label welcomeText;

    @FXML
    private ComboBox<String> myComboBox;

    @FXML
    private ComboBox<String> display_selector;

    @FXML
    private ComboBox<String> timeslot_selection;

    @FXML
    private ComboBox<String> appointment_type;

    @FXML
    public void initialize() {
        myComboBox.getItems().addAll("X RAY", "ULTRA SOUND", "CATSCAN");
        display_selector.getItems().addAll("Display office appointments", "Display imaging appointments", "Display all appointments by date", "Display all appointments by patient", "Display all appointments by county", "Display billing statements for all patients", "Display all credit amounts for providers");
        appointment_type.getItems().addAll("Office", "Imaging");

        appointment_type.setOnAction(e -> {
            String selectedType = appointment_type.getValue();
            System.out.println("Selected: " + selectedType);
        });

        display_selector.setOnAction(e -> {
            String selectedOption = display_selector.getValue();
            System.out.println("Selected: " + selectedOption);
        });

        initializeTimeSlots();
    }

    private void initializeTimeSlots() {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        // 9:00AM ~ 11:30AM Morning slots from index 1 to 6
        addTimeSlots(timeSlots, 1, 6);
        // 2:00PM ~ 5:00PM Afternoon slots from index 7 to 12
        addTimeSlots(timeSlots, 7, 12);
        timeslot_selection.setItems(timeSlots);
    }

    private void addTimeSlots(ObservableList<String> timeSlots, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; i++) {
            Timeslot slot = Timeslot.fromString(Integer.toString(i));
            timeSlots.add(slot.toString());
        }
    }

}