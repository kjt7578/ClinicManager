package model.project1;
import model.util.Person;

/**
 * The Patient class extends the Person class and manages a list of visits.
 * It includes methods to add visits, compute charges, and manage patient information.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Patient extends Person{
    private Visit visit; //a linked list of visits (completed appt.)

    /**
     * Constructor for the Patient class.
     *
     * @param profile The patient's profile (inherited from Person)
     */
    public Patient(Profile profile) {
        super(profile);  // Call the superclass constructor
        this.visit = null;  // Initialize visits as null (no visits initially)
    }

    /**
     * Gets the profile of the patient.
     *
     * @return the profile of the patient
     */
    public Profile getProfile(){
        return profile;
    }

    /**
     * Checks if this patient is equal to another object.
     * Two patients are considered equal if they have the same profile.
     *
     * @param obj the object to compare with
     * @return true if the patients are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Patient that = (Patient) obj;
        return this.profile.equals(that.profile);
    }

    /**
     * Compares this patient to another patient.
     * Patients are compared by their profiles.
     *
     * @param other the other patient to compare to
     * @return a negative integer, zero, or a positive integer as this patient is less than, equal to, or greater than the specified patient
     */
    @Override
    public int compareTo(Person other) {
        // Compare by profile, assuming Profile class has its own compareTo implementation
        return this.profile.compareTo(other.getProfile());
    }

    /**
     * Returns a string representation of the patient.
     *
     * @return a string representation of the patient
     */
    @Override
    public String toString(){
        return profile.toString();
    }
}