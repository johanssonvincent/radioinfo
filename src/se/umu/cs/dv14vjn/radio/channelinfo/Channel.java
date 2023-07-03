package se.umu.cs.dv14vjn.radio.channelinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Channel object that stores information about a channel
 * @author Vincent Johansson
 */
@JsonIgnoreProperties({"imagetemplate", "color", "tagline", "liveaudio", "xmltvid", "siteurl"})
public class Channel {
    private String name;
    private String image;
    private String siteurl;
    private String scheduleurl;
    private String channeltype;
    private Schedule schedule;
    private int id;
    private boolean cached = false;

    /**
     * Returns the name of the channel
     * @return String with name of channel
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the channel
     * @param name String with name of channel
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the channel id
     * @return int with channel id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the channel id
     * @param id int with channel id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the image url
     * @param image String with image url
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Returns the image url
     * @return String with image url
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the type of channel
     * @param channeltype String with type of channel
     */
    public void setChanneltype(String channeltype) {
        this.channeltype = channeltype;
    }

    /**
     * Returns the type of channel
     * @return String with type of channel
     */
    public String getChanneltype() {
        return channeltype;
    }

    /**
     * Sets the schedule url
     * @param scheduleurl String with schedule url
     */
    public void setScheduleurl(String scheduleurl) {
        this.scheduleurl = scheduleurl + "&format=json";
    }

    /**
     * Returns the schedule url
     * @return String with schedule url
     */
    public String getScheduleurl() {
        return scheduleurl;
    }

    /**
     * Sets the schedule
     * @param schedule Schedule object
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Returns the schedule
     * @return Schedule object
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Mark the channel as cached
     */
    public void setCached() {
        cached = true;
    }

    /**
     * Check if the channel is cached
     * @return true if cached, false if not
     */
    public boolean isCached() {
        return cached;
    }
}
