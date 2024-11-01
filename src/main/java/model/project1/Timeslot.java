package model.project1;

/**
 * Represents a timeslot with a specific hour and minute.
 * Provides methods to get the hour and minute of each timeslot,
 * check if the timeslot is in the morning, and compare timeslots.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class Timeslot implements Comparable<Timeslot> {
    private int hour;
    private int minute;

    public static final int BASE_HOUR_MORNING = 9;  // 9:00 AM
    public static final int BASE_HOUR_AFTERNOON = 14;  // 2:00 PM
    public static final int SLOT_DURATION_MINUTES = 30;  // Slot duration in minutes
    public static final int MORNING_END_HOUR = 12;  // Noon time for morning ending
    public static final int MORNING_SLOTS_COUNT = 6;  // Number of slots in the morning
    public static final int AFTERNOON_SLOT_START_INDEX = 7;  // Afternoon slot index starts at 7
    public static final int MINUTES_PER_HOUR = 60;  // Minutes in one hour
    public static final int TWELVE_HOUR_CLOCK = 12;  // 12-hour format
    public static final int MIN_SLOT_INDEX = 1;
    public static final int MAX_SLOT_INDEX = 12;
    public static final int ZERO_INDEX = 0;

    /**
     * Constructs a Timeslot with the specified hour and minute.
     *
     * @param hour the hour of the timeslot
     * @param minute the minute of the timeslot
     */
    public Timeslot(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * Gets the slot index for this timeslot.
     *
     * @return the slot index corresponding to the current timeslot
     */
    public int getSlotIndex() {
        if (hour >= BASE_HOUR_MORNING && hour < MORNING_END_HOUR) {
            int totalMinutes = (hour - BASE_HOUR_MORNING) * MINUTES_PER_HOUR + minute;
            return totalMinutes / SLOT_DURATION_MINUTES + MIN_SLOT_INDEX;
        } else if (hour >= BASE_HOUR_AFTERNOON) {
            int totalMinutes = (hour - BASE_HOUR_AFTERNOON) * MINUTES_PER_HOUR + minute;
            return totalMinutes / SLOT_DURATION_MINUTES + AFTERNOON_SLOT_START_INDEX;
        }
        throw new IllegalArgumentException("Invalid time: " + this.toString());
    }

    /**
     * Checks if the timeslot is in the morning.
     *
     * @return true if the timeslot is in the morning, false otherwise
     */
    public boolean isMorning() {
        return hour < 12;
    }

    /**
     * Converts a string to a timeslot.
     * The string should correspond to a slot from "1" to "12",
     * where "1" represents 9:00 AM and "12" represents 5:00 PM.
     *
     * @param slotStr the string representation of the timeslot
     * @return the corresponding timeslot
     * @throws IllegalArgumentException if the string does not match any timeslot
     */
    public static Timeslot fromString(String slotStr) {
        int slotIndex = Integer.parseInt(slotStr);

        if (slotIndex < MIN_SLOT_INDEX || slotIndex > MAX_SLOT_INDEX) {
            throw new IllegalArgumentException("Invalid timeslot: " + slotStr);
        }

        if (slotIndex <= MORNING_SLOTS_COUNT) {
            int baseHour = BASE_HOUR_MORNING;
            int totalMinutes = (slotIndex - MIN_SLOT_INDEX) * SLOT_DURATION_MINUTES;
            int hour = baseHour + totalMinutes / MINUTES_PER_HOUR;
            int minute = totalMinutes % MINUTES_PER_HOUR;
            return new Timeslot(hour, minute);
        } else {
            int baseHour = BASE_HOUR_AFTERNOON;
            int totalMinutes = (slotIndex - AFTERNOON_SLOT_START_INDEX) * SLOT_DURATION_MINUTES;
            int hour = baseHour + totalMinutes / MINUTES_PER_HOUR;
            int minute = totalMinutes % MINUTES_PER_HOUR;
            return new Timeslot(hour, minute);
        }
    }

    /**
     * Compares this timeslot to another timeslot for ordering purposes.
     *
     * @param other the timeslot to compare to
     * @return a negative integer, zero, or a positive integer as this timeslot is less than,
     * equal to, or greater than the specified timeslot
     */
    @Override
    public int compareTo(Timeslot other) {
        if (this.hour != other.hour) {
            return Integer.compare(this.hour, other.hour);
        }
        return Integer.compare(this.minute, other.minute);
    }

    /**
     * Checks if two timeslots are equal based on their hour and minute.
     *
     * @param obj the object to compare with
     * @return true if the timeslots are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Timeslot timeslot = (Timeslot) obj;
        return hour == timeslot.hour && minute == timeslot.minute;
    }

    /**
     * Returns a string representation of the timeslot.
     *
     * @return a string representation of the timeslot
     */
    @Override
    public String toString() {
        int displayHour = hour % TWELVE_HOUR_CLOCK;
        if (displayHour == ZERO_INDEX) {
            displayHour = TWELVE_HOUR_CLOCK;
        }
        String amPm = isMorning() ? "AM" : "PM";
        return String.format("%d:%02d %s", displayHour, minute, amPm);
    }
}