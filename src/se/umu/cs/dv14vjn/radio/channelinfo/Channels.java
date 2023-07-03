package se.umu.cs.dv14vjn.radio.channelinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

/**
 * Channels object that stores channel objects in an array
 * @author Vincent Johansson
 */
@JsonIgnoreProperties({"copyright"})
public class Channels {

    private final HashMap<String, Channel> channelsMap = new HashMap<>();
    private String[] channels;

    /**
     * Returns the channels array
     * @return Channel array
     */
    public String[] getNames() {
        return channels;
    }

    /**
     * Sets the channels array
     * @param channels Channel array
     */
    public void setNamesArray(String[] channels) {
        this.channels = channels;
    }

    public void addChannel(String name, Channel channel) {
        channelsMap.put(name, channel);
    }

    public Channel getChannel(String name) {
        return channelsMap.get(name);
    }
}
