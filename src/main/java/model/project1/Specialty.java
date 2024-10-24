package model.project1;
/**
 * Enum representing different medical specialties along with their associated charges.
 * Each specialty has a specific charge associated with it.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public enum Specialty {
    FAMILY(250),
    PEDIATRICIAN(300),
    ALLERGIST(350);

    private final int charge;

    /**
     * Constructs a Specialty with the specified charge.
     * @param charge The charge for the specialty.
     */
    Specialty(int charge) {
        this.charge = charge;
    }

    /**
     * Gets the charge associated with the specialty.
     * @return The charge for the specialty.
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Returns a string representation of the specialty.
     * @return The name of the specialty.
     */
    @Override
    public String toString() {
        return name();
    }
}