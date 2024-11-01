package model.util;

import javafx.fxml.FXML;
import model.project1.Appointment;
import model.project1.List;
import model.project1.Provider;
import com.example.demo.ClinicManagerController;
import java.text.DecimalFormat;

/**
 * The Sort class provides methods for sorting appointments and providers
 * based on various criteria. It includes functionality to sort appointments
 * by date, patient details, county, and billing amounts.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Sort {
    private static ClinicManagerController controller;

    public static void setController(ClinicManagerController controllerInstance) {
        controller = controllerInstance;
    }

    private static void appendText(String text) {
        if (controller != null) {
            controller.appendToDisplayTextArea(text + "\n");
        }
    }


    /**
     * Sorts the list of appointments based on the provided key.
     *
     * @param list the list of appointments to be sorted
     * @param key  the sorting key (A, P, L, O, I, S, or C)
     * @throws IllegalArgumentException if the key is invalid
     */
    public static void appointment(List<Appointment> list, char key) {
        if (list == null || list.size() == 0) {
            appendText("Schedule calendar is empty.");
            return;
        }
        switch (key) {
            case 'A':
                sortAppointmentsByDateTimeProvider(list);
                printAppointments(list, "List of appointments, ordered by date/time/provider.");
                break;
            case 'P':
                sortAppointmentsByPatient(list);
                printAppointments(list, "Appointments ordered by patient/date/time");
                break;
            case 'L':
                sortAppointmentsByCountyDateTime(list);
                printAppointments(list, "List of appointments, ordered by county/date/time.");
                break;
            case 'O':
                sortNonTechnicianAppointmentsByCountyDateTime(list);
                printNonTechnicianAppointments(list, "** List of office appointments, ordered by county/date/time.");
                appendText("** end of list **");
                break;
            case 'I':
                sortImagingAppointmentsByCountyDateTime(list);
                printImagingAppointments(list, "** List of radiology appointments, ordered by county/date/time.");
                appendText("** end of list **");
                break;
            case 'S':
                sortAppointmentsByPatientAndPrintBilling(list);
                break;
            case 'C':
                sortAppointmentsByLastname(list);
                printAppointmentsByCredit(list);
                break;
            default:
                throw new IllegalArgumentException("Invalid sorting key: " + key);
        }
    }


    /**
     * Sorts appointments by patient details and prints a billing statement for each patient.
     *
     * @param list the list of appointments to be sorted and processed
     */
    private static void sortAppointmentsByPatientAndPrintBilling(List<Appointment> list) {
        // First, sort the list by patient details
        sortAppointmentsByPatient(list);
        String[] patientNames = new String[list.size()];
        double[] totalDueAmounts = new double[list.size()];
        int count = 0;

        // Aggregate bills for each patient
        for (Appointment appointment : list) {
            String patientName = appointment.getPatient().getProfile().getLname() + " " +
                    appointment.getPatient().getProfile().getFname() + " " +
                    appointment.getPatient().getProfile().getDob();
            double dueAmount = getDueAmount(appointment); // Assume this method returns the due amount for the appointment

            boolean found = false;
            for (int j = 0; j < count; j++) {
                if (patientNames[j].equals(patientName)) {
                    totalDueAmounts[j] += dueAmount; // Combine the amounts
                    found = true;
                    break;
                }
            }
            if (!found) {
                patientNames[count] = patientName; // Add new patient
                totalDueAmounts[count] = dueAmount; // Initialize with due amount
                count++;
            }
        }
        DecimalFormat df = new DecimalFormat("#,###.00");

        for (int i = 0; i < count; i++) {
            String line = String.format("(%d) %s [due: $%s]", (i + 1), patientNames[i], df.format(totalDueAmounts[i]));
            appendText(line);
        }

        // Clear the list of appointments after printing
        clearList(list);
    }

    /**
     * Retrieves the due amount for a given appointment based on the provider's specialty billing.
     *
     * @param appointment the appointment for which to retrieve the due amount
     * @return the due amount for the appointment
     */
    private static double getDueAmount(Appointment appointment) {
        // Assuming each provider has a billing amount and it's retrievable
        Provider provider = (Provider) appointment.getProvider();
        return provider.getSpecialtyBilling(); // Modify this logic based on how you calculate the due amount
    }

    /**
     * Clears the list of appointments.
     *
     * @param list the list of appointments to be cleared
     */
    private static void clearList(List<Appointment> list) {
        // Clear the list of appointments
        for (int i = list.size() - 1; i >= 0; i--) {
            list.remove(list.get(i)); // Safely remove appointments from the end to avoid shifting issues
        }
    }

    /**
     * Sorts appointments by provider's last name.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsByLastname(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                Provider provider1 = (Provider) a1.getProvider();
                Provider provider2 = (Provider) a2.getProvider();

                // Assuming you have a method to get the last name of the provider
                String lastName1 = provider1.getLastName(); // Replace with actual method to get last name
                String lastName2 = provider2.getLastName(); // Replace with actual method to get last name

                if (lastName1.compareTo(lastName2) > 0) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Prints a credit statement for appointments based on provider specialty billing amounts.
     *
     * @param list the list of appointments to process
     */
    private static void printAppointmentsByCredit(List<Appointment> list) {
        Appointment[] tempAppointments = new Appointment[list.size()];
        double[] totalCredits = new double[list.size()];
        int count = 0;

        for (int i = 0; i < list.size(); i++) {
            Appointment appointment = list.get(i);
            if (appointment.getProvider() instanceof Provider) {
                Provider provider = (Provider) appointment.getProvider();
                double creditAmount = provider.getSpecialtyBilling();

                boolean found = false;
                for (int j = 0; j < count; j++) {
                    if (tempAppointments[j].getProvider().equals(provider)) {
                        totalCredits[j] += creditAmount;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tempAppointments[count] = appointment;
                    totalCredits[count] = creditAmount;
                    count++;
                }
            }
        }

        appendText("** Credit amount ordered by provider.");
        for (int i = 0; i < count; i++) {
            Provider provider = (Provider) tempAppointments[i].getProvider();
            double totalCredit = totalCredits[i]; // Use the aggregated total credit
            String line = String.format("(%d) %s [credit amount: $%.2f]", (i + 1), provider.getProfile(), totalCredit);
            appendText(line);
        }
        appendText("** end of list **");
    }

    /**
     * Sorts non-technician appointments by county, date, and time.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortNonTechnicianAppointmentsByCountyDateTime(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                if (!(a1.getProvider() instanceof Technician) && !(a2.getProvider() instanceof Technician)) {
                    if (shouldSwapByCountyDateTime(a1, a2)) {
                        swap(list, i, j);
                    }
                }
            }
        }
    }

    /**
     * Prints only the non-technician appointments from the list.
     *
     * @param list  the list of appointments to print
     * @param label the label or message to display before printing
     */
    private static void printNonTechnicianAppointments(List<Appointment> list, String label) {
        appendText(label);
        boolean hasNonTechnicianAppointments = false;

        for (Appointment appointment : list) {
            // Print only if the provider is NOT a Technician
            if (!(appointment.getProvider() instanceof Technician)) {
                appendText(String.valueOf(appointment));
                hasNonTechnicianAppointments = true;
            }
        }
        if (!hasNonTechnicianAppointments) {
            appendText("No non-technician office appointments available.");
        }
    }

    /**
     * Sorts only imaging appointments by county, date, time, and provider's first name.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortImagingAppointmentsByCountyDateTime(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                // Check if both appointments are instances of Imaging
                if (a1 instanceof Imaging && a2 instanceof Imaging) {
                    if (shouldSwapImaging(a1, a2)) {
                        swap(list, i, j);
                    }
                }
            }
        }
    }

    /**
     * Determines whether two imaging appointments should be swapped based on county, date, time, and provider's first name.
     *
     * @param a1 the first appointment
     * @param a2 the second appointment
     * @return true if a1 should be placed after a2; false otherwise
     */
    private static boolean shouldSwapImaging(Appointment a1, Appointment a2) {
        // Cast to Imaging appointments
        Imaging imaging1 = (Imaging) a1;
        Imaging imaging2 = (Imaging) a2;

        // Cast provider to Technician (or Provider if necessary)
        Technician provider1 = (Technician) imaging1.getProvider();
        Technician provider2 = (Technician) imaging2.getProvider();

        // Compare by county first
        int countyComparison = provider1.getLocation().getCounty().compareTo(provider2.getLocation().getCounty());
        if (countyComparison > 0) return true;
        if (countyComparison < 0) return false;

        // If counties are the same, compare by date
        int dateComparison = imaging1.getDate().compareTo(imaging2.getDate());
        if (dateComparison > 0) return true;
        if (dateComparison < 0) return false;

        // If dates are the same, compare by timeslot
        int timeslotComparison = imaging1.getTimeslot().compareTo(imaging2.getTimeslot());
        if (timeslotComparison > 0) return true;
        if (timeslotComparison < 0) return false;

        // If all the above are the same, compare by provider's first name
        return provider1.getFirstName().compareTo(provider2.getFirstName()) > 0;
    }

    /**
     * Prints only the imaging appointments from the list.
     *
     * @param list  the list of appointments to print
     * @param label the label or message to display before printing
     */
    private static void printImagingAppointments(List<Appointment> list, String label) {
        appendText(label);
        boolean hasImagingAppointments = false;

        for (Appointment appointment : list) {
            if (appointment instanceof Imaging) {
                appendText(String.valueOf(appointment));
                hasImagingAppointments = true;
            }
        }

        if (!hasImagingAppointments) {
            appendText("No imaging appointments available.");
        }
    }

    /**
     * Sorts appointments by date, time slot, and provider's last name.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsByDateTimeProvider(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                if (shouldSwapByDateTimeProvider(a1, a2)) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Sorts appointments by patient's last name, first name, date of birth, appointment date, and time.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsByPatient(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                if (shouldSwapByPatient(a1, a2)) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Sorts appointments by county, date, and time.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsByCountyDateTime(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                if (shouldSwapByCountyDateTime(a1, a2)) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Sorts appointments by provider's specialty billing amount.
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsBySpecialtyBilling(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                Provider provider1 = (Provider) a1.getProvider();
                Provider provider2 = (Provider) a2.getProvider();

                if (provider1.getSpecialtyBilling() > provider2.getSpecialtyBilling()) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Sorts appointments by provider profile (last name).
     *
     * @param list the list of appointments to be sorted
     */
    private static void sortAppointmentsByProviderProfile(List<Appointment> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Appointment a1 = list.get(i);
                Appointment a2 = list.get(j);

                if (a1.getProvider().getProfile().compareTo(a2.getProvider().getProfile()) > 0) {
                    swap(list, i, j);
                }
            }
        }
    }

    /**
     * Swaps two appointments in the list.
     *
     * @param list the list containing the appointments
     * @param i    the index of the first appointment
     * @param j    the index of the second appointment
     */
    private static void swap(List<Appointment> list, int i, int j) {
        Appointment temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    /**
     * Determines whether two appointments should be swapped based on date, time, and provider.
     *
     * @param a1 the first appointment
     * @param a2 the second appointment
     * @return true if the appointments should be swapped, false otherwise
     */
    private static boolean shouldSwapByDateTimeProvider(Appointment a1, Appointment a2) {
        if (a1.getDate().compareTo(a2.getDate()) > 0) return true;
        if (a1.getDate().equals(a2.getDate()) && a1.getTimeslot().compareTo(a2.getTimeslot()) > 0) return true;
        return a1.getDate().equals(a2.getDate()) && a1.getTimeslot().equals(a2.getTimeslot()) &&
                a1.getProvider().getProfile().getLname().compareTo(a2.getProvider().getProfile().getLname()) > 0;
    }

    /**
     * Determines whether two appointments should be swapped based on the patient's details.
     *
     * @param a1 the first appointment
     * @param a2 the second appointment
     * @return true if the appointments should be swapped, false otherwise
     */
    private static boolean shouldSwapByPatient(Appointment a1, Appointment a2) {
        if (a1.getPatient().getProfile().getLname().compareTo(a2.getPatient().getProfile().getLname()) > 0) return true;
        if (a1.getPatient().getProfile().getLname().equals(a2.getPatient().getProfile().getLname()) &&
                a1.getPatient().getProfile().getFname().compareTo(a2.getPatient().getProfile().getFname()) > 0) return true;
        if (a1.getPatient().getProfile().getLname().equals(a2.getPatient().getProfile().getLname()) &&
                a1.getPatient().getProfile().getFname().equals(a2.getPatient().getProfile().getFname()) &&
                a1.getPatient().getProfile().getDob().compareTo(a2.getPatient().getProfile().getDob()) > 0) return true;
        return a1.getPatient().getProfile().getLname().equals(a2.getPatient().getProfile().getLname()) &&
                a1.getPatient().getProfile().getFname().equals(a2.getPatient().getProfile().getFname()) &&
                a1.getPatient().getProfile().getDob().equals(a2.getPatient().getProfile().getDob()) &&
                a1.getDate().compareTo(a2.getDate()) > 0;
    }

    /**
     * Determines whether two appointments should be swapped based on county, date, and time.
     *
     * @param a1 the first appointment
     * @param a2 the second appointment
     * @return true if the appointments should be swapped, false otherwise
     */
    private static boolean shouldSwapByCountyDateTime(Appointment a1, Appointment a2) {
        Provider provider1 = (Provider) a1.getProvider();
        Provider provider2 = (Provider) a2.getProvider();

        int countyComparison = provider1.getLocation().getCounty().compareTo(provider2.getLocation().getCounty());
        if (countyComparison > 0) return true;
        if (countyComparison == 0 && a1.getDate().compareTo(a2.getDate()) > 0) return true;
        return countyComparison == 0 && a1.getDate().equals(a2.getDate()) &&
                a1.getTimeslot().compareTo(a2.getTimeslot()) > 0;
    }

    /**
     * Prints the sorted list of appointments with a header.
     *
     * @param list   the list of appointments to print
     * @param header the header to display before the appointments
     */
    private static void printAppointments(List<Appointment> list, String header) {
        appendText("** " + header);
        for (Appointment appointment : list) {
            appendText(String.valueOf(appointment));
        }
        appendText("** end of list **");
    }

    /**
     * Sorts the list of providers by their last name.
     *
     * @param list the list of providers to be sorted
     */
    public static void provider(List<Provider> list) {
        // Sort providers by last name
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Provider p1 = list.get(i);
                Provider p2 = list.get(j);

                if (p1.getProfile().getLname().compareTo(p2.getProfile().getLname()) > 0) {
                    list.set(i, p2);
                    list.set(j, p1);
                }
            }
        }
    }
}