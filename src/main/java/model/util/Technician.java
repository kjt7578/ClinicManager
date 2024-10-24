package model.util;

import model.project1.Location;
import model.project1.Profile;
import model.project1.Provider;

/**
 * The Technician class represents a technician provider in the healthcare system.
 * It extends the Provider class and includes additional attributes specific to a technician,
 * such as the rate charged per visit.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Technician extends Provider {
    private int ratePerVisit;

    /**
     * Constructs a Technician object with the specified profile, location, and rate per visit.
     *
     * @param profile      the profile of the technician
     * @param location     the location where the technician works
     * @param ratePerVisit the rate charged by the technician per visit
     */
    public Technician(Profile profile, Location location, int ratePerVisit) {
        super(profile, location);
        this.ratePerVisit = ratePerVisit;
    }

    /**
     * Returns the rate per visit for the technician.
     *
     * @return the rate charged by the technician per visit
     */
    public int getRatePerVisit() {
        return ratePerVisit;
    }

    /**
     * Returns the specialty billing amount for the technician.
     * In this case, it is simply the rate per visit.
     *
     * @return the billing amount for the technician
     */
    @Override
    public int getSpecialtyBilling() {
        return ratePerVisit; // Directly return the rate per visit
    }

    /**
     * Returns the rate charged for the technician's services.
     * This method overrides the rate method in the Provider class.
     *
     * @return the rate charged by the technician per visit
     */
    @Override
    public int rate() {
        return ratePerVisit;
    }
}