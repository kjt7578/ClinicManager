package model.util;

import model.project1.Appointment;
import model.project1.Specialty;
import model.project1.Profile;
import model.project1.Location;
import model.project1.List;
import model.project1.Date;
import model.project1.Timeslot;
import model.project1.Patient;
import model.project1.Provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Calendar;

/**
 * ClinicManager class processes user commands and interacts with the scheduling system.
 * It manages office and imaging appointments and handles input commands from the terminal.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class ClinicManager {

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
     * File path for provider data.
     */
    private static final String PROVIDERS_FILE_PATH = "src/util/providers.txt";

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

    /**
     * Constructs a ClinicManager object and initializes the lists and variables.
     * Loads provider data and prints the technician rotation list.
     * Initializes the technician assignment matrix based on the number of technicians.
     */
    public ClinicManager() {
        providerList = new List<>();
        appointmentList = new List<>();
        technicianRotationList = new List<>();
        technicianRotationIndex = INITIAL_ROTATION_INDEX;

        loadProviders();
        printTechnicianRotation();

        int MAX_TECHNICIANS = technicianRotationList.size();
        technicianAssigned = new boolean[MAX_TIMESLOTS][MAX_TECHNICIANS];
    }

    /**
     * Runs the Clinic Manager and processes user commands from the terminal.
     */
    public void run() {
        System.out.println("Clinic Manager is running...");
        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            command = scanner.nextLine().trim();
            if (command.equals("Q")) {
                System.out.println("Clinic Manager terminated.");
                break;
            }
            processCommand(appointmentList, command);
        }
        scanner.close();
    }

    /**
     * Loads providers from the file and sorts them.
     */
    private void loadProviders() {
        try (Scanner scanner = new Scanner(new File(PROVIDERS_FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Provider provider = parseProvider(line);
                    if (provider != null) {
                        providerList.add(provider);
                    }
                }
            }
            createTechnicianRotation();
            Sort.provider(providerList);
            displayProviders();
        } catch (FileNotFoundException e) {
            System.out.println(PROVIDERS_FILE_PATH + " cannot be found.");
        }
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
     * Parses a provider's information from a given line of text.
     *
     * @param line The line of text containing provider information.
     * @return A Provider object (Doctor or Technician) or null if the provider type is invalid.
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
                processImagingAppointment(tokens);
                break;
            case "C":
                processCancellation(tokens);
                break;
            case "R":
                processReschedule(tokens);
                break;
            default:
                System.out.println("Invalid appointment command!");
                break;
        }
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
                System.out.println("Invalid command!");
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
     * Processes an imaging appointment based on the provided tokens.
     *
     * @param tokens an array of strings containing appointment details,
     *               including date, timeslot, patient information, and imaging service
     */
    private void processImagingAppointment(String[] tokens) {
        if (!validateTokenLength(tokens)) return;

        String imagingService = tokens[6].toUpperCase();
        if (!validateImagingService(imagingService)) return;

        try {
            Date appointmentDate = validateAppointmentDate(tokens[1]);
            Timeslot timeslot = validateTimeslot(tokens[2]);
            Date dob = validateDateOfBirth(tokens[5]);
            if (!validateInputs(appointmentDate, timeslot, dob)) return;

            handleImagingAppointment(tokens, appointmentDate, timeslot, dob, imagingService);
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

    /**
     * Handles the creation and validation of an imaging appointment.
     *
     * @param tokens          the array of appointment tokens
     * @param appointmentDate the validated appointment date
     * @param timeslot        the validated timeslot
     * @param dob             the patient's date of birth
     * @param imagingService  the imaging service to use for the appointment
     */
    private void handleImagingAppointment(String[] tokens, Date appointmentDate, Timeslot timeslot, Date dob, String imagingService) {
        String firstName = tokens[3];
        String lastName = tokens[4];
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
     * Processes the cancellation of an appointment based on the provided details.
     *
     * @param tokens an array of strings containing the cancellation command details,
     *               including appointment date, timeslot, first name, last name, and date of birth
     */
    private void processCancellation(String[] tokens) {
        if (tokens.length != TOKEN_LENGTH_CANCEL) {
            System.out.println("Missing data tokens.");
            return;
        }
        try {
            Date appointmentDate = validateAppointmentDate(tokens[1]);
            Timeslot timeslot = validateTimeslot(tokens[2]);
            String firstName = tokens[3];
            String lastName = tokens[4];
            Date dob = validateDateOfBirth(tokens[5]);

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
     * Processes the rescheduling of an appointment based on the provided details.
     *
     * @param tokens an array of strings containing the rescheduling command details,
     *               including appointment date, old timeslot, first name, last name,
     *               date of birth, and new timeslot
     */
    private void processReschedule(String[] tokens) {
        if (!validateTokenLength(tokens)) return;

        try {
            Date appointmentDate = validateAppointmentDate(tokens[1]);
            Timeslot oldSlot = validateTimeslot(tokens[2]);
            Date dob = validateDateOfBirth(tokens[5]);
            Timeslot newSlot = validateTimeslot(tokens[6]);
            if (!validateInputs(appointmentDate, oldSlot, dob, newSlot)) return;

            handleRescheduling(tokens, appointmentDate, oldSlot, dob, newSlot);
        } catch (Exception e) {
            System.out.println("Missing data tokens.");
        }
    }

    /**
     * Handles the rescheduling process.
     *
     * @param tokens          an array of strings containing the rescheduling command details
     * @param appointmentDate the appointment date
     * @param oldSlot         the original timeslot of the appointment
     * @param dob             the patient's date of birth
     * @param newSlot         the new timeslot to reschedule to
     */
    private void handleRescheduling(String[] tokens, Date appointmentDate, Timeslot oldSlot, Date dob, Timeslot newSlot) {
        String firstName = tokens[3];
        String lastName = tokens[4];

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
}
