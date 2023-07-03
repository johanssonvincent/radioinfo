package se.umu.cs.dv14vjn.radio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.umu.cs.dv14vjn.radio.channelinfo.Channel;
import se.umu.cs.dv14vjn.radio.channelinfo.Channels;
import se.umu.cs.dv14vjn.radio.channelinfo.Program;
import se.umu.cs.dv14vjn.radio.channelinfo.Schedule;
import se.umu.cs.dv14vjn.radio.gui.DataChangeListener;

import javax.swing.*;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * APIHandler is a class that handles the communication with the API
 * @author Vincent Johansson
 */
public class APIHandler {

    private final Channels channels;
    private final List<DataChangeListener> listeners = new ArrayList<>();

    /**
     * Initializes a new se.umu.cs.dv14vjn.radio.APIHandler
     */
    public APIHandler() {
        channels = parseChannels();
        recurringUpdate();
    }

    /**
     * Get JSON data from the API
     * @param urlString String URL to the API
     * @return String JSON data
     */
    private String getJSONFromURL(String urlString) {
        StringBuilder jsonData = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            /* Verify response from API, if it can't be reached display error message */
            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "Could not connect to the API, please check your internet connection",
                        "Error", JOptionPane.ERROR_MESSAGE));
            } else if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String buf;
                while ((buf = reader.readLine()) != null) {
                    jsonData.append(buf);
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonData.toString();
    }

    /**
     * Parse the JSON data from the API into a se.umu.cs.dv14vjn.radio.channelinfo.Channels object
     * @return se.umu.cs.dv14vjn.radio.channelinfo.Channels object
     */
    private Channels parseChannels() {
        SwingWorker<Channels, Void> worker = new SwingWorker<>() {
            @Override
            protected Channels doInBackground() {
                String jsonData = getJSONFromURL(
                        "http://api.sr.se/api/v2/channels?format=json&pagination=false");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode;
                try {
                    rootNode = mapper.readTree(jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                Channels channels = new Channels();
                List<String> channelList = new ArrayList<>();
                for (JsonNode channelNode : rootNode.path("channels")) {
                    try {
                        Channel channel = mapper.treeToValue(channelNode, Channel.class);
                        if (!Objects.equals(channel.getChanneltype(), "Extrakanaler")) {
                            channelList.add(channel.getName());
                            channels.addChannel(channel.getName(), channel);
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                String[] channelArray = channelList.toArray(String[]::new);
                channels.setNamesArray(channelArray);
                return channels;
            }

            @Override
            protected void done() {
                notifyDataChangeListeners();
            }
        };
        worker.execute();
        try {
            return worker.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse the JSON data from the API into a se.umu.cs.dv14vjn.radio.channelinfo.Schedule object
     * @param channel se.umu.cs.dv14vjn.radio.channelinfo.Channel object
     * @return se.umu.cs.dv14vjn.radio.channelinfo.Schedule object
     */
    public Schedule parseSchedule(Channel channel) {

        /*
         * Get current time and calculate time 6 hours before and 12 hours from now
         * Reformat to Calendar object for validation of date
         */
        long currentTime = System.currentTimeMillis();
        long sixHoursAgo = currentTime - (6 * 60 * 60 * 1000);
        long twelveHoursFromNow = currentTime + (12 * 60 * 60 * 1000);
        Calendar currentCal = Calendar.getInstance();
        Calendar sixHoursAgoCal = Calendar.getInstance();
        Calendar twelveHoursFromNowCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentTime);
        sixHoursAgoCal.setTimeInMillis(sixHoursAgo);
        twelveHoursFromNowCal.setTimeInMillis(twelveHoursFromNow);

        List<Program> filteredSchedule = new ArrayList<>();
        String jsonData;

        /* If sixHoursAgo is before 00:00, get yesterday's schedule first */
        if (sixHoursAgoCal.get(Calendar.DATE) < currentCal.get(Calendar.DATE)) {
            String date = sixHoursAgoCal.get(Calendar.YEAR) + "-" + (sixHoursAgoCal.get(Calendar.MONTH) + 1) + "-" +
                    sixHoursAgoCal.get(Calendar.DATE);
            jsonData = getJSONFromURL("http://api.sr.se/api/v2/scheduledepisodes?channelid="
                    + channel.getId() + "&format=json&pagination=false&date=" + date);
            filterPrograms(filteredSchedule, jsonData);
        }

        /* Get today's schedule */
        jsonData = getJSONFromURL("http://api.sr.se/api/v2/scheduledepisodes?channelid="
                            + channel.getId() + "&format=json&pagination=false");
        filterPrograms(filteredSchedule, jsonData);

        /* If twelveHoursFromNow is after 23:59, get tomorrow's schedule last */
        if (twelveHoursFromNowCal.get(Calendar.DATE) > currentCal.get(Calendar.DATE)) {
            String date = twelveHoursFromNowCal.get(Calendar.YEAR) + "-" +
                    (twelveHoursFromNowCal.get(Calendar.MONTH) + 1) + "-" + twelveHoursFromNowCal.get(Calendar.DATE);
            jsonData = getJSONFromURL("http://api.sr.se/api/v2/scheduledepisodes?channelid="
                    + channel.getId() + "&format=json&pagination=false&date=" + date);
            filterPrograms(filteredSchedule, jsonData);
        }

        /* Create Schedule object, reformat list to array and add to the new Schedule object */
        Program[] programs = filteredSchedule.toArray(Program[]::new);
        Schedule schedule = new Schedule();
        schedule.setSchedule(programs);
        return schedule;
    }

    /**
     * Filter the JSON data from the API into a se.umu.cs.dv14vjn.radio.channelinfo.Program object
     * @param lst List of se.umu.cs.dv14vjn.radio.channelinfo.Program objects
     * @param jsonData String JSON data
     */
    private void filterPrograms(List<Program> lst, String jsonData) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode= mapper.readTree(jsonData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        DateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        long currentTime = System.currentTimeMillis();
        long sixHoursAgo = currentTime - (6 * 60 * 60 * 1000);
        long twelveHoursFromNow = currentTime + (12 * 60 * 60 * 1000);

        /* Loop through all programs adding the ones matching requirements to list */
        assert rootNode != null;
        for (JsonNode programNode : rootNode.get("schedule")) {
            try {
                Program program = mapper.treeToValue(programNode, Program.class);
                Date startTime = formatter.parse(program.getLocalTime(program.getStarttimeutc()));
                /* Only acquire programs that are within 6 hours before and 12 hours from now */
                if (currentTime >= startTime.getTime() && startTime.getTime() >= sixHoursAgo) {
                    program.setImage();
                    lst.add(program);
                } else if (currentTime <= startTime.getTime()&& startTime.getTime() <= twelveHoursFromNow) {
                    program.setImage();
                    lst.add(program);
                }
            } catch (ParseException | JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initialize background thread that updates cached data every 60 minutes
     */
    private void recurringUpdate() {
        Timer timer = new Timer(3600000, e -> updateCachedSchedules());
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Update cached data
     */
    public void updateCachedSchedules() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (String channel : channels.getNames()) {
                    if (channels.getChannel(channel).isCached()) {
                        channels.getChannel(channel).setSchedule(parseSchedule(channels.getChannel(channel)));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                notifyDataChangeListeners();
            }
        };
        worker.execute();
    }

    /**
     * Get Channel[] array with all channels
     * @return Channel[] array with all channels
     */
    public Channels getChannels() {
        return channels;
    }

    /**
     * Add a DataChangeListener to the list of listeners
     * @param listener Listener to be added
     */
    public void addDataChangeListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a DataChangeListener from the list of listeners
     * @param listener Listener to be removed
     */
    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that data has changed
     */
    private void notifyDataChangeListeners() {
        for (DataChangeListener listener : listeners) {
            listener.dataChanged();
        }
    }
}
