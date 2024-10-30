package com.example.demo;

import javafx.application.Platform;
import model.project1.*;
import model.util.Doctor;
import model.util.Technician;
import model.util.Person;
import model.project1.List;
import model.project1.Appointment;
import model.project1.Patient;
import model.project1.Profile;
import model.project1.Timeslot;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ClinicManagerController {

    @FXML
    private TextField office_patient_first_name;

    @FXML
    private TextField office_patient_last_name;

    @FXML
    private DatePicker office_appointment_date;

    @FXML
    private DatePicker office_date_of_birth;

    @FXML
    private ComboBox<String> office_timeslot_selection;

    @FXML
    private ComboBox<String> office_provider_selection;

    @FXML
    private TextArea status_messages;

    @FXML
    private TextField imaging_patient_first_name;

    @FXML
    private TextField imaging_patient_last_name;

    @FXML
    private DatePicker imaging_appointment_date;

    @FXML
    private DatePicker imaging_date_of_birth;

    @FXML
    private ComboBox<String> imaging_timeslot_selection;

    @FXML
    private ComboBox<String> imaging_provider_selection;

    private ObservableList<String> providerList;
    private static final String PROVIDERS_FILE_PATH = "providers.txt";
    private List<Appointment> appointmentList;

    @FXML
    public void initialize() {
        providerList = FXCollections.observableArrayList();
        loadProviders();
        appointmentList = new List<>();
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
            office_provider_selection.setItems(providerList);
            imaging_provider_selection.setItems(providerList);
        } catch (FileNotFoundException e) {
            appendMessage("Error: " + PROVIDERS_FILE_PATH + " Cannot find providers.txt!");
        }
    }

    private Provider parseProvider(String line) {
        String[] tokens = line.split("\\s+");

        if (tokens.length < 6) {
            status_messages.appendText("Error: Invalid provider data format.\n");
            return null;
        }

        String providerType = tokens[0].toUpperCase();
        String firstName = tokens[1];
        String lastName = tokens[2];
        String[] dateParts = tokens[3].split("/");

        if (dateParts.length != 3) {
            status_messages.appendText("Error: Invalid date format.\n");
            return null;
        }

        Date dateOfBirth = new Date(
                Integer.parseInt(dateParts[2]),
                Integer.parseInt(dateParts[0]),
                Integer.parseInt(dateParts[1]));

        Location location = Location.valueOf(tokens[4].toUpperCase());

        if (providerType.equals("D")) {
            if (tokens.length < 7) {
                status_messages.appendText("Error: Invalid doctor data format.\n");
                return null;
            }
            return new Doctor(new Profile(firstName, lastName, dateOfBirth), location,
                    Specialty.valueOf(tokens[5].toUpperCase()), tokens[6]);
        } else if (providerType.equals("T")) {
            if (tokens.length < 6) {
                status_messages.appendText("Error: Invalid technician data format.\n");
                return null;
            }
            return new Technician(new Profile(firstName, lastName, dateOfBirth), location,
                    Integer.parseInt(tokens[5]));
        }
        return null;
    }

    private void initializeTimeSlots() {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        addTimeSlots(timeSlots, 1, 6); // Morning slots
        addTimeSlots(timeSlots, 7, 12); // Afternoon slots
        office_timeslot_selection.setItems(timeSlots);
        imaging_timeslot_selection.setItems(timeSlots);
    }

    private void addTimeSlots(ObservableList<String> timeSlots, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; i++) {
            Timeslot slot = Timeslot.fromString(Integer.toString(i));
            timeSlots.add(slot.toString());
        }
    }

    @FXML
    private void handleScheduleOffice() {
        scheduleAppointment("Office");
    }

    @FXML
    private void handleScheduleImaging() {
        scheduleAppointment("Imaging");
    }

    private void scheduleAppointment(String type) {
        TextField firstNameField = type.equals("Office") ? office_patient_first_name : imaging_patient_first_name;
        TextField lastNameField = type.equals("Office") ? office_patient_last_name : imaging_patient_last_name;
        DatePicker appointmentDatePicker = type.equals("Office") ? office_appointment_date : imaging_appointment_date;
        DatePicker dobPicker = type.equals("Office") ? office_date_of_birth : imaging_date_of_birth;
        ComboBox<String> timeslotSelection = type.equals("Office") ? office_timeslot_selection : imaging_timeslot_selection;
        ComboBox<String> providerSelection = type.equals("Office") ? office_provider_selection : imaging_provider_selection;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate appointmentDate = appointmentDatePicker.getValue();
        String timeslotStr = timeslotSelection.getValue();
        String providerName = providerSelection.getValue();
        LocalDate dob = dobPicker.getValue();

        String appointmentCode = type.equals("Office") ? "D" : "T";

        System.out.println(firstName + " " + lastName + " " + appointmentDate + " " + timeslotStr + " " + providerName + " " + dob);

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
                appointmentDate == null || timeslotStr == null || providerName == null || dob == null) {
            appendMessage("Fill all fields");
            System.out.println("Empty field exists");
            return;
        }

        if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, timeslotStr)) {
            appendMessage("Appointment already exists.");
            System.out.println("Appointment already exists.");
            return;
        }

        try {
            createNewAppointment(firstName, lastName, dob, appointmentDate, timeslotStr, providerName, appointmentCode);
            Platform.runLater(() -> {
                appendMessage(firstName + " " + appointmentDate + " Success.");
            });
            System.out.println("Success");
            clearFields(type);
        } catch (Exception e) {
            appendMessage("Error.");
            System.out.println("Error");
        }
        System.out.println("handleSchedule Done");
    }

    private boolean isDuplicateAppointment(String firstName, String lastName, LocalDate dob, LocalDate appointmentDate, String timeslot) {
        for (int i = 0; i < appointmentList.size(); i++) {
            Appointment appointment = appointmentList.get(i);
            Person person = appointment.getPatient();

            if (person instanceof Patient) {
                Patient patient = (Patient) person;

                if (appointment.getDate().equals(appointmentDate) &&
                        appointment.getTimeslot().toString().equals(timeslot) &&
                        patient.getProfile().getFname().equalsIgnoreCase(firstName) &&
                        patient.getProfile().getLname().equalsIgnoreCase(lastName) &&
                        patient.getProfile().getDob().equals(dob)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createNewAppointment(String firstName, String lastName, LocalDate dob, LocalDate appointmentDate, String timeslot, String provider, String appointmentTypeCode) {
        try {
            Date dateOfBirth = new Date(dob.getYear(), dob.getMonthValue(), dob.getDayOfMonth());
            Date appointmentDateConverted = new Date(appointmentDate.getYear(), appointmentDate.getMonthValue(), appointmentDate.getDayOfMonth());
            Patient newPatient = new Patient(new Profile(firstName, lastName, dateOfBirth));

            Doctor selectedDoctor = findDoctorByName(provider);
            if (selectedDoctor == null) {
                appendMessage("Doctor does not exist.");
                return;
            }

            String npi = selectedDoctor.getNpi();  // Fetch NPI from doctor
            Appointment newAppointment = new Appointment(appointmentDateConverted, Timeslot.fromString(timeslot), newPatient, selectedDoctor);
            appointmentList.add(newAppointment);

            appendMessage("Appointment successfully created: " + appointmentTypeCode + "," + appointmentDate + "," + timeslot + "," + firstName + "," + lastName + "," + dob + "," + npi);
        } catch (Exception e) {
            e.printStackTrace();
            appendMessage("Error while creating appointment.");
        }
    }

    private Doctor findDoctorByName(String providerName) {
        String[] providerParts = providerName.split(" ");
        if (providerParts.length < 2) {
            return null;
        }

        String firstName = providerParts[0];
        String lastName = providerParts[1];

        for (String providerInfo : providerList) {
            String[] providerDetails = providerInfo.split(" ");
            if (providerDetails.length < 2) {
                continue;
            }

            String providerFirstName = providerDetails[0];
            String providerLastName = providerDetails[1];

            if (firstName.equalsIgnoreCase(providerFirstName) && lastName.equalsIgnoreCase(providerLastName)) {
                for (int i = 0; i < providerList.size(); i++) {
                    Provider provider = parseProvider(providerList.get(i));
                    if (provider instanceof Doctor) {
                        return (Doctor) provider;
                    }
                }
            }
        }

        return null;
    }

    private void clearFields(String type) {
        if (type.equals("Office")) {
            office_patient_first_name.clear();
            office_patient_last_name.clear();
            office_appointment_date.setValue(null);
            office_timeslot_selection.setValue(null);
            office_provider_selection.setValue(null);
        } else {
            imaging_patient_first_name.clear();
            imaging_patient_last_name.clear();
            imaging_appointment_date.setValue(null);
            imaging_timeslot_selection.setValue(null);
            imaging_provider_selection.setValue(null);
        }
    }

    private void appendMessage(String message) {
        Platform.runLater(() -> {
            status_messages.appendText(message + "\n");
        });
    }
}
