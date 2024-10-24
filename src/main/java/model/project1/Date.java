package model.project1;

/**
 * Represents a date with year, month, and day.
 * This class implements the Comparable interface to allow sorting of dates.
 * It also provides methods to validate the date and check for leap years.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Date implements Comparable<Date> {
    private int year;
    private int month;
    private int day;

    private static final int MIN_YEAR = 1;
    private static final int MIN_DAY = 1;
    private static final int DAYS_IN_FEBRUARY_NON_LEAP = 28;
    private static final int DAYS_IN_FEBRUARY_LEAP = 29;
    private static final int DAYS_IN_MONTH_30 = 30;
    private static final int DAYS_IN_MONTH_31 = 31;

    private static final int JANUARY = 1;
    private static final int FEBRUARY = 2;
    private static final int MARCH = 3;
    private static final int APRIL = 4;
    private static final int MAY = 5;
    private static final int JUNE = 6;
    private static final int JULY = 7;
    private static final int AUGUST = 8;
    private static final int SEPTEMBER = 9;
    private static final int OCTOBER = 10;
    private static final int NOVEMBER = 11;
    private static final int DECEMBER = 12;

    private static final int LEAP_YEAR_DIVISIBLE_BY = 4;
    private static final int CENTURY_DIVISIBLE_BY = 100;
    private static final int FOUR_CENTURY_DIVISIBLE_BY = 400;

    /**
     * Constructs a Date with the specified year, month, and day.
     *
     * @param year  the year of the date
     * @param month the month of the date
     * @param day   the day of the date
     */
    public Date(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Gets the year of the date.
     *
     * @return the year of the date
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the month of the date.
     *
     * @return the month of the date
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets the day of the date.
     *
     * @return the day of the date
     */
    public int getDay() {
        return day;
    }

    /**
     * Checks if the date is valid.
     *
     * @return true if the date is valid, false otherwise
     */
    public boolean isValid() {
        if (year < MIN_YEAR || month < JANUARY || month > DECEMBER || day < MIN_DAY || day > DAYS_IN_MONTH_31) {
            return false;
        }

        if (month == FEBRUARY) {
            return day <= (isLeapYear(year) ? DAYS_IN_FEBRUARY_LEAP : DAYS_IN_FEBRUARY_NON_LEAP);
        }

        if (month == APRIL || month == JUNE || month == SEPTEMBER || month == NOVEMBER) {
            return day <= DAYS_IN_MONTH_30;
        }

        return day <= DAYS_IN_MONTH_31;
    }

    /**
     * Checks if the year is a leap year.
     *
     * @param year the year to check
     * @return true if the year is a leap year, false otherwise
     */
    private boolean isLeapYear(int year) {
        return (year % LEAP_YEAR_DIVISIBLE_BY == 0 && year % CENTURY_DIVISIBLE_BY != 0) ||
                (year % FOUR_CENTURY_DIVISIBLE_BY == 0);
    }

    /**
     * Compares this date to another date.
     * Dates are compared first by year, then by month, and finally by day.
     *
     * @param other the other date to compare to
     * @return a negative integer, zero, or a positive integer as this date is less than, equal to, or greater than the specified date
     */
    @Override
    public int compareTo(Date other) {
        if (this.year != other.year) {
            return Integer.compare(this.year, other.year);
        }

        if (this.month != other.month) {
            return Integer.compare(this.month, other.month);
        }

        return Integer.compare(this.day, other.day);
    }

    /**
     * Checks if this date is equal to another object.
     * Two dates are considered equal if they have the same year, month, and day.
     *
     * @param obj the object to compare with
     * @return true if the dates are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Date other = (Date) obj;
        return year == other.year &&
                month == other.month &&
                day == other.day;
    }

    /**
     * Returns a string representation of the date in MM/DD/YYYY format.
     *
     * @return a string representation of the date
     */
    @Override
    public String toString() {
        return String.format("%d/%d/%d", month, day, year);
    }

}