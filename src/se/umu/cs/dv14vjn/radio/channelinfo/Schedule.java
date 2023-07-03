package se.umu.cs.dv14vjn.radio.channelinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Schedule object that stores program objects in an array
 */
@JsonIgnoreProperties({"copyright"})
public class Schedule {

    private Program[] schedule;

    /**
     * Returns the programs array
     * @return Program array
     */
    public Program[] getSchedule() {
        return schedule;
    }

    /**
     * Sets the programs array
     * @param schedule Program array
     */
    public void setSchedule(Program[] schedule) {
        this.schedule = schedule;
    }
}
