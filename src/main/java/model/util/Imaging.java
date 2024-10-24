package model.util;

import model.project1.Appointment;
import model.project1.Date;
import model.project1.Timeslot;

/**
 * Represents an imaging appointment in the healthcare system.
 * The Imaging class extends the Appointment class and includes an additional
 * attribute for the radiology room where the imaging procedure takes place.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Imaging extends Appointment {
    private Radiology room;

    /**
     * Constructs an Imaging appointment with the specified date, timeslot, patient, provider, and radiology room.
     *
     * @param date      the date of the appointment as a string
     * @param timeslot  the timeslot of the appointment
     * @param patient   the patient involved in the appointment
     * @param provider  the provider involved in the appointment
     * @param room      the radiology room where the imaging will take place
     */
    public Imaging(Date date, Timeslot timeslot, Person patient, Technician provider, Radiology room) {
        super(date, timeslot, patient, provider);
        this.room = room;
    }

    /**
     * Returns the radiology room for the imaging appointment.
     *
     * @return the radiology room associated with the appointment
     */
    public Radiology getRoom() {
        return room;
    }

    /**
     * Returns a string representation of the imaging appointment, including the room information.
     *
     * @return a string representation of the imaging appointment
     */
    @Override
    public String toString() {
        return String.format("%s[%s]", super.toString(), room);
    }
}