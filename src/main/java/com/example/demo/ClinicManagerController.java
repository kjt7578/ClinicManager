package com.example.demo;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

/**
 * The ClinicManagerController class manages the functionality of the clinic's appointment scheduling system.
 * It handles user interactions within the JavaFX interface
 *
 * The class utilizes FXML components for the user interface and contains methods to manipulate
 * appointment lists and display messages to the user.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
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
    private TextArea cancel_status_messages;

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
    private RadioButton XRAY_button;

    @FXML
    private RadioButton CATSCAN_button;

    @FXML
    private RadioButton ULTRASOUND_button;

    @FXML
    private ToggleGroup imagingGroup;

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

    @FXML
    private TableColumn<Provider, String> countyColumn;

    @FXML
    private TableColumn<Provider, String> zipColumn;

    @FXML
    private TableView<Provider> providerTable;

    private ObservableList<Provider> OBSdoctorList = FXCollections.observableArrayList();
    private ObservableList<Provider> OBStechnicianList = FXCollections.observableArrayList();
    private ObservableList<Provider> providerData = FXCollections.observableArrayList();
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
        countyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation().getCounty()));
        zipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation().getZipCode()));
        imagingGroup = new ToggleGroup();
        XRAY_button.setToggleGroup(imagingGroup);
        CATSCAN_button.setToggleGroup(imagingGroup);
        ULTRASOUND_button.setToggleGroup(imagingGroup);
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
    }

    /**
     * Loads provider information from a file and populates the lists for doctors and technicians.
     * It also updates the UI with the names of the providers and initializes the technician rotation.
     */
    @FXML
    private void loadProviders() {
        loadProviderDataFromFile();
        initializeUniqueLocations();
        updateUIWithProviderData();
    }

    /**
     * Loads provider data from the file and populates the lists for doctors and technicians.
     */
    private void loadProviderDataFromFile() {
        try (Scanner scanner = new Scanner(new File(PROVIDERS_FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Provider provider = parseProvider(line);
                    addProviderToLists(provider);
                }
            }
        } catch (FileNotFoundException e) {
            appendMessage("Error: " + PROVIDERS_FILE_PATH + " cannot be found.");
        }
    }

    /**
     * Adds a provider to the respective lists for doctors, technicians, and general providers.
     *
     * @param provider the provider object to add
     */
    private void addProviderToLists(Provider provider) {
        if (provider instanceof Doctor) {
            OBSdoctorList.add(provider);
        } else if (provider instanceof Technician) {
            OBStechnicianList.add(provider);
        }
        if (provider != null) {
            providerList.add(provider);
        }
    }

    /**
     * Initializes unique locations from the provider list to avoid duplicates.
     */
    private void initializeUniqueLocations() {
        List<Location> uniqueLocations = new List<>();
        for (Provider provider : providerList) {
            Location loc = provider.getLocation();
            if (!isDuplicateLocation(uniqueLocations, loc)) {
                uniqueLocations.add(loc);
                providerData.add(provider);
            }
        }
    }

    /**
     * Checks if a location is already in the list of unique locations.
     *
     * @param uniqueLocations list of unique locations
     * @param loc location to check
     * @return true if the location is duplicate, false otherwise
     */
    private boolean isDuplicateLocation(List<Location> uniqueLocations, Location loc) {
        for (Location uniqueLoc : uniqueLocations) {
            if (uniqueLoc.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the UI with provider data, sets ComboBox items, and initializes the technician rotation.
     */
    private void updateUIWithProviderData() {
        providerTable.setItems(providerData);

        ObservableList<String> doctorNames = FXCollections.observableArrayList();
        for (Provider doctor : OBSdoctorList) {
            doctorNames.add(doctor.getProfile().getFname() + " " + doctor.getProfile().getLname() + " (" + doctor.getLocation().name() + ")");
        }
        office_provider_selection.setItems(doctorNames);

        ObservableList<String> technicianNames = FXCollections.observableArrayList();
        for (Provider technician : OBStechnicianList) {
            technicianNames.add(technician.getProfile().getFname() + " " + technician.getProfile().getLname() + " (" + technician.getLocation().name() + ")");
        }

        providerTable.setItems(providerData);
        createTechnicianRotation();
        Sort.provider(providerList);
        displayProviders();
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
        AppointmentData data = gatherAppointmentData();
        if (data == null) {
            appendMessage("Fill all fields");
            return;
        }

        if (!validateAndCheckAvailability(data)) return;

        createNewAppointment(data.firstName, data.lastName, data.dob, data.appointmentDate, data.timeslot, data.doctor, true);
    }

    /**
     * Gathers input data for the appointment.
     *
     * @return an AppointmentData object containing the input data, or null if any field is missing
     */
    private AppointmentData gatherAppointmentData() {
        String firstName = office_patient_first_name.getText();
        String lastName = office_patient_last_name.getText();
        LocalDate appointmentDateLocal = office_appointment_date.getValue();
        String timeslotStr = office_timeslot_selection.getValue();
        String providerName = office_provider_selection.getValue();
        LocalDate dobLocal = office_date_of_birth.getValue();

        if (providerName != null) {
            providerName = providerName.split(" \\(")[0];
        }

        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        if (isAnyFieldEmpty(firstName, lastName, appointmentDate, timeslotStr, providerName, dob)) {
            return null;
        }

        return new AppointmentData(firstName, lastName, appointmentDate, dob, timeslotStr, providerName);
    }

    /**
     * Validates appointment data, checks for duplicates, and verifies provider availability.
     *
     * @param data the AppointmentData object containing the data to validate
     * @return true if data is valid and the provider is available, false otherwise
     */
    private boolean validateAndCheckAvailability(AppointmentData data) {
        try {
            data.appointmentDate = validateAppointmentDate(String.valueOf(data.appointmentDate), status_messages);
            data.timeslot = validateTimeslot(convertTimeToSlot(data.timeslotStr));
            data.dob = validateDateOfBirth(String.valueOf(data.dob), status_messages);
            if (!validateInputs(data.appointmentDate, data.timeslot, data.dob)) return false;
            data.doctor = getDoctorByNPI(convertProvicerToSNPI(data.providerName));
            if (data.doctor == null) return false;

            if (isDuplicateAppointment(data.firstName, data.lastName, data.dob, data.appointmentDate, data.timeslot)) {
                appendToOfficeTextArea(data.appointmentDate, data.timeslot, data.firstName, data.lastName, data.dob,
                        data.doctor.getProfile().getFname(), data.doctor.getProfile().getLname(),
                        data.doctor.getDateOfBirth(), data.doctor.getLocation(),
                        data.doctor.getSpecialty(), data.doctor.getNpi(), false);
                return false;
            }

            if (isDoctorUnavailable(data.doctor, data.appointmentDate, data.timeslot)) {
                appendToOfficeTextAreaUnav(data.appointmentDate, data.timeslot, data.firstName, data.lastName, data.dob,
                        data.doctor.getProfile().getFname(), data.doctor.getProfile().getLname(),
                        data.doctor.getDateOfBirth(), data.doctor.getLocation(),
                        data.doctor.getSpecialty(), data.doctor.getNpi(), false);
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if any of the provided fields are empty or null.
     *
     * @param fields the fields to check
     * @return true if any field is empty or null, false otherwise
     */
    private boolean isAnyFieldEmpty(Object... fields) {
        for (Object field : fields) {
            if (field == null || field.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Data class to hold appointment details.
     */
    private static class AppointmentData {
        String firstName;
        String lastName;
        Date appointmentDate;
        Date dob;
        String timeslotStr;
        String providerName;
        Timeslot timeslot;
        Doctor doctor;

        AppointmentData(String firstName, String lastName, Date appointmentDate, Date dob, String timeslotStr, String providerName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.appointmentDate = appointmentDate;
            this.dob = dob;
            this.timeslotStr = timeslotStr;
            this.providerName = providerName;
        }
    }


    /**
     * Processes the cancellation of an appointment by gathering input data,
     * validating fields, and finding the matching appointment in the list.
     */
    @FXML
    private void processCancellation() {
        String firstName = cancel_patient_first_name.getText();
        String lastName = cancel_patient_last_name.getText();
        LocalDate appointmentDateLocal = cancel_appointment_date.getValue();
        LocalDate dobLocal = cancel_date_of_birth.getValue();
        String timeslotStr = cancel_timeslot_selection.getValue();
        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
                appointmentDate == null || timeslotStr == null || dob == null) {
            cancel_status_messages.appendText("Fill all fields");
            return;
        }
        try {
            appointmentDate = validateAppointmentDate(String.valueOf(appointmentDate),cancel_status_messages);
            Timeslot timeslot = validateTimeslot(convertTimeToSlot(timeslotStr));
            dob = validateDateOfBirth(String.valueOf(dob),cancel_status_messages);
            if (!validateInputs(appointmentDate, timeslot, dob)) return;
            Appointment appointmentToCancel = findAppointment(appointmentDate, timeslot, firstName.toLowerCase(), lastName.toLowerCase(), dob);

            if (appointmentToCancel != null) {
                appointmentList.remove(appointmentToCancel);
                cancel_status_messages.appendText(appointmentDate + " " + timeslot + " " + firstName + " " + lastName + " " + dob + " - appointment has been canceled.\n");
            } else {
                cancel_status_messages.appendText(appointmentDate + " " + timeslot + " " + firstName + " " + lastName + " " + dob + " - appointment does not exist.\n");
            }
        } catch (Exception e) {
            cancel_status_messages.appendText("Error: Invalid cancellation command.\n");
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
        if ("ANDREW PATEL".equals(providerName)) return "01";
        else if ("RACHAEL LIM".equals(providerName)) return "23";
        else if ("MONICA ZIMNES".equals(providerName)) return "11";
        else if ("JOHN HARPER".equals(providerName)) return "32";
        else if ("TOM KAUR".equals(providerName)) return "54";
        else if ("ERIC TAYLOR".equals(providerName)) return "91";
        else if ("BEN RAMESH".equals(providerName)) return "39";
        else if ("JUSTIN CERAVOLO".equals(providerName)) return "09";
        else if ("GARY JOHNSON".equals(providerName)) return "85";
        else if ("BEN JERRY".equals(providerName)) return "77";
        else if ("FRANK LIN".equals(providerName)) return "120";
        else if ("CHARLES BROWN".equals(providerName)) return "100";
        else if ("MONICA FOX".equals(providerName)) return "130";
        else if ("JENNY PATEL".equals(providerName)) return "125";
        else return null;
    }

    /**
     * Converts a given timeslot string (in "HH:MM AM/PM" format) to a corresponding
     * slot number as a string.
     *
     * @param timeslot the timeslot string to convert
     * @return the corresponding slot number as a string, or null if the timeslot is unrecognized
     */
    private String convertTimeToSlot(String timeslot) {
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
        display_text_area.clear();

        if (appointmentList.isEmpty()) {
            display_text_area.setText("No appointments to display.");
            return;
        }

        String selectedOption = display_selector.getValue();
        if (selectedOption != null) {
            String command = getCommandFromOption(selectedOption);
            if (command != null) {
                processSortingCommand(appointmentList, command);
            } else {
                appendToDisplayTextArea("Invalid display option selected.");
            }
        } else {
            appendToDisplayTextArea("Please select a display option.");
        }
    }

    /**
     * Maps selected option to the corresponding command.
     *
     * @param selectedOption the selected option from the ComboBox
     * @return the corresponding command string, or null if invalid
     */
    private String getCommandFromOption(String selectedOption) {
        if ("PA: Sort by Appointment Date".equals(selectedOption)) return "PA";
        else if ("PP: Sort by Patient".equals(selectedOption)) return "PP";
        else if ("PL: Sort by County".equals(selectedOption)) return "PL";
        else if ("PS: Display Billing by Specialty".equals(selectedOption)) return "PS";
        else if ("PO: Sort Office Appointments by County".equals(selectedOption)) return "PO";
        else if ("PI: Sort Imaging Appointments by County".equals(selectedOption)) return "PI";
        else if ("PC: Display Credit by Provider".equals(selectedOption)) return "PC";
        else return null;
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
     * Prints the technician rotation list.
     * If the list is empty, a message is displayed; otherwise,
     * the technicians' names and locations are printed.
     */
    private void printTechnicianRotation() {
        if (technicianRotationList.isEmpty()) {
            appendToTextArea(status_messages, "No technicians found for rotation.");
        } else {
            appendToTextArea(status_messages, "\nRotation list for the technicians:");
            for (int i = 0; i < technicianRotationList.size(); i++) {
                Technician technician = technicianRotationList.get(i);
                String message = technician.getProfile().getFname() + " " + technician.getProfile().getLname()
                        + " (" + technician.getLocation().name() + ")";
                if (i < technicianRotationList.size() - 1) {
                    message += " --> ";
                }
                appendToTextArea(status_messages, message);
            }
        }
        appendToTextArea(status_messages, "");  // 빈 줄 추가
    }


    /**
     * Displays the list of providers loaded in the system, including their
     * profiles, locations, and additional information specific to their type.
     */
    private void displayProviders() {
        appendToTextArea(status_messages, "Providers loaded to the list.");

        for (Provider provider : providerList) {
            StringBuilder message = new StringBuilder("[" + provider.getProfile().toString() + ", ");
            message.append(provider.getLocation().toString());

            if (provider instanceof Doctor doctor) {
                message.append("[").append(doctor.getSpecialty()).append(", #").append(doctor.getNpi());
            } else if (provider instanceof Technician technician) {
                message.append("[rate: $").append(String.format("%.2f", (double) technician.getRatePerVisit()));
            }
            message.append("]");
            appendToTextArea(status_messages, message.toString());
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
            Date appointmentDate = validateAppointmentDate(tokens[1],status_messages);
            Timeslot timeslot = validateTimeslot(tokens[2]);
            Date dob = validateDateOfBirth(tokens[5],status_messages);
            if (!validateInputs(appointmentDate, timeslot, dob)) return;

            String firstName = tokens[3];
            String lastName = tokens[4];
            Doctor doctor = getDoctorByNPI(tokens[6]);
            if (doctor == null) return;

            // Check for duplicate appointment
            if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, timeslot)) {
                return;  // Stop further processing since it's a duplicate
            }

            // Check if the doctor is unavailable at the given time
            if (isDoctorUnavailable(doctor, appointmentDate, timeslot)) {
                return;
            }

            // Create the new appointment and add it to the appointment list
            createNewAppointment(firstName, lastName, dob, appointmentDate, timeslot, doctor, true);
        } catch (Exception e) {
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
            }
            return doctor;
        } catch (NumberFormatException e) {
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
    private void createNewAppointment(String firstName, String lastName, Date dob, Date appointmentDate,
                                      Timeslot timeslot, Doctor doctor, boolean isAvailable) {
        Appointment newAppointment = new Appointment(appointmentDate, timeslot,
                new Patient(new Profile(firstName, lastName, dob)), doctor);
        appointmentList.add(newAppointment);

        // Log the appointment status based on the availability
        if (isAvailable) {
            // If the doctor is available and the appointment is booked
            appendToOfficeTextArea(appointmentDate, timeslot, firstName, lastName, dob,
                    doctor.getProfile().getFname(), doctor.getProfile().getLname(),
                    doctor.getProfile().getDob(), doctor.getLocation(),
                    doctor.getSpecialty(), doctor.getNpi(), true); // Pass true for availability
        } else {
        }
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
        ImagingAppointmentData data = gatherImagingAppointmentData();
        if (data == null) {
            imaging_status_messages.appendText("Fill all fields");
            return;
        }

        if (!validateImagingAppointmentData(data)) return;

        handleImagingAppointment(data.firstName, data.lastName, data.appointmentDate, data.timeslot, data.dob, data.imagingService);
    }

    /**
     * Gathers input data for the imaging appointment.
     *
     * @return an ImagingAppointmentData object containing the input data, or null if any field is missing
     */
    private ImagingAppointmentData gatherImagingAppointmentData() {
        String firstName = imaging_patient_first_name.getText();
        String lastName = imaging_patient_last_name.getText();
        LocalDate appointmentDateLocal = imaging_appointment_date.getValue();
        String timeslotStr = imaging_timeslot_selection.getValue();
        LocalDate dobLocal = imaging_date_of_birth.getValue();

        RadioButton selectedImagingButton = (RadioButton) imagingGroup.getSelectedToggle();
        String imagingService = (selectedImagingButton != null) ? selectedImagingButton.getText() : null;

        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        if (isAnyFieldEmptyImage(firstName, lastName, appointmentDateLocal, dobLocal, timeslotStr, imagingService)) {
            return null;
        }

        return new ImagingAppointmentData(firstName, lastName, appointmentDate, dob, timeslotStr, imagingService);
    }

    /**
     * Validates imaging appointment data and checks if the inputs are valid.
     *
     * @param data the ImagingAppointmentData object containing the data to validate
     * @return true if data is valid, false otherwise
     */
    private boolean validateImagingAppointmentData(ImagingAppointmentData data) {
        try {
            data.appointmentDate = validateAppointmentDate(String.valueOf(data.appointmentDate), imaging_status_messages);
            data.timeslot = validateTimeslot(convertTimeToSlot(data.timeslotStr));
            data.dob = validateDateOfBirth(String.valueOf(data.dob), imaging_status_messages);

            return validateInputs(data.appointmentDate, data.timeslot, data.dob);
        } catch (Exception e) {
            imaging_status_messages.appendText("An error occurred while processing the appointment.");
            return false;
        }
    }

    /**
     * Checks if any of the provided fields are empty or null.
     *
     * @param fields the fields to check
     * @return true if any field is empty or null, false otherwise
     */
    private boolean isAnyFieldEmptyImage(Object... fields) {
        for (Object field : fields) {
            if (field == null || field.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Data class to hold imaging appointment details.
     */
    private static class ImagingAppointmentData {
        String firstName;
        String lastName;
        Date appointmentDate;
        Date dob;
        String timeslotStr;
        String imagingService;
        Timeslot timeslot;

        ImagingAppointmentData(String firstName, String lastName, Date appointmentDate, Date dob, String timeslotStr, String imagingService) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.appointmentDate = appointmentDate;
            this.dob = dob;
            this.timeslotStr = timeslotStr;
            this.imagingService = imagingService;
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
            return false;
        }
        return true;
    }

    /**
     * Handles the scheduling of a new imaging appointment for a patient.
     *
     * @param firstName        The first name of the patient.
     * @param lastName         The last name of the patient.
     * @param appointmentDate   The date of the appointment.
     * @param timeslot         The timeslot for the appointment.
     * @param dob              The date of birth of the patient.
     * @param imagingService    The type of imaging service requested.
     */
    private void handleImagingAppointment(String firstName, String lastName, Date appointmentDate, Timeslot timeslot, Date dob, String imagingService) {
        Radiology room = getRadiologyRoom(imagingService);

        // Check for duplicate imaging appointment
        if (isDuplicateImagingAppointment(firstName, lastName, dob, appointmentDate, timeslot)) {
            appendToImagingTextArea(
                    appointmentDate, timeslot, firstName, lastName, dob, null, null, null, null, 0.0, imagingService
            );
            return;
        }

        Technician technician = findAvailableTechnician(timeslot, room);
        if (technician == null) {
            appendToImagingTextAreaNoTech(
                    appointmentDate, timeslot, firstName, lastName, dob, null, null, null, null, 0.0, imagingService
            );
            return;
        }

        createNewImagingAppointment(firstName, lastName, dob, appointmentDate, timeslot, technician, room, imagingService);
    }

    /**
     * Returns the appropriate Radiology room based on the specified imaging service.
     *
     * @param imagingService The type of imaging service requested.
     * @return The corresponding Radiology room.
     * @throws IllegalArgumentException if the imaging service is invalid.
     */
    private Radiology getRadiologyRoom(String imagingService) {
        switch (imagingService) {
            case "XRAY":
                return Radiology.XRAY;
            case "CATSCAN":
                return Radiology.CATSCAN;
            case "ULTRASOUND":
                return Radiology.ULTRASOUND;
            default:
                throw new IllegalArgumentException("Invalid imaging service: " + imagingService);
        }
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
    private void processReschedule(ActionEvent actionEvent) {
        RescheduleData data = gatherRescheduleData();
        if (data == null) {
            re_status_messages.appendText("Fill all fields\n");
            return;
        }

        try {
            if (!validateRescheduleInputs(data)) return;
            handleRescheduling(data.firstName, data.lastName, data.appointmentDate, data.oldSlot, data.dob, data.newSlot);
        } catch (Exception e) {
            re_status_messages.appendText("An error occurred while processing the reschedule.\n");
        }
    }

    /**
     * Gathers input data for the rescheduling.
     *
     * @return a RescheduleData object containing the input data, or null if any field is missing
     */
    private RescheduleData gatherRescheduleData() {
        String firstName = re_patient_first_name.getText();
        String lastName = re_patient_last_name.getText();
        LocalDate appointmentDateLocal = re_appointment_date.getValue();
        String oldTimeslotStr = re_timeslot_selection.getValue();
        String newTimeslotStr = re_newtimeslot_selection.getValue();
        LocalDate dobLocal = re_date_of_birth.getValue();

        if (firstName == null || firstName.isEmpty() ||
                lastName == null || lastName.isEmpty() ||
                appointmentDateLocal == null || dobLocal == null ||
                oldTimeslotStr == null || newTimeslotStr == null) {
            return null;
        }

        Date appointmentDate = convertToDate(appointmentDateLocal);
        Date dob = convertToDate(dobLocal);

        return new RescheduleData(firstName, lastName, appointmentDate, dob, oldTimeslotStr, newTimeslotStr);
    }

    /**
     * Validates and converts rescheduling inputs.
     *
     * @param data the RescheduleData object containing the data to validate
     * @return true if data is valid, false otherwise
     */
    private boolean validateRescheduleInputs(RescheduleData data) {
        data.oldSlot = validateTimeslot(convertTimeToSlot(data.oldTimeslotStr));
        data.newSlot = validateTimeslot(convertTimeToSlot(data.newTimeslotStr));

        return validateInputs(data.appointmentDate, data.oldSlot, data.dob, data.newSlot);
    }

    /**
     * Data class to hold reschedule details.
     */
    private static class RescheduleData {
        String firstName;
        String lastName;
        Date appointmentDate;
        Date dob;
        String oldTimeslotStr;
        String newTimeslotStr;
        Timeslot oldSlot;
        Timeslot newSlot;

        RescheduleData(String firstName, String lastName, Date appointmentDate, Date dob, String oldTimeslotStr, String newTimeslotStr) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.appointmentDate = appointmentDate;
            this.dob = dob;
            this.oldTimeslotStr = oldTimeslotStr;
            this.newTimeslotStr = newTimeslotStr;
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
    private void handleRescheduling(String firstName, String lastName, Date appointmentDate, Timeslot oldSlot, Date dob, Timeslot newSlot) {

        // Find appointment in the old timeslot
        Appointment appointmentToReschedule = findAppointment(appointmentDate, oldSlot, firstName, lastName, dob);
        if (appointmentToReschedule == null) {
            String message = String.format("%s %s %s %s %s does not exist.%n",
                    appointmentDate.toString(), oldSlot, firstName, lastName, dob);
            re_status_messages.appendText(message);
            return;
        }

        // Check if there's already an appointment at the new timeslot
        if (isDuplicateAppointment(firstName, lastName, dob, appointmentDate, newSlot)) {
            String message = String.format("%s %s %s has an existing appointment at %s %s.%n",
                    firstName, lastName, dob, appointmentDate, newSlot);
            re_status_messages.appendText(message);
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

        String message = String.format("Rescheduled to %s %s %s %s %s %s%n",
                appointmentDate.toString(), newSlot, firstName, lastName, dob, appointmentToReschedule.getProvider().toString());

        re_status_messages.appendText(message);
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
    private Date validateAppointmentDate(String appointmentDateStr, TextArea outputTextArea) {
        Date appointmentDate = parseDate(appointmentDateStr);
        if (appointmentDate == null || !appointmentDate.isValid()) {
            String message = String.format("Appointment date: %s is not a valid calendar date%n", appointmentDateStr);
            appendToTextArea(outputTextArea, message);
            return null;
        }
        if (appointmentDate.equals(getToday()) || isBeforeToday(appointmentDate)) {
            String message = String.format("Appointment date: %s is today or a date before today.%n", appointmentDateStr);
            appendToTextArea(outputTextArea, message);
            return null;
        }
        if (isWeekend(appointmentDate)) {
            String message = String.format("Appointment date: %s is Saturday or Sunday.%n", appointmentDateStr);
            appendToTextArea(outputTextArea, message);
            return null;
        }
        if (isBeyondSixMonths(appointmentDate)) {
            String message = String.format("Appointment date: %s is not within six months.%n", appointmentDateStr);
            appendToTextArea(outputTextArea, message);
            return null;
        }
        return appointmentDate;
    }

    private void appendToTextArea(TextArea textArea, String text) {
        if (textArea != null) {
            textArea.appendText(text + "\n");
        }
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
            return null;
        }
    }

    /**
     * Validates the provided date of birth (DOB) string and returns the corresponding Date object.
     *
     * @param dobStr the string representation of the date of birth to validate
     * @return the corresponding Date object if valid, or null if invalid
     */
    private Date validateDateOfBirth(String dobStr, TextArea outputTextArea) {
        Date dob = parseDate(dobStr);
        if (dob == null || !dob.isValid()) {
            String message = "Patient dob: " + dobStr + " is not a valid calendar date";
            appendToTextArea(outputTextArea, message);
            return null;
        }
        if (dob.equals(getToday()) || isFutureDate(dob)) {
            String message = "Patient dob: " + dobStr + " is today or a date after today.";
            appendToTextArea(outputTextArea, message);
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
        if (display_text_area != null) {
            display_text_area.appendText(text + "\n");
        }
    }

    /**
     * Appends a message detailing the doctor's availability for an office appointment.
     *
     * @param appointmentDate       The date of the appointment.
     * @param timeslot              The timeslot for the appointment.
     * @param firstName             The first name of the patient.
     * @param lastName              The last name of the patient.
     * @param dob                   The date of birth of the patient.
     * @param doctorFirstName       The first name of the doctor.
     * @param doctorLastName        The last name of the doctor.
     * @param doctorDob             The date of birth of the doctor.
     * @param doctorLocation        The location of the doctor.
     * @param doctorSpecialty       The specialty of the doctor.
     * @param doctorNpi             The NPI of the doctor.
     * @param isAvailable           Indicates whether the doctor is available or not.
     */
    public void appendToOfficeTextArea(Date appointmentDate, Timeslot timeslot, String firstName,
                                       String lastName, Date dob, String doctorFirstName,
                                       String doctorLastName, Date doctorDob,
                                       Location doctorLocation, Specialty doctorSpecialty,
                                       String doctorNpi, boolean isAvailable) {
        String message;
        if (isAvailable) {
            message = String.format("%s %s %s %s %s [%s %s %s, %s[%s, #%s] booked.",
                    appointmentDate, timeslot, firstName, lastName, dob, doctorFirstName,
                    doctorLastName, doctorDob, doctorLocation.toString(),
                    doctorSpecialty.toString(), doctorNpi);
        } else {
            // Check if this is a duplicate appointment message
            if (firstName != null && lastName != null) {
                message = String.format("%s %s %s already has an existing appointment at slot %s.",
                        firstName, lastName, dob, timeslot.getSlotIndex());
            } else {
                message = String.format("[%s %s %s, %s[%s, #%s] is not available at slot %s.",
                        doctorFirstName, doctorLastName, doctorDob, doctorLocation.toString(),
                        doctorSpecialty.toString(), doctorNpi, timeslot.getSlotIndex());
            }
        }
        status_messages.appendText(message + "\n");
    }

    /**
     * Appends a message to the status messages area indicating that the specified doctor
     * is not available for an office appointment at the given timeslot.
     *
     * @param appointmentDate  The date of the appointment.
     * @param timeslot         The timeslot of the appointment.
     * @param firstName        The first name of the patient.
     * @param lastName         The last name of the patient.
     * @param dob              The date of birth of the patient.
     * @param doctorFirstName  The first name of the doctor.
     * @param doctorLastName   The last name of the doctor.
     * @param doctorDob        The date of birth of the doctor.
     * @param doctorLocation   The location of the doctor.
     * @param doctorSpecialty  The specialty of the doctor.
     * @param doctorNpi        The NPI of the doctor.
     * @param isAvailable      Indicates whether the doctor is available or not.
     */
    public void  appendToOfficeTextAreaUnav(Date appointmentDate, Timeslot timeslot, String firstName,
                                            String lastName, Date dob, String doctorFirstName,
                                            String doctorLastName, Date doctorDob,
                                            Location doctorLocation, Specialty doctorSpecialty,
                                            String doctorNpi, boolean isAvailable) {
        String message;
        message = String.format("[%s %s %s, %s[%s, #%s] is not available at slot %s.",
                doctorFirstName, doctorLastName, doctorDob, doctorLocation.toString(),
                doctorSpecialty.toString(), doctorNpi, timeslot.getSlotIndex());


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
    public void appendToImagingTextArea(Date appointmentDate, Timeslot timeslot, String firstName,
                                        String lastName, Date dob, String technicianFirstName,
                                        String technicianLastName, Date technicianDob,
                                        Location technicianLocation, double technicianRate,
                                        String imagingService) {
        String message;

        if (technicianFirstName == null || technicianLastName == null) {
            // This indicates either a duplicate or no technician available
            if (firstName != null && lastName != null) {
                message = String.format("%s %s %s already has an existing imaging appointment at slot %s.",
                        firstName, lastName, dob, timeslot.getSlotIndex());
            } else {
                message = String.format("Cannot find an available technician for imaging service %s at slot %s.",
                        imagingService, timeslot.getSlotIndex());
            }
        } else {
            // Format for a successful appointment
            message = String.format("%s %s %s %s %s [%s %s %s, %s[$%.2f][%s] booked.",
                    appointmentDate, timeslot, firstName, lastName, dob, technicianFirstName,
                    technicianLastName, technicianDob, technicianLocation.toString(), technicianRate, imagingService);
        }

        imaging_status_messages.appendText(message + "\n");
    }

    /**
     * Appends a message to the imaging status messages area indicating that no available technician
     * could be found for the specified imaging service at the given timeslot.
     *
     * @param appointmentDate     The date of the appointment.
     * @param timeslot            The timeslot of the appointment.
     * @param firstName           The first name of the patient.
     * @param lastName            The last name of the patient.
     * @param dob                 The date of birth of the patient.
     * @param technicianFirstName The first name of the technician.
     * @param technicianLastName  The last name of the technician.
     * @param technicianDob       The date of birth of the technician.
     * @param technicianLocation  The location of the technician.
     * @param technicianRate      The rate of the technician.
     * @param imagingService      The specific imaging service requested.
     */
    public void appendToImagingTextAreaNoTech (Date appointmentDate, Timeslot timeslot, String firstName,
                                               String lastName, Date dob, String technicianFirstName,
                                               String technicianLastName, Date technicianDob,
                                               Location technicianLocation, double technicianRate,
                                               String imagingService) {
        String message;
        message = String.format("Cannot find an available technician for imaging service %s at slot %s.",
                imagingService, timeslot.getSlotIndex());


        imaging_status_messages.appendText(message + "\n");
    }


}