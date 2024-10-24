package model.project1;
/**
 * Represents a profile with a first name, last name, and date of birth.
 * Implements Comparable to allow sorting based on last name, first name, and date of birth.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Profile implements Comparable<Profile> {
    private String fname;
    private String lname;
    private Date dob;

    /**
     * Constructs a Profile with the specified first name, last name, and date of birth.
     * @param fname The first name of the profile.
     * @param lname The last name of the profile.
     * @param dob The date of birth of the profile.
     */
    public Profile(String fname, String lname, Date dob) {
        this.fname = fname;
        this.lname = lname;
        this.dob = dob;
    }

    /**
     * Gets the first name of the profile.
     * @return The first name.
     */
    public String getFname() {
        return fname;
    }

    /**
     * Gets the last name of the profile.
     * @return The last name.
     */
    public String getLname() {
        return lname;
    }

    /**
     * Gets the date of birth of the profile.
     * @return The date of birth.
     */
    public Date getDob() {
        return dob;
    }

    /**
     * Returns the current Profile instance.
     * @return The current Profile.
     */
    public Profile getProfile() {
        return this;
    }

    /**
     * Compares this profile to another object for equality.
     * @param obj The object to compare with.
     * @return true if the profiles are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profile profile = (Profile) obj;
        return fname.equals(profile.fname) &&
                lname.equals(profile.lname) &&
                dob.equals(profile.dob);
    }

    /**
     * Compares this profile with another profile for order.
     * @param other The other profile to compare to.
     * @return A negative integer, zero, or a positive integer as this profile is less than, equal to, or greater than the specified profile.
     */
    @Override
    public int compareTo(Profile other) {
        int lastNameComp = this.lname.compareTo(other.lname);
        if (lastNameComp != 0) {
            return lastNameComp;
        }
        int firstNameComp = this.fname.compareTo(other.fname);
        if (firstNameComp != 0) {
            return firstNameComp;
        }
        return this.dob.compareTo(other.dob);
    }

    /**
     * Returns a string representation of the profile.
     * @return A string containing the first name, last name, and date of birth.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s", fname, lname, dob.toString());
    }
}