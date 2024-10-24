package model.util;

import model.project1.Location;
import model.project1.Profile;
import model.project1.Provider;
import model.project1.Specialty;

/**
 * Represents a doctor in the healthcare system.
 * The Doctor class extends the Provider class and includes additional
 * attributes such as specialty and npi.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Doctor extends Provider {
    private Specialty specialty;
    private String npi;

    /**
     * Constructs a Doctor object with the specified profile, location, specialty, and NPI.
     *
     * @param profile   the profile of the doctor
     * @param location  the location where the doctor works
     * @param specialty the doctor's specialty
     * @param npi       the doctor's National Provider Identifier (NPI)
     */
    public Doctor(Profile profile, Location location, Specialty specialty, String npi) {
        super(profile, location);
        this.specialty = specialty;
        this.npi = npi;
    }

    /**
     * Returns the specialty of the doctor.
     *
     * @return the specialty of the doctor
     */
    public Specialty getSpecialty() {
        return specialty;
    }

    /**
     * Returns the National Provider Identifier (NPI) of the doctor.
     *
     * @return the NPI of the doctor
     */
    public String getNpi() {
        return npi;
    }

    /**
     * Calculates the rate for the doctor based on their specialty.
     * This method overrides the rate method in the Provider class.
     *
     * @return the rate or charge based on the doctor's specialty
     */
    @Override
    public int rate() {
        return specialty.getCharge(); // Assuming Specialty has a method to get the rate per visit
    }

    /**
     * Returns the specialty billing amount for the doctor.
     * This assumes the specialty has a method to get the billing amount.
     *
     * @return the billing amount
     */
    @Override
    public int getSpecialtyBilling() {
        return specialty.getCharge(); // Assuming getCharge() returns the billing amount
    }

    /**
     * Returns a string representation of the Doctor object.
     *
     * @return a string containing the doctor's details
     */
    @Override
    public String toString() {
        return "[" +
                profile +
                ", " + getLocation() +
                "[" +
                specialty + ", " +
                "#" + npi + "]";
    }
}