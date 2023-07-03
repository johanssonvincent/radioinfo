package se.umu.cs.dv14vjn.radio.channelinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Program object that stores information about a program
 * @author Vincent Johansson
 */
@JsonIgnoreProperties({"episodeid", "program", "channel", "imageurltemplate", "photographer", "endtimeutc"})
public class Program {

    private String title;
    private String description;
    private String starttimeutc;
    private String imageurl;
    private Image image;

    /**
     * Returns the title of the program
     * @return String with title of program
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the program
     * @param title String with title of program
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Adds subtitle to the title of the program
     * @param subtitle String with subtitle of program
     */
    public void setSubtitle(String subtitle) {
        this.title = title + subtitle;
    }

    /**
     * Returns the description of the program
     * @return String with description of program
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the program
     * @param description String with description of program
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the start time of the program
     */
    public void setStarttimeutc(String starttimeutc) {
        this.starttimeutc = starttimeutc;
    }

    /**
     * Returns the start time of the program
     * @return String with start time of program in Unix time format
     */
    public String getStarttimeutc() {
        return starttimeutc;
    }

    /**
     * Sets the image url for the program
     * @param imageurl String with image url of program
     */
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    /**
     * Returns the image url for the program
     * @return String with image url
     */
    public String getImageurl() {
        return imageurl;
    }

    public void setImage() throws IOException {
        if (getImageurl() != null) {
            Image tempImage = ImageIO.read(new URL(getImageurl()));
            this.image = tempImage.getScaledInstance(60, 40, Image.SCALE_SMOOTH);
        } else {
            Image tempImage = ImageIO.read(
                    new URL("https://static-cdn.sr.se/images/content/default-list-image.png"));
            this.image = tempImage.getScaledInstance(60, 50, Image.SCALE_SMOOTH);
        }
    }

    public Image getImage() {
        return image;
    }

    /**
     * Returns the start time of the program in local time
     * @return String with start time of program in local time
     */
    public String getLocalTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        Matcher matcher = Pattern.compile("(\\d+)").matcher(time);
        long timestamp;
        String formattedTime = "";
        if (matcher.find()) {
            timestamp = Long.parseLong(matcher.group());
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            formattedTime = localDateTime.format(formatter);
        }
        return formattedTime;
    }
}
