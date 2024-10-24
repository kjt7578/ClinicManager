package model.project1;
/**
 * Represents a location with a county and zip code.
 * This enum provides methods to get the county and zip code of each location.
 * It also overrides the toString method to provide a string representation of the location.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public enum Location {
    BRIDGEWATER("Somerset", "08807"),
    EDISON("Middlesex", "08817"),
    PISCATAWAY("Middlesex", "08854"),
    PRINCETON("Mercer", "08542"),
    MORRISTOWN("Morris", "07960"),
    CLARK("Union", "07066");

    private final String county;
    private final String zip;

    /**
     * Constructs a Location with the specified county and zip code.
     *
     * @param county the county of the location
     * @param zip the zip code of the location
     */
    Location(String county, String zip) {
        this.county = county;
        this.zip = zip;
    }

    /**
     * Gets the county of the location.
     *
     * @return the county of the location
     */
    public String getCounty() {
        return county;
    }

    /**
     * Returns a string representation of the location.
     *
     * @return a string representation of the location
     */
    @Override
    public String toString() {
        return name() + ", " + county + " " + zip + "]";
    }


}