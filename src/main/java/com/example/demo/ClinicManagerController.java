package com.example.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import model.project1.*;
import model.util.*;
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
import java.io.FilterOutputStream;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Scanner;


public class ClinicManagerController {

    /**
     * List of providers in the clinic.
     */
    private List<Provider> providerList;

    /**
     * List of appointments scheduled in the clinic.
     */
    private List<Appointment> appointmentList;

    /**
     * List of technicians in the rotation schedule.
     */
    private List<Technician> technicianRotationList;

    /**
     * Maximum number of timeslots available.
     */
    private static final int MAX_TIMESLOTS = 16;

    /**
     * Initial index for technician rotation.
     */
    private static final int INITIAL_ROTATION_INDEX = 0;

    /**
     * Expected token length for appointment commands.
     */
    private static final int TOKEN_LENGTH_APPOINTMENT = 7;

    /**
     * Expected token length for cancellation commands.
     */
    private static final int TOKEN_LENGTH_CANCEL = 6;

    /**
     * Maximum number of months ahead for scheduling.
     */
    private static final int MAX_MONTHS_AHEAD = 6;

    /**
     * Index for month part in date array.
     */
    private static final int DATE_PART_MONTH = 0;

    /**
     * Index for day part in date array.
     */
    private static final int DATE_PART_DAY = 1;

    /**
     * Index for year part in date array.
     */
    private static final int DATE_PART_YEAR = 2;

    /**
     * Current index in the technician rotation.
     */
    private int technicianRotationIndex;

    /**
     * Matrix to track technician assignments to timeslots.
     */
    private boolean[][] technicianAssigned;

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
    private TextArea imaging_status_messages;

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
    private ComboBox<String> imaging_service;

    @FXML
    private ComboBox<String> display_selector;

    @FXML
    public TextArea display_text_area;

    @FXML
    private TextField cancel_patient_first_name;

    @FXML
    private TextField cancel_patient_last_name;

    @FXML
    private DatePicker cancel_appointment_date;

    @FXML
    private DatePicker cancel_date_of_birth;

    @FXML
    private ComboBox<String> cancel_timeslot_selection;

    @FXML
    private TextField re_patient_first_name;

    @FXML
    private TextField re_patient_last_name;

    @FXML
    private DatePicker re_appointment_date;

    @FXML
    private DatePicker re_date_of_birth;

    @FXML
    private ComboBox<String> re_timeslot_selection;

    @FXML
    private ComboBox<String> re_newtimeslot_selection;

    @FXML
    private TextArea re_status_messages;

    private ObservableList<Provider> OBSdoctorList = FXCollections.observableArrayList();
    private ObservableList<Provider> OBStechnicianList = FXCollections.observableArrayList();
    private ObservableList<String> OBSproviderList;
    private static final String PROVIDERS_FILE_PATH = "providers.txt";

    /**
     * Initializes the Clinic Manager Controller by setting up lists, loading providers,
     * and initializing UI components. This method is called when the controller is
     * first created.
     */
    @FXML
    public void initialize() {
        providerList = new List<>();
        appointmentList = new List<>();
        technicianRotationList = new List<>();
        technicianRotationIndex = INITIAL_ROTATION_INDEX;

        OBSproviderList = FXCollections.observableArrayList();
        loadProviders();
        int MAX_TECHNICIANS = technicianRotationList.size();
        technicianAssigned = new boolean[MAX_TIMESLOTS][MAX_TECHNICIANS];

        printTechnicianRotation();
        appointmentList = new List<>();
        initializeTimeSlots();
        initializeDisplayOptions();
        Sort.setController(this);

        ObservableList<String> displayOptions = FXCollections.observableArrayList(
                "PA: Sort by Appointment Date",
                "PP: Sort by Patient",
                "PL: Sort by County",
                "PS: Display Billing by Specialty",
                "PO: Sort Office Appointments by County",
                "PI: Sort Imaging Appointments by County",
                "PC: Display Credit by Provider"
        );
        display_selector.setItems(displayOptions);
        ObservableList<String> imagingServices = FXCollections.observableArrayList("XRAY", "CATSCAN", "ULTRASOUND");
        imaging_service.setItems(imagingServices);
    }

    /**
     * Loads provider information from a file and populates the lists for doctors and technicians.
     * It also updates the UI with the names of the providers and initializes the technician rotation.
     */
    @FXML
    private void loadProviders() {
        try (Scanner scanner = new Scanner(new File(PROVIDERS_FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Provider provider = parseProvider(line);
                    if (provider instanceof Doctor) {
                        OBSdoctorList.add(provider);
                    } else if (provider instanceof Technician) {
                        OBStechnicianList.add(provider);
                    }
                    if (provider != null) {
                        providerList.add(provider);
                    }
                }
            }

            ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (Provider doctor : OBSdoctorList) {
                doctorNames.add(doctor.getProfile().getFname() + " " + doctor.getProfile().getLname() + " (" + doctor.getLocation().name() + ")");
            }
            office_provider_selection.setItems(doctorNames);

            ObservableList<String> technicianNames = FXCollections.observableArrayList();
            for (Provider technician : OBStechnicianList) {
                technicianNames.add(technician.getProfile().getFname() + " " + technician.getProfile().getLname() + " (" + technician.getLocation().name() + ")");
            }

            createTechnicianRotation();
            Sort.provider(providerList);
            displayProviders();

        } catch (FileNotFoundException e) {
            appendMessage("Error: " + PROVIDERS_FILE_PATH + " cannot be found.");
        }
    }

    /**
     * Parses a provider's information from a given line of text.
     * Returns a Provider object based on the parsed information or null if the provider type is unrecognized.
     *
     * @param line the line of text containing provider information
     * @return a Provider object (Doctor or Technician) or null if the type is unrecognized
     */
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

    /**
     * Initializes the available time slots for different appointment types by populating
     * a list of time slots and setting it in the corresponding ComboBoxes in the UI.
     */
    private void initializeTimeSlots() {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        addTimeSlots(timeSlots, 1, 6); // Morning slots
        addTimeSlots(timeSlots, 7, 12); // Afternoon slots
        office_timeslot_selection.setItems(timeSlots);
        imaging_timeslot_selection.setItems(timeSlots);
        cancel_timeslot_selection.setItems(timeSlots);
        re_timeslot_selection.setItems(timeSlots);
        re_newtimeslot_selection.setItems(timeSlots);
    }

    /**
     * Adds time slots to the given list based on a specified start and end range.
     * Converts each slot number within the range to a Timeslot object and adds its
     * string representation to the list.
     *
     * @param timeSlots the list to which time slots will be added
     * @param startSlot the starting slot number
     * @param endSlot the ending slot number
     */
    private void addTimeSlots(ObservableList<String> timeSlots, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; i++) {
            Timeslot slot = Timeslot.fromString(Integer.toString(i));
            timeSlots.add(slot.toString());
        }
    }

    /**
     * Handles the scheduling of an office appointment by calling the
     * scheduleAppointment method with the type "Office".
     */
    @FXML
    private void handleScheduleOffice() {
        scheduleAppointment("Office");
    }

    /**
     * Handles the scheduling of an imaging appointment by calling the
     * scheduleAppointment method with the type "Imaging".
     */
    @FXML
    private void handleScheduleImaging() {
        scheduleAppointment("Imaging");
    }

    /**
     * Schedules an appointment by gathering input data, validating fields,
     * checking for duplicates, and verifying provider availability.
     *
     * @param type the type of appointment ("Office" or "Imaging")
     */
    private void scheduleAppointment(String type) {
        TextField firstNameField = office_patient_first_name;
        TextField lastNameField = office_patient_last_name;
        DatePicker appointmentDatePicker = office_appointment_date;
        DatePicker dobPicker =  office_date_of_birth;
        ComboBox<String> timeslotSelection = office_timeslot_selection;
        ComboBox<String> providerSelection = office_provider_selection;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate appointmentDateLocal = appointmentDatePicker.getValue();
        String timeslotStr = timeslotSelection.getValue();
        String providerName = providerSelection.getValue();
        LocalDate dobLocal = dobPicker.getValue();

        if (providerName != null) {
            providerName = providerName.split(" \\(")[0];
        }
        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        System.out.println("D," + appointmentDate + "," + convertTimeToSlot(timeslotStr) + "," +
                firstName + "," + lastName + "," + dob + "," + convertProvicerToSNPI(providerName));

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
                appointmentDate == null || timeslotStr == null || providerName == null || dob == null) {
            appendMessage("Fill all fields");
            System.out.println("Empty field exists");
            return;
        }

        try {
            appointmentDate = validateAppointmentDate(String.valueOf(appointmentDate));
            Timeslot timeslot = validateTimeslot(convertTimeToSlot(timeslotStr));
            dob = validateDateOfBirth(String.valueOf(dob));
            if (!validateInputs(appointmentDate, timeslot, dob)) return;
            Doctor doctor = getDoctorByNPI(convertProvicerToSNPI(providerName));
            if (doctor == null) return;

            // Check for duplicate appointment
            if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, timeslot)) {
                System.out.printf("%s %s %s has an existing appointment at the same time slot.%n",
                        firstName, lastName, dob);
                return;  // Stop further processing since it's a duplicate
            }

            // Check if the doctor is unavailable at the given time
            if (isDoctorUnavailable(doctor, appointmentDate, timeslot)) {
                System.out.printf("[%s %s %s, %s[%s, #%s] is not available at slot %s %n",
                        doctor.getProfile().getFname(), doctor.getProfile().getLname(), doctor.getDateOfBirth(),
                        doctor.getLocation(), doctor.getSpecialty(), doctor.getNpi(), timeslot.getSlotIndex());
                return;
            }

            // Create the new appointment and add it to the appointment list
            createNewAppointment(firstName, lastName, dob, appointmentDate, timeslot, doctor);

        } catch (Exception e) {
            System.out.println("Error: Invalid office appointment command.");
        }
    }

    /**
     * Processes the cancellation of an appointment by gathering input data,
     * validating fields, and finding the matching appointment in the list.
     */
    //C,2/3/2025,4,john,doe,12/13/1989
    @FXML
    private void processCancellation() {
        String firstName = cancel_patient_first_name.getText();
        String lastName = cancel_patient_last_name.getText();
        LocalDate appointmentDateLocal = cancel_appointment_date.getValue();
        LocalDate dobLocal = cancel_date_of_birth.getValue();
        String timeslotStr = cancel_timeslot_selection.getValue();
        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);
        System.out.println("C," + appointmentDate + "," + convertTimeToSlot(timeslotStr) + "," +
                firstName + "," + lastName + "," + dob);

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
                appointmentDate == null || timeslotStr == null || dob == null) {
            appendMessage("Fill all fields");
            System.out.println("Empty field exists");
            return;
        }

        try {
            appointmentDate = validateAppointmentDate(String.valueOf(appointmentDate));
            Timeslot timeslot = validateTimeslot(convertTimeToSlot(timeslotStr));
            dob = validateDateOfBirth(String.valueOf(dob));
            if (!validateInputs(appointmentDate, timeslot, dob)) return;
            Appointment appointmentToCancel = findAppointment(appointmentDate, timeslot, firstName.toLowerCase(), lastName.toLowerCase(), dob);

            if (appointmentToCancel != null) {
                appointmentList.remove(appointmentToCancel);
                System.out.println(appointmentDate + " " + timeslot + " " + firstName + " " + lastName + " " + dob + " - appointment has been canceled.");
            } else {
                System.out.println(appointmentDate + " " + timeslot + " " + firstName + " " + lastName + " " + dob + " - appointment does not exist.");
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid cancellation command.");
        }
    }

    /**
     * Converts a LocalDate object to a Date object.
     *
     * @param localDate the LocalDate to convert
     * @return the converted Date object, or null if localDate is null
     */
    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return new Date(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }

    /**
     * Converts a provider's name to their corresponding NPI.
     *
     * @param providerName the name of the provider to convert
     * @return the NPI as a string, or null if the provider name is not recognized
     */
    private String convertProvicerToSNPI(String providerName) {
        switch (providerName) {
            case "ANDREW PATEL":
                return "01";
            case "RACHAEL LIM":
                return "23";
            case "MONICA ZIMNES":
                return "11";
            case "JOHN HARPER":
                return "32";
            case "TOM KAUR":
                return "54";
            case "ERIC TAYLOR":
                return "91";
            case "BEN RAMESH":
                return "39";
            case "JUSTIN CERAVOLO":
                return "09";
            case "GARY JOHNSON":
                return "85";
            case "BEN JERRY":
                return "77";
            case "FRANK LIN":
                return "120";
            case "CHARLES BROWN":
                return "100";
            case "MONICA FOX":
                return "130";
            case "JENNY PATEL":
                return "125";
            default:
                return null;
        }
    }

    /**
     * Converts a given timeslot string (in "HH:MM AM/PM" format) to a corresponding
     * slot number as a string.
     *
     * @param timeslot the timeslot string to convert
     * @return the corresponding slot number as a string, or null if the timeslot is unrecognized
     */
    private String convertTimeToSlot(String timeslot) {
        if (timeslot == null) {
            System.out.println("Error: timeslot is null.");
            return null;
        }
        switch (timeslot) {
            case "9:00 AM":
                return "1";
            case "9:30 AM":
                return "2";
            case "10:00 AM":
                return "3";
            case "10:30 AM":
                return "4";
            case "11:00 AM":
                return "5";
            case "11:30 AM":
                return "6";
            case "2:00 PM":
                return "7";
            case "2:30 PM":
                return "8";
            case "3:00 PM":
                return "9";
            case "3:30 PM":
                return "10";
            case "4:00 PM":
                return "11";
            case "4:30 PM":
                return "12";
            default:
                return null;
        }
    }

    /**
     * Clears input fields in the UI based on the specified appointment type.
     * Resets all relevant fields to their default or empty states.
     *
     * @param type the type of appointment ("Office" or "Imaging")
     */
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
        }
    }

    /**
     * Appends a message to the status messages TextArea in the UI.
     *
     * @param message the message to append to the status messages
     */
    private void appendMessage(String message) {
        Platform.runLater(() -> {
            status_messages.appendText(message + "\n");
        });
    }

    /**
     * Initializes the display options for the UI by creating a list of options
     * and setting it to the display_selector ComboBox.
     */
    private void initializeDisplayOptions() {
        ObservableList<String> displayOptions = FXCollections.observableArrayList();
        displayOptions.add("Show Appointments");
        display_selector.setItems(displayOptions);

        display_selector.setOnAction(event -> handleDisplaySelection());
    }

    /**
     * Processes commands related to sorting appointments.
     *
     * @param appointments the list of appointments to manage
     * @param command      the command string containing the sorting operation to perform
     */
    private void processSortingCommand(List<Appointment> appointments, String command) {
        System.out.println(command);
        switch (command) {
            case "PA": // Sort by appointment date, time, then provider's last name
                Sort.appointment(appointments, 'A');
                break;
            case "PP": // Sort by patient (last name, first name, date of birth, appointment date, time)
                Sort.appointment(appointments, 'P');
                break;
            case "PL": // Sort by county name, appointment date, time
                Sort.appointment(appointments, 'L');
                break;
            case "PS": // Display billing statements based on provider's specialty
                Sort.appointment(appointments, 'S');
                break;
            case "PO": // Sort office appointments by county name, date, time
                Sort.appointment(appointments, 'O');
                break;
            case "PI": // Sort imaging appointments by county name, date, time
                Sort.appointment(appointments, 'I');
                break;
            case "PC": // Display expected credit amounts for providers, sorted by provider profile
                Sort.appointment(appointments, 'C');
                break;
            default:
                System.out.println("Invalid command!");
                break;
        }
    }

    /**
     * Handles the selection of a display option from the display_selector ComboBox.
     * Depending on the selected option, it processes the corresponding command
     * to sort or display appointments.
     */
    @FXML
    private void handleDisplaySelection() {
        String selectedOption = display_selector.getValue();
        String command = "";

        if (appointmentList.size() == 0) {
            display_text_area.setText("No appointments to display.");
            return;
        }
        if (selectedOption != null) {
            switch (selectedOption) {
                case "PA: Sort by Appointment Date":
                    command = "PA";
                    break;
                case "PP: Sort by Patient":
                    command = "PP";
                    break;
                case "PL: Sort by County":
                    command = "PL";
                    break;
                case "PS: Display Billing by Specialty":
                    command = "PS";
                    break;
                case "PO: Sort Office Appointments by County":
                    command = "PO";
                    break;
                case "PI: Sort Imaging Appointments by County":
                    command = "PI";
                    break;
                case "PC: Display Credit by Provider":
                    command = "PC";
                    break;
                default:
                    appendToDisplayTextArea("Invalid display option selected.");
                    return;
            }

            processSortingCommand(appointmentList, command);
            System.out.println("Command: " + command);
            displayAppointments();
        } else {
            appendToDisplayTextArea("Please select a display option.");
        }
    }

    /**
     * Displays the list of appointments in the display_text_area.
     */
    private void displayAppointments() {
        if (appointmentList.size() == 0) {
            display_text_area.setText("No appointments to display.");
            return;
        }

        StringBuilder displayText = new StringBuilder();
        for (int i = 0; i < appointmentList.size(); i++) {
            Appointment appointment = appointmentList.get(i);
            displayText.append(appointment.toString()).append("\n");
        }
        display_text_area.setText(displayText.toString());
    }

    /**
     * Creates a rotation list of technicians by iterating through the provider list.
     * This method adds each provider that is an instance of Technician to the technician rotation list.
     */
    private void createTechnicianRotation() {
        for (int i = providerList.size() - 1; i >= 0; i--) {
            Provider provider = providerList.get(i);
            if (provider instanceof Technician) {
                technicianRotationList.add((Technician) provider);
            }
        }
    }

    /**
     * Prints the technician rotation list to the console.
     * If the list is empty, a message is displayed; otherwise,
     * the technicians' names and locations are printed.
     */
    private void printTechnicianRotation(){
        if (technicianRotationList.isEmpty()) {
            System.out.println("No technicians found for rotation.");
        } else {
            System.out.println("\nRotation list for the technicians:");
            for (int i = 0; i < technicianRotationList.size(); i++) {
                Technician technician = technicianRotationList.get(i);
                System.out.print(technician.getProfile().getFname() + " " + technician.getProfile().getLname()
                        + " (" + technician.getLocation().name() + ")");
                if (i < technicianRotationList.size() - 1) {
                    System.out.print(" --> ");
                }
            }
            System.out.println();
        }
    }

    /**
     * Displays the list of providers loaded in the system, including their
     * profiles, locations, and additional information specific to their type.
     */
    private void displayProviders() {
        System.out.println("Providers loaded to the list.");

        for (Provider provider : providerList) {
            System.out.print("[" + provider.getProfile().toString() + ", ");
            System.out.print(provider.getLocation().toString());

            if (provider instanceof Doctor doctor) {
                System.out.print("[" + doctor.getSpecialty() + ", #" + doctor.getNpi());
            } else if (provider instanceof Technician technician) {
                System.out.print("[rate: $" + String.format("%.2f", (double) technician.getRatePerVisit()));
            }
            System.out.println("]");
        }
    }

    /**
     * Processes a command by delegating to either appointment handling or sorting functions.
     *
     * @param appointments the list of appointments to manage
     * @param command      the command string to process
     */
    private void processCommand(List<Appointment> appointments, String command) {
        if (command.startsWith("D") || command.startsWith("T") || command.startsWith("C") || command.startsWith("R")) {
            processAppointmentCommand(appointments, command);
        } else {
            processSortingCommand(appointments, command);
        }
    }

    /**
     * Processes commands related to appointment management (add, cancel, reschedule).
     *
     * @param appointments the list of appointments to manage
     * @param command      the command string containing the operation to perform
     */
    private void processAppointmentCommand(List<Appointment> appointments, String command) {
        String[] tokens = command.split(",");
        switch (tokens[0]) {
            case "D":
                processOfficeAppointment(tokens);
                break;
            case "T":
                //processImagingAppointment(tokens);
                break;
            case "C":
                //processCancellation(tokens);
                break;
            case "R":
                //processReschedule(tokens);
                break;
            default:
                System.out.println("Invalid appointment command!");
                break;
        }
    }

    /**
     * Processes a command to create a new office appointment using the provided tokens.
     * Validates the input data, checks for duplicates and doctor availability,
     * and adds the appointment to the list if all conditions are met.
     *
     * @param tokens the array of tokens containing appointment details
     */
    private void processOfficeAppointment(String[] tokens) {
        if (!validateTokenLength(tokens)) return;

        try {
            Date appointmentDate = validateAppointmentDate(tokens[1]);
            Timeslot timeslot = validateTimeslot(tokens[2]);
            Date dob = validateDateOfBirth(tokens[5]);
            if (!validateInputs(appointmentDate, timeslot, dob)) return;

            String firstName = tokens[3];
            String lastName = tokens[4];
            Doctor doctor = getDoctorByNPI(tokens[6]);
            if (doctor == null) return;

            // Check for duplicate appointment
            if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, timeslot)) {
                System.out.printf("%s %s %s has an existing appointment at the same time slot.%n",
                        firstName, lastName, dob);
                return;  // Stop further processing since it's a duplicate
            }

            // Check if the doctor is unavailable at the given time
            if (isDoctorUnavailable(doctor, appointmentDate, timeslot)) {
                System.out.printf("[%s %s %s, %s[%s, #%s] is not available at slot %s %n",
                        doctor.getProfile().getFname(), doctor.getProfile().getLname(), doctor.getDateOfBirth(),
                        doctor.getLocation(), doctor.getSpecialty(), doctor.getNpi(), timeslot.getSlotIndex());
                return;
            }

            // Create the new appointment and add it to the appointment list
            createNewAppointment(firstName, lastName, dob, appointmentDate, timeslot, doctor);
        } catch (Exception e) {
            System.out.println("Error: Invalid office appointment command.");
        }
    }

    /**
     * Validates the length of the input tokens.
     *
     * @param tokens the array of tokens containing appointment details
     * @return true if the length is valid, false otherwise
     */
    private boolean validateTokenLength(String[] tokens) {
        if (tokens.length != TOKEN_LENGTH_APPOINTMENT) {
            System.out.println("Missing data tokens.");
            return false;
        }
        return true;
    }

    /**
     * Validates the inputs for appointment date, timeslot, and date of birth.
     *
     * @param appointmentDate the date of the appointment
     * @param timeslot the timeslot of the appointment
     * @param dob the patient's date of birth
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs(Date appointmentDate, Timeslot timeslot, Date dob) {
        return appointmentDate != null && timeslot != null && dob != null;
    }

    /**
     * Retrieves a Doctor object based on the provided NPI.
     *
     * @param npiStr the NPI string of the doctor
     * @return the Doctor object if found, null if not found or invalid NPI
     */
    private Doctor getDoctorByNPI(String npiStr) {
        try {
            int npi = Integer.parseInt(npiStr);
            Doctor doctor = findDoctorByNPI(npi);
            if (doctor == null) {
                System.out.println(npiStr + " - provider doesn't exist.");
            }
            return doctor;
        } catch (NumberFormatException e) {
            System.out.println(npiStr + " - provider doesn't exist.");
            return null;
        }
    }

    /**
     * Creates a new appointment and adds it to the appointment list.
     *
     * @param firstName the patient's first name
     * @param lastName the patient's last name
     * @param dob the patient's date of birth
     * @param appointmentDate the appointment date
     * @param timeslot the timeslot for the appointment
     * @param doctor the doctor assigned to the appointment
     */
    private void createNewAppointment(String firstName, String lastName, Date dob, Date appointmentDate, Timeslot timeslot, Doctor doctor) {
        Appointment newAppointment = new Appointment(appointmentDate, timeslot, new Patient(new Profile(firstName, lastName, dob)), doctor);
        appointmentList.add(newAppointment);

        appendToOfficeTextArea(appointmentDate, timeslot, firstName, lastName, dob, doctor.getProfile().getFname(), doctor.getProfile().getLname(),
                doctor.getProfile().getDob(), doctor.getLocation(), doctor.getSpecialty(), doctor.getNpi());
        // Prints in terminal
        System.out.printf("%s %s %s %s %s [%s %s %s, %s[%s, #%s] booked.%n",
                appointmentDate, timeslot, firstName, lastName, dob, doctor.getProfile().getFname(), doctor.getProfile().getLname(),
                doctor.getProfile().getDob(), doctor.getLocation(), doctor.getSpecialty(), doctor.getNpi());
    }

    /**
     * Checks if the specified doctor is unavailable for the given appointment date and timeslot.
     * An existing appointment for the same doctor at the specified date and timeslot indicates unavailability.
     *
     * @param doctor the doctor to check for availability
     * @param appointmentDate the date of the appointment
     * @param timeslot the timeslot of the appointment
     * @return true if the doctor is unavailable; false otherwise
     */
    private boolean isDoctorUnavailable(Doctor doctor, Date appointmentDate, Timeslot timeslot) {
        for (Appointment appointment : appointmentList) {
            if (appointment.getProvider() instanceof Doctor) {
                Doctor existingDoctor = (Doctor) appointment.getProvider();
                if (existingDoctor.equals(doctor) &&
                        appointment.getDate().equals(appointmentDate) &&
                        appointment.getTimeslot().equals(timeslot)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if an appointment with the same patient, date, and timeslot already exists.
     * A duplicate appointment is identified by matching the patient's name, date of birth,
     * appointment date, and timeslot.
     *
     * @param firstName the first name of the patient
     * @param lastName the last name of the patient
     * @param dob the date of birth of the patient
     * @param appointmentDate the date of the appointment
     * @param timeslot the timeslot of the appointment
     * @return true if a duplicate appointment exists; false otherwise
     */
    private boolean isDuplicateAppointment(String firstName, String lastName, Date dob, Date appointmentDate, Timeslot timeslot) {
        for (Appointment appointment : appointmentList) {
            if (appointment.getPatient() instanceof Patient) {
                Patient patient = (Patient) appointment.getPatient();
                if (appointment.getDate().equals(appointmentDate) &&
                        appointment.getTimeslot().equals(timeslot) &&
                        patient.getProfile().getFname().equalsIgnoreCase(firstName) &&
                        patient.getProfile().getLname().equalsIgnoreCase(lastName) &&
                        patient.getProfile().getDob().equals(dob)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Processes the imaging appointment based on the user's input from the UI.
     *
     * @param actionEvent The ActionEvent triggered by the imaging appointment button.
     */
    @FXML
    private void processImagingAppointment(ActionEvent actionEvent) {
        TextField firstNameField = imaging_patient_first_name;
        TextField lastNameField = imaging_patient_last_name;
        DatePicker appointmentDatePicker = imaging_appointment_date;
        DatePicker dobPicker =  imaging_date_of_birth;
        ComboBox<String> timeslotSelection = imaging_timeslot_selection;
        ComboBox<String> imagingSelection = imaging_service;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate appointmentDateLocal = appointmentDatePicker.getValue();
        String timeslotStr = timeslotSelection.getValue();
        String imagingService = imagingSelection.getValue();
        LocalDate dobLocal = dobPicker.getValue();

        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        System.out.println("T," + appointmentDate + "," + convertTimeToSlot(timeslotStr) + "," +
                firstName + "," + lastName + "," + dob + "," + imagingService);

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
                appointmentDateLocal == null || dobLocal == null || timeslotStr == null || imagingService == null) {
            appendMessage("Fill all fields");
            System.out.println("Empty field exists");
            return;
        }

        try {
            Timeslot timeslot = validateTimeslot(convertTimeToSlot(timeslotStr));

            if (!validateInputs(appointmentDate, timeslot, dob)) return;
            handleImagingAppointment(firstName,lastName, appointmentDate, timeslot, dob, imagingService);
        } catch (Exception e) {
            System.out.println("Error: Invalid imaging appointment command.");
        }
    }

    /**
     * Validates the imaging service.
     *
     * @param imagingService the imaging service to validate
     * @return true if the imaging service is valid, false otherwise
     */
    private boolean validateImagingService(String imagingService) {
        if (!isValidImagingService(imagingService)) {
            System.out.printf("%s - imaging service not provided.%n", imagingService.toLowerCase());
            return false;
        }
        return true;
    }

    private void handleImagingAppointment(String firstName, String lastName, Date appointmentDate, Timeslot timeslot, Date dob, String imagingService) {
        Radiology room = Radiology.valueOf(imagingService);
        if (isDuplicateImagingAppointment(firstName, lastName, dob, appointmentDate, timeslot)) {
            System.out.printf("%s %s %s has an existing appointment at the same time slot.%n", firstName, lastName, dob);
            return;
        }
        Technician technician = findAvailableTechnician(timeslot, room);
        if (technician == null) {
            System.out.printf("Cannot find an available technician at all locations for %s at slot %d.%n", room, timeslot.getSlotIndex());
            return;
        }

        createNewImagingAppointment(firstName, lastName, dob, appointmentDate, timeslot, technician, room, imagingService);
    }

    /**
     * Creates a new imaging appointment and adds it to the appointment list.
     *
     * @param firstName the patient's first name
     * @param lastName the patient's last name
     * @param dob the patient's date of birth
     * @param appointmentDate the appointment date
     * @param timeslot the timeslot for the appointment
     * @param technician the technician assigned to the appointment
     * @param room the radiology room for the appointment
     * @param imagingService the imaging service for the appointment
     */
    private void createNewImagingAppointment(String firstName, String lastName, Date dob, Date appointmentDate,
                                             Timeslot timeslot, Technician technician, Radiology room, String imagingService) {
        Imaging newImaging = new Imaging(appointmentDate, timeslot, new Patient(new Profile(firstName, lastName, dob)), technician, room);
        appointmentList.add(newImaging);


        appendToImagingTextArea(appointmentDate, timeslot, firstName, lastName, dob, technician.getProfile().getFname(), technician.getProfile().getLname(),
                technician.getProfile().getDob(), technician.getLocation(), (double) technician.getRatePerVisit(), imagingService);

        System.out.printf("%s %s %s %s %s [%s %s %s, %s][rate: $%.2f][%s] booked.%n",
                appointmentDate.toString(), timeslot.toString(), firstName, lastName, dob, technician.getProfile().getFname(),
                technician.getProfile().getLname(), technician.getProfile().getDob(), technician.getLocation(), (double) technician.getRatePerVisit(), imagingService);
    }


    /**
     * Checks if the provided imaging service is valid.
     *
     * @param service the name of the imaging service to validate
     * @return true if the service is valid (either uppercase or lowercase), false otherwise
     */
    private boolean isValidImagingService(String service) {
        return service.equals("XRAY") || service.equals("ULTRASOUND") || service.equals("CATSCAN") ||
                service.equals("xray") || service.equals("ultrasound") || service.equals("catscan");
    }

    /**
     * Checks if an imaging appointment with the same patient details and time already exists.
     *
     * @param firstName     the first name of the patient
     * @param lastName      the last name of the patient
     * @param dob           the date of birth of the patient
     * @param appointmentDate the date of the new appointment
     * @param timeslot      the timeslot of the new appointment
     * @return true if a duplicate imaging appointment exists, false otherwise
     */
    private boolean isDuplicateImagingAppointment(String firstName, String lastName, Date dob, Date appointmentDate, Timeslot timeslot) {
        for (Appointment appointment : appointmentList) {
            if (appointment instanceof Imaging) {
                Imaging imagingAppointment = (Imaging) appointment;
                Person person = imagingAppointment.getPatient();

                if (person instanceof Patient patient) {
                    if (imagingAppointment.getDate().equals(appointmentDate) &&
                            imagingAppointment.getTimeslot().equals(timeslot) &&
                            patient.getProfile().getFname().equalsIgnoreCase(firstName) &&
                            patient.getProfile().getLname().equalsIgnoreCase(lastName) &&
                            patient.getProfile().getDob().equals(dob)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Processes the rescheduling of an existing appointment based on user input from the UI.
     *
     * @param actionEvent The ActionEvent triggered by the reschedule button.
     */
    @FXML
    private void processReschedule(ActionEvent actionEvent){
        TextField firstNameField = re_patient_first_name;
        TextField lastNameField = re_patient_last_name;
        DatePicker appointmentDatePicker = re_appointment_date;
        DatePicker dobPicker =  re_date_of_birth;
        ComboBox<String> oldtimeslotSelection = re_timeslot_selection;
        ComboBox<String> newtimeslotSelection = re_newtimeslot_selection;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate appointmentDateLocal = appointmentDatePicker.getValue();
        String oldTimeslotStr = oldtimeslotSelection.getValue();
        String newTimeslotStr = newtimeslotSelection.getValue();
        LocalDate dobLocal = dobPicker.getValue();

        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        if (firstName == null || firstName.isEmpty() ||
                lastName == null || lastName.isEmpty() ||
                appointmentDateLocal == null || dobLocal == null ||
                oldTimeslotStr == null || newTimeslotStr == null) {
            re_status_messages.appendText("Fill all fields\n");
            return;
        }

        System.out.println("R," + appointmentDate + "," + convertTimeToSlot(oldTimeslotStr) + "," +
                firstName + "," + lastName + "," + dob + "," + convertTimeToSlot(newTimeslotStr));

        try {
            Timeslot oldSlot = validateTimeslot(convertTimeToSlot(oldTimeslotStr));
            Timeslot newSlot = validateTimeslot(convertTimeToSlot(newTimeslotStr));
            if (!validateInputs(appointmentDate, oldSlot, dob, newSlot)) return;

            handleRescheduling(firstName, lastName, appointmentDate, oldSlot, dob, newSlot);
        } catch (Exception e) {
            System.out.println("Missing data tokens.");
        }
    }

    /**
     * Handles the rescheduling of an existing appointment.
     *
     * @param firstName        The first name of the patient.
     * @param lastName         The last name of the patient.
     * @param appointmentDate   The date of the appointment.
     * @param oldSlot          The original timeslot of the appointment.
     * @param dob              The date of birth of the patient.
     * @param newSlot          The new timeslot to which the appointment will be rescheduled.
     */
    private void handleRescheduling(String firstName,String lastName, Date appointmentDate, Timeslot oldSlot, Date dob, Timeslot newSlot) {

        // Find appointment in the old timeslot
        Appointment appointmentToReschedule = findAppointment(appointmentDate, oldSlot, firstName, lastName, dob);
        if (appointmentToReschedule == null) {
            // Print a message if the appointment does not exist in the old timeslot
            System.out.printf("%s %s %s %s %s does not exist.%n",
                    appointmentDate.toString(), oldSlot, firstName, lastName, dob);
            return;
        }

        // Check if there's already an appointment at the new timeslot
        if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, newSlot)) {
            System.out.printf("%s %s %s has an existing appointment at %s %s.%n",
                    firstName, lastName, dob, appointmentDate, newSlot);
            return;
        }

        // Reschedule the appointment to the new timeslot
        rescheduleAppointment(appointmentToReschedule, newSlot, appointmentDate, firstName, lastName, dob);
    }

    /**
     * Reschedules the appointment to a new timeslot.
     *
     * @param appointmentToReschedule the appointment to reschedule
     * @param newSlot the new timeslot to assign
     * @param appointmentDate the date of the appointment
     * @param firstName the patient's first name
     * @param lastName the patient's last name
     * @param dob the patient's date of birth
     */
    private void rescheduleAppointment(Appointment appointmentToReschedule, Timeslot newSlot, Date appointmentDate, String firstName, String lastName, Date dob) {
        appointmentToReschedule.setTimeslot(newSlot);  // Update the timeslot in the appointment
        System.out.printf("Rescheduled to %s %s %s %s %s %s%n",
                appointmentDate.toString(), newSlot, firstName, lastName, dob, appointmentToReschedule.getProvider().toString());
    }

    /**
     * Validates the inputs for rescheduling.
     *
     * @param appointmentDate the appointment date
     * @param oldSlot         the original timeslot of the appointment
     * @param dob             the patient's date of birth
     * @param newSlot         the new timeslot
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs(Date appointmentDate, Timeslot oldSlot, Date dob, Timeslot newSlot) {
        return appointmentDate != null && oldSlot != null && dob != null && newSlot != null;
    }

    /**
     * Validates the given appointment date string and returns a Date object if valid.
     *
     * @param appointmentDateStr the string representation of the appointment date to validate
     * @return a valid Date object if the appointment date is valid, or null if invalid
     */
    private Date validateAppointmentDate(String appointmentDateStr) {
        Date appointmentDate = parseDate(appointmentDateStr);
        if (appointmentDate == null || !appointmentDate.isValid()) {
            System.out.printf("Appointment date: %s is not a valid calendar date%n", appointmentDateStr);
            return null;
        }
        if (appointmentDate.equals(getToday()) || isBeforeToday(appointmentDate)) {
            System.out.printf("Appointment date: %s is today or a date before today.%n", appointmentDateStr);
            return null;
        }
        if (isWeekend(appointmentDate)) {
            System.out.printf("Appointment date: %s is Saturday or Sunday.%n", appointmentDateStr);
            return null;
        }
        if (isBeyondSixMonths(appointmentDate)) {
            System.out.printf("Appointment date: %s is not within six months.%n", appointmentDateStr);
            return null;
        }
        return appointmentDate;
    }

    /**
     * Validates the provided timeslot input and returns the corresponding Timeslot object.
     *
     * @param timeslotInput the string representation of the timeslot to validate
     * @return the corresponding Timeslot object if valid, or null if invalid
     */
    private Timeslot validateTimeslot(String timeslotInput) {
        try {
            return Timeslot.fromString(timeslotInput);
        } catch (IllegalArgumentException e) {
            System.out.println(timeslotInput + " is not a valid time slot.");
            return null;
        }
    }

    /**
     * Validates the provided date of birth (DOB) string and returns the corresponding Date object.
     *
     * @param dobStr the string representation of the date of birth to validate
     * @return the corresponding Date object if valid, or null if invalid
     */
    private Date validateDateOfBirth(String dobStr) {
        Date dob = parseDate(dobStr);
        if (dob == null || !dob.isValid()) {
            System.out.println("Patient dob: " + dobStr + " is not a valid calendar date");
            return null;
        }
        if (dob.equals(getToday()) || isFutureDate(dob)) {
            System.out.println("Patient dob: " + dobStr + " is today or a date after today.");
            return null;
        }
        return dob;
    }

    /**
     * Retrieves the current date.
     *
     * @return a Date object corresponding to today's date
     */
    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        return new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Checks if the specified date is before today's date.
     *
     * @param date the date to check
     * @return true if the specified date is before today, false otherwise
     */
    private boolean isBeforeToday(Date date) {
        Date today = getToday();
        return date.compareTo(today) < 0;
    }

    /**
     * Checks if the specified date falls on a weekend (Saturday or Sunday).
     *
     * @param date the date to check
     * @return true if the date is a Saturday or Sunday, false otherwise
     */
    private boolean isWeekend(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Checks if the specified date is beyond six months from today.
     *
     * @param date the date to check
     * @return true if the date is more than six months in the future, false otherwise
     */
    private boolean isBeyondSixMonths(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, MAX_MONTHS_AHEAD);
        Date sixMonthsFromNow = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        return date.compareTo(sixMonthsFromNow) > 0;
    }
    /**
     * Checks if the specified date is in the future compared to today's date.
     *
     * @param date the date to check
     * @return true if the date is in the future, false otherwise
     */
    private boolean isFutureDate(Date date) {
        Date today = getToday();
        return date.compareTo(today) > 0;
    }

    /**
     * Parses a date string in the format "MM/DD/YYYY" and converts it to a Date object.
     *
     * @param dateString the date string to parse
     * @return a Date object representing the parsed date, or null if the format is invalid
     */
    private Date parseDate(String dateString) {
        try {
            String[] parts = dateString.split("/");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            return new Date(year, month, day);
        } catch (Exception e) {
            System.out.println("Error: Invalid date format.");
            return null;
        }
    }

    /**
     * Searches for a doctor in the provider list by their National Provider Identifier (NPI).
     *
     * @param npi the National Provider Identifier of the doctor to find
     * @return the Doctor object if found, or null if no doctor with the given NPI exists
     */
    private Doctor findDoctorByNPI(int npi) {
        for (Provider provider : providerList) {
            if (provider instanceof Doctor doctor) {
                if (Integer.parseInt(doctor.getNpi()) == npi) {
                    return doctor;
                }
            }
        }
        return null;
    }

    /**
     * Searches for an available technician for a specified timeslot and radiology room.
     *
     * @param timeslot the timeslot for which an available technician is required
     * @param room the radiology room where the service is requested
     * @return the available Technician if found, or null if no technician is available
     */
    private Technician findAvailableTechnician(Timeslot timeslot, Radiology room) {
        if (technicianRotationList.isEmpty()) {
            System.out.println("Error: No technicians available for rotation.");
            return null;
        }
        int technicianCount = technicianRotationList.size();
        int slotIndex = timeslot.getSlotIndex() - 1;

        for (int i = 0; i < technicianCount; i++) {
            Technician technician = technicianRotationList.get(technicianRotationIndex);

            boolean isRoomAvailable = isRoomAvailable(timeslot, technician.getLocation(), room);
            boolean isTechnicianAlreadyAssigned = technicianAssigned[slotIndex][technicianRotationIndex];

            if (isRoomAvailable && !isTechnicianAlreadyAssigned && isTechnicianAvailable(technician, timeslot, room)) {
                technicianAssigned[slotIndex][technicianRotationIndex] = true;
                technicianRotationIndex = (technicianRotationIndex + 1) % technicianCount;

                return technician;
            }
            technicianRotationIndex = (technicianRotationIndex + 1) % technicianCount;
        }
        return null;
    }

    /**
     * Checks if a specified radiology room is available during a given timeslot at a specific location.
     *
     * @param timeslot the timeslot to check for availability
     * @param location the location of the technician
     * @param room the radiology room to check for availability
     * @return true if the room is available for the given timeslot; false otherwise
     */
    private boolean isRoomAvailable(Timeslot timeslot, Location location, Radiology room) {
        for (Appointment appointment : appointmentList) {
            if (appointment instanceof Imaging imagingAppointment) {
                Technician technician = (Technician) imagingAppointment.getProvider();
                if (imagingAppointment.getTimeslot().equals(timeslot) &&
                        imagingAppointment.getRoom().equals(room) &&
                        technician.getLocation().equals(location)) {
                    return false;
                }
            }
        }
        return true; // Room is available
    }

    /**
     * Checks if a specified technician is available during a given timeslot for a specific radiology room.
     *
     * @param technician the technician to check for availability
     * @param timeslot the timeslot to check for conflicts
     * @param room the radiology room to check for conflicts
     * @return true if the technician is available for the given timeslot and room; false otherwise
     */
    private boolean isTechnicianAvailable(Technician technician, Timeslot timeslot, Radiology room) {
        for (Appointment appointment : appointmentList) {
            if (appointment instanceof Imaging imagingAppointment) {
                if (imagingAppointment.getProvider().equals(technician) &&
                        imagingAppointment.getTimeslot().equals(timeslot) &&
                        imagingAppointment.getRoom().equals(room)) {
                    return false; // Technician is already booked at the same time for the same room
                }
            }
        }
        return true;
    }

    /**
     * Searches for an appointment based on the specified date, timeslot, patient's name, and date of birth.
     *
     * @param date the date of the appointment to search for
     * @param timeslot the timeslot of the appointment to search for
     * @param firstName the first name of the patient associated with the appointment
     * @param lastName the last name of the patient associated with the appointment
     * @param dob the date of birth of the patient associated with the appointment
     * @return the found Appointment if a match exists; null if no matching appointment is found
     */
    private Appointment findAppointment(Date date, Timeslot timeslot, String firstName, String lastName, Date dob) {
        for (Appointment appointment : appointmentList) {
            if (appointment.getDate().equals(date) &&
                    appointment.getTimeslot().equals(timeslot) &&
                    appointment.getPatient().getProfile().getFname().equalsIgnoreCase(firstName) &&
                    appointment.getPatient().getProfile().getLname().equalsIgnoreCase(lastName) &&
                    appointment.getPatient().getProfile().getDob().equals(dob)) {
                return appointment;
            }
        }
        return null;
    }

    /**
     * Appends a given text to the display text area, adding a newline after the text.
     *
     * @param text The text to be appended to the display text area.
     */
    public void appendToDisplayTextArea(String text) {
        display_text_area.appendText(text + "\n");
    }

    /**
     * Appends a message detailing the office appointment to the status messages area.
     *
     * @param appointmentDate     The date of the appointment.
     * @param timeslot            The timeslot of the appointment.
     * @param firstName          The first name of the patient.
     * @param lastName           The last name of the patient.
     * @param dob                The date of birth of the patient.
     * @param doctorFirstName     The first name of the doctor.
     * @param doctorLastName      The last name of the doctor.
     * @param doctorDob           The date of birth of the doctor.
     * @param doctorLocation      The location of the doctor.
     * @param doctorSpecialty     The specialty of the doctor.
     * @param doctorNpi           The NPI of the doctor.
     */
    public void appendToOfficeTextArea(Date appointmentDate, Timeslot timeslot, String firstName, String lastName,
                                       Date dob, String doctorFirstName, String doctorLastName, Date doctorDob,
                                       Location doctorLocation, Specialty doctorSpecialty, String doctorNpi) {
        String message = String.format("%s %s %s %s %s [%s %s %s, %s[%s, #%s] booked.",
                appointmentDate, timeslot, firstName, lastName, dob, doctorFirstName,
                doctorLastName, doctorDob, doctorLocation.toString(), doctorSpecialty.toString(), doctorNpi);
        status_messages.appendText(message + "\n");
    }

    /**
     * Appends a message detailing the imaging appointment to the imaging status messages area.
     *
     * @param appointmentDate       The date of the appointment.
     * @param timeslot              The timeslot of the appointment.
     * @param firstName            The first name of the patient.
     * @param lastName             The last name of the patient.
     * @param dob                  The date of birth of the patient.
     * @param technicianFirstName    The first name of the technician.
     * @param technicianLastName     The last name of the technician.
     * @param technicianDob         The date of birth of the technician.
     * @param technicianLocation    The location of the technician.
     * @param technicianRate        The rate of the technician.
     * @param imagingService        The specific imaging service requested.
     */
    public void appendToImagingTextArea(Date appointmentDate, Timeslot timeslot, String firstName, String lastName,
                                        Date dob, String technicianFirstName, String technicianLastName, Date technicianDob,
                                        Location technicianLocation, double technicianRate, String imagingService) {
        String message = String.format("%s %s %s %s %s [%s %s %s, %s[$%.2f][%s]",
                appointmentDate, timeslot, firstName, lastName, dob, technicianFirstName,
                technicianLastName, technicianDob, technicianLocation.toString(), technicianRate, imagingService);
        imaging_status_messages.appendText(message + "\n");
    }
}
