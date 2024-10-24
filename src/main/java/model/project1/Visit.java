package model.project1;
/**
 * Represents a visit with an appointment and a reference to the next visit.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Visit {
    private Appointment appointment;
    private Visit next;

    /**
     * Constructs a Visit with the specified appointment.
     * Initializes the next visit to null.
     *
     * @param appointment the appointment for this visit
     */
    public Visit(Appointment appointment){
        this.appointment = appointment;
        this.next = null;
    }
}