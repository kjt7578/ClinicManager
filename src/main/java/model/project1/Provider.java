package model.project1;
import model.util.Person;

/**
 * Abstract class representing a provider in the medical system.
 * It extends the Person class and includes location information.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public abstract class Provider extends Person {
    private Location location;  // Provider's practice location



    /**
     * Constructor to initialize the Provider with profile and location.
     *
     * @param profile  The profile of the provider.
     * @param location The location where the provider practices.
     */
    public Provider(Profile profile, Location location) {
        super(profile);
        this.location = location;
    }

    /**
     * Abstract method to get the provider's rate per visit.
     * This must be implemented by subclasses like Doctor or Technician.
     *
     * @return The rate charged per visit.
     */
    public abstract int rate();

    /**
     * Gets the provider's practice location.
     *
     * @return The location of the provider.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the specialty billing amount for the provider.
     * Must be implemented by subclasses.
     *
     * @return the billing amount
     */
    public abstract int getSpecialtyBilling();

    @Override
    public String toString() {
        return "[" +
                profile +
                " " + location;
    }
}