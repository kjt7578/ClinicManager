package model.util;

import model.project1.Date;
import model.project1.Profile;

/**
 * Person class represents a general profile for patients and providers.
 * This class will be extended by the Patient and Provider classes.
 *
 * @author Jeongtae Kim and Stephen Kwok
 */
public class Person implements Comparable<Person>{
    protected Profile profile;

    /**
     * Constructor to initialize a Person with a given Profile.
     *
     * @param profile Profile of the person
     */
    public Person(Profile profile){
        this.profile = profile;
    }

    /**
     * Compares this person with another person based on the profile.
     *
     * @param other The other person to compare to
     */
    @Override
    public int compareTo(Person other){
        return this.profile.compareTo(other.profile);
    }

    /**
     * Checks if two Person objects are equal based on their profiles.
     *
     * @param obj The object to compare to
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        Person person = (Person) obj;
        return profile.equals(person.profile);
    }

    /**
     * Provides a string representation of the Person object.
     *
     * @return The string representation of the Person
     */
    @Override
    public String toString(){
        return profile.toString();
    }

    /**
     * Getter for the profile.
     *
     * @return the profile of the person
     */
    public Profile getProfile() {
        return this.profile;
    }

    /**
     * Getter for the first name.
     * Uses the Profile's getFname() method.
     *
     * @return the first name of the person
     */
    public String getFirstName() {
        return profile.getFname();  // Use Profile's getter
    }

    /**
     * Getter for the last name.
     * Uses the Profile's getLname() method.
     *
     * @return the last name of the person
     */
    public String getLastName() {
        return profile.getLname();  // Use Profile's getter
    }

    /**
     * Getter for the date of birth.
     * Uses the Profile's getDob() method.
     *
     * @return the date of birth of the person
     */
    public Date getDateOfBirth() {
        return profile.getDob();  // Use Profile's getter
    }
}