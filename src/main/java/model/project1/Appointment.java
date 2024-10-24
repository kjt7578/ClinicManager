package model.project1;
import model.util.Person;

/**
 * Represents an appointment with a date, timeslot, patient, and provider.
 * This class allows for the creation and management of appointments,
 * including comparisons for sorting purposes.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Appointment implements Comparable<Appointment> {
    protected Date date;
    protected Timeslot timeslot;
    protected Person patient;
    protected Person provider;

    /**
     * Constructs an Appointment object with the specified date, timeslot, patient, and provider.
     *
     * @param date     the date of the appointment as a string
     * @param timeslot the timeslot of the appointment
     * @param patient  the patient involved in the appointment
     * @param provider the provider involved in the appointment
     */
    public Appointment(Date date, Timeslot timeslot, Person patient, Person provider) {
        this.date = date;
        this.timeslot = timeslot;
        this.patient = patient;
        this.provider = provider;
    }

    /**
     * Returns the date of the appointment.
     *
     * @return the date of the appointment as a string
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date of the appointment.
     *
     * @param date the date to set as a string
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the timeslot of the appointment.
     *
     * @return the timeslot of the appointment
     */
    public Timeslot getTimeslot() {
        return timeslot;
    }

    /**
     * Returns the patient of the appointment.
     *
     * @return the patient involved in the appointment
     */
    public Person getPatient() {
        return patient;
    }

    /**
     * Returns the provider of the appointment.
     *
     * @return the provider involved in the appointment
     */
    public Person getProvider() {
        return provider;
    }

    /**
     * Sets the provider of the appointment.
     *
     * @param provider the provider to set
     */
    public void setProvider(Person provider) {
        this.provider = provider;
    }

    /**
     * Checks whether this appointment is equal to another object.
     * Appointments are considered equal if their date, timeslot, and patient are equal.
     *
     * @param obj the object to compare with
     * @return true if the appointments are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Appointment that = (Appointment) obj;

        boolean dateEqual = (this.date == null && that.date == null) || (this.date != null && this.date.equals(that.date));
        boolean timeslotEqual = (this.timeslot == null && that.timeslot == null) || (this.timeslot != null && this.timeslot.equals(that.timeslot));
        boolean patientEqual = (this.patient == null && that.patient == null) || (this.patient != null && this.patient.equals(that.patient));

        return dateEqual && timeslotEqual && patientEqual;
    }

    /**
     * Compares this appointment with another for ordering purposes.
     * Appointments are compared first by date, and if the dates are equal, by timeslot.
     *
     * @param other the appointment to compare to
     * @return a negative integer, zero, or a positive integer as this appointment
     *         is less than, equal to, or greater than the specified appointment
     */
    @Override
    public int compareTo(Appointment other) {
        int dateComparison = this.date.compareTo(other.date);
        if (dateComparison != 0) {
            return dateComparison;
        }

        return this.timeslot.compareTo(other.timeslot);
    }

    /**
     * Returns a string representation of the appointment.
     *
     * @return a string representation of the appointment
     */
    @Override
    public String toString() {
        String providerInfo = (provider != null) ? provider.toString() : "No provider assigned";
        return String.format("%s %s %s %s",
                date.toString(),
                timeslot.toString(),
                patient.toString(),
                providerInfo);
    }
}