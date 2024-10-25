package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import model.project1.*;
import model.util.Doctor;
import model.util.Technician;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ClinicManagerController {
    @FXML
    private Label welcomeText;

    @FXML
    private ComboBox<String> display_selector;

    @FXML
    private ComboBox<String> timeslot_selection;

    @FXML
    private ComboBox<String> appointment_type;

    @FXML
    private ComboBox<String> provider_selection;

    @FXML
    private TextArea status_messages;

    private ObservableList<String> providerList;
    private static final String PROVIDERS_FILE_PATH = "providers.txt";

    @FXML
    public void initialize() {
        providerList = FXCollections.observableArrayList();
        loadProviders();

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

    private void loadProviders() {
        try (Scanner scanner = new Scanner(new File(PROVIDERS_FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Provider provider = parseProvider(line);
                    if (provider != null) {
                        providerList.add(provider.getProfile().getFname() + " " + provider.getProfile().getLname()
                                + " (" + provider.getLocation().name() + ")");
                    }
                }
            }
            provider_selection.setItems(providerList);
            displayProviders();
        } catch (FileNotFoundException e) {
            status_messages.setText("Error: " + PROVIDERS_FILE_PATH + " Cannot find providers.txt!");
        }
    }

    private Provider parseProvider(String line) {
        String[] tokens = line.split("\\s+");
        String providerType = tokens[0].toUpperCase();
        String firstName = tokens[1];
        String lastName = tokens[2];
        String[] dateParts = tokens[3].split("/");
        Date dateOfBirth = new Date(
                Integer.parseInt(dateParts[2]),
                Integer.parseInt(dateParts[0]),
                Integer.parseInt(dateParts[1]));
        Location location = Location.valueOf(tokens[4].toUpperCase());

        if (providerType.equals("D")) {
            return new Doctor(new Profile(firstName, lastName, dateOfBirth), location,
                    Specialty.valueOf(tokens[5].toUpperCase()), tokens[6]);
        } else if (providerType.equals("T")) {
            return new Technician(new Profile(firstName, lastName, dateOfBirth), location,
                    Integer.parseInt(tokens[5]));
        }
        return null;
    }

    private void displayProviders() {
        StringBuilder sb = new StringBuilder("Providers loaded:\n");
        for (String provider : providerList) {
            sb.append(provider).append("\n");
        }
        status_messages.setText(sb.toString());
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