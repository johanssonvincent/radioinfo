package se.umu.cs.dv14vjn.radio.gui;

import se.umu.cs.dv14vjn.radio.channelinfo.Channel;
import se.umu.cs.dv14vjn.radio.channelinfo.Channels;
import se.umu.cs.dv14vjn.radio.channelinfo.Program;
import se.umu.cs.dv14vjn.radio.channelinfo.Schedule;
import se.umu.cs.dv14vjn.radio.APIHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.UnknownHostException;

import static javax.swing.BorderFactory.createEmptyBorder;

/**
 * The GUI for the RadioInfo application
 * @author Vincent Johansson
 */
public class GUI extends JFrame implements DataChangeListener {

    private final JFrame window;
    private CardLayout cardLayout;
    private final JLabel label;
    private JTable table;
    private final DefaultTableModel tableModel;
    private APIHandler handler;
    private Channel currentChannel;

    /**
     * Initializes a new se.umu.cs.dv14vjn.radio.gui.GUI
     */
    public GUI() {
        tableModel = new DefaultTableModel() {
            public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };

        window = new JFrame("RadioInfo");

        /* Initialize handler, get channels list from API and add to the Channels JMenu */
        try {
            this.handler = new APIHandler();
        } catch (Exception e) {
            if (e.getCause() instanceof UnknownHostException) {
                JOptionPane.showMessageDialog(window,
                        "Could not connect to the API, please check your internet connection");
            } else {
                JOptionPane.showMessageDialog(window, "An error occurred, please try again later");
            }
        }

        label = new JLabel(" ");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        window.add(label, BorderLayout.NORTH);

        JPanel infoPanel = createInfoPanel(handler);
        window.add(infoPanel, BorderLayout.CENTER);
        window.pack();
        configureWindow();


        window.setJMenuBar(createMenu(window));
        handler.addDataChangeListener(this);
        initChannelMenu(window.getJMenuBar(), handler);
    }

    /**
     * Creates the menu bar for the window
     * @param window The JFrame to add the menu bar to
     * @return JMenuBar The menu bar
     */
    private JMenuBar createMenu(JFrame window) {

        JMenuBar menuBar = new JMenuBar();

        /* Menu with refresh and close option */
        JMenu file = new JMenu("File");
        file.setBackground(Color.LIGHT_GRAY);
        file.setRolloverEnabled(true);
        JMenuItem refreshData = new JMenuItem("Refresh cached data");
        refreshData.addActionListener(e -> this.handler.updateCachedSchedules());
        file.add(refreshData);
        JMenuItem fileClose = new JMenuItem("Close");
        fileClose.addActionListener(e -> window.dispose());
        file.add(fileClose);
        menuBar.add(file);

        /* Menu with view options */
        JMenu view = new JMenu("View");
        MementoManager stateHandler = new MementoManager(); /* MementoManager to save states of columns */
        menuBar.add(view);
        JMenuItem viewImg = new JMenuItem("Show/hide image");
        /* Add actionListener that toggles the visibility of the image */
        viewImg.addActionListener(e -> {
            if (table.getColumnModel().getColumn(0).getWidth() == 0) {
                restoreColumn(table, 0, stateHandler);
            } else {
                hideColumn(table, table.getColumnModel().getColumn(0), stateHandler);
            }
        });
        view.add(viewImg);
        JMenuItem viewDescription = new JMenuItem("Show/hide description");
        viewDescription.addActionListener(e -> {
            if (table.getColumnModel().getColumn(2).getWidth() == 0) {
                restoreColumn(table, 2, stateHandler);
            } else {
                hideColumn(table, table.getColumnModel().getColumn(2), stateHandler);
            }
        });
        view.add(viewDescription);


        return menuBar;
    }

    /**
     * Initialize the Channels dropdown menu
     * @param menuBar The menu bar to add the menu to
     * @param handler The APIHandler to use
     */
    private void initChannelMenu(JMenuBar menuBar, APIHandler handler) {
        /* Create Channels drop down */
        JMenu channelsMenu = new JMenu("Channel");
        /* Add all channels as items in the menu */
        Channels channels = handler.getChannels();
        for (String channel : channels.getNames()) {
            JMenuItem channelItem = new JMenuItem(channel);
            channelItem.addActionListener(e -> {
                if (currentChannel != channels.getChannel(channel)){
                    /* Show text to user that new data is being loaded.  */
                    label.setText("Loading...");
                    /* Update the table with the information for the selected channel */
                    updateTable(channels.getChannel(channel));
                    currentChannel = channels.getChannel(channel);
                }
            });
            channelsMenu.add(channelItem);
        }
        menuBar.add(channelsMenu);
    }

    /**
     * Creates the panel that contains the JTable and name which channel is currently displayed
     * The JTable uses a DefaultTableModel to display the information
     * @return JPanel containing the JTable and JLabel components
     */
    private JPanel createInfoPanel(APIHandler handler) {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel pickLabel = new JLabel("Pick a channel");
        pickLabel.setFont(new Font("Arial", Font.BOLD, 30));

        // Get name for each channel and add to the dropdown menu
        Channels channels = handler.getChannels();
        String[] channelNames = new String[channels.getNames().length];
        for (int i = 0; i < channels.getNames().length; i++) {
            channelNames[i] = channels.getNames()[i];
        }
        JComboBox<String> channelDropdown = new JComboBox<>(channelNames);
        channelDropdown.setRenderer(new CenterTextComboBoxRenderer(SwingConstants.CENTER));
        channelDropdown.setPreferredSize(new Dimension(100,25));
        channelDropdown.addActionListener(new DropdownListener(channelDropdown, panel, channels));

        // Create panel with GridBagLayout to arrange label and JComboBox position
        JPanel dropdownPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        dropdownPanel.add(pickLabel, c);
        // Add dropdown to panel
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        dropdownPanel.add(channelDropdown, c);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(dropdownPanel, BorderLayout.NORTH);

        panel.add(wrapperPanel, "dropdown");

        table = new JTable(tableModel) {
            /* Make the table uneditable by user */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        configureTable(table);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(createEmptyBorder());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollPane, "table");
        return panel;
    }

    /**
     * Updates the tableModel with the information for the selected channel
     * @param channel The channel to get the information for
     */
    private void updateTable(Channel channel) {
        /* Clear the table */
        tableModel.setRowCount(0);
        final Channel finalChannel = channel;

        if (channel != null) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    if (!finalChannel.isCached()) {
                        finalChannel.setSchedule(handler.parseSchedule(finalChannel));
                        finalChannel.setCached();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    /* Add the rows with info about the programs in the schedule */
                    Schedule schedule = finalChannel.getSchedule();
                    for (Program program : schedule.getSchedule()) {
                        JLabel iconLabel = new JLabel();
                        iconLabel.setIcon(new ImageIcon(program.getImage()));
                        tableModel.addRow(new Object[]{iconLabel.getIcon(), program.getTitle(),
                                program.getDescription(), program.getLocalTime(program.getStarttimeutc())});
                    }
                    label.setText("Currently showing: " + channel.getName());
                }
            };
            worker.execute();
        }
    }

    /**
     * Configures the appearance of the JTable
     * @param table The JTable to configure
     */
    private void configureTable(JTable table) {
        /* Add columns to the table model */
        tableModel.addColumn("");
        tableModel.addColumn("Title");
        tableModel.addColumn("Description");
        tableModel.addColumn("Start time");

        /* Setup appearance of the table */
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        /* Set column widths */
        TableColumn imgColumn = table.getColumnModel().getColumn(0);
        setColumnWidth(imgColumn, 60);
        TableColumn titleColumn = table.getColumnModel().getColumn(1);
        setColumnWidth(titleColumn, 200);
        TableColumn timeColumn = table.getColumnModel().getColumn(3);
        setColumnWidth(timeColumn, 150);

        /* Disable user resizing of columns */
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        /* Make it possible for user to click description to see full text */
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int targetcol = 2;
                int col = table.columnAtPoint(e.getPoint());

                if (col == targetcol) {
                    String description = table.getValueAt(row, col).toString();
                    JOptionPane.showMessageDialog(null, description,
                            table.getValueAt(row, 1).toString() + " program description",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    /**
     * Sets the width of a column
     * @param column The column to set the width for
     * @param width The width to set
     */
    private void setColumnWidth(TableColumn column, int width) {
        column.setMaxWidth(width);
        column.setMinWidth(width);
        column.setWidth(width);
        column.setPreferredWidth(width);
    }

    /**
     * Hides a column in the JTable
     * @param table The JTable
     * @param column The column to hide
     * @param manager The MementoManager to save the state of the column
     */
    private void hideColumn(JTable table, TableColumn column, MementoManager manager) {
        ColumnMemento state = new ColumnMemento(column);
        manager.save(String.valueOf(column.getModelIndex()), state);

        table.getColumnModel().getColumn(column.getModelIndex()).setMinWidth(0);
        table.getColumnModel().getColumn(column.getModelIndex()).setMaxWidth(0);
        table.getColumnModel().getColumn(column.getModelIndex()).setWidth(0);
        table.getColumnModel().getColumn(column.getModelIndex()).setPreferredWidth(0);
    }

    /**
     * Restores the view of a column in the JTable
     * @param table The JTable
     * @param column The index of column to restore
     * @param manager The MementoManager to restore the state of the column
     */
    private void restoreColumn(JTable table, int column, MementoManager manager) {
        ColumnMemento oldState = manager.restore(String.valueOf(column));
        table.getColumnModel().getColumn(column).setMaxWidth(oldState.getState().getMaxWidth());
        table.getColumnModel().getColumn(column).setMinWidth(oldState.getState().getMinWidth());
        table.getColumnModel().getColumn(column).setWidth(oldState.getState().getWidth());
        table.getColumnModel().getColumn(column).setPreferredWidth(oldState.getState().getPreferredWidth());
    }

    /**
     * Configure the JFrame window
     */
    private void configureWindow() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1000, 500);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    /**
     * Overrides the dataChanged method from the DataListener interface
     * Updates the table with the new data
     */
    @Override
    public void dataChanged() {
        updateTable(currentChannel);
        table.repaint();
    }

    /**
     * Private ActionListener class handling events in JComboBox menu
     */
    private class DropdownListener implements ActionListener {
        private final JComboBox<String> dropdown;
        private final JPanel panel;
        private final Channels channels;

        /**
         * Constructor for the DropdownListener
         * @param dropdown JComboBox<String>
         * @param panel JPanel with CardLayout to trigger change when actionPerformed is called
         * @param channels Channels object containing HashMap with all channels
         */
        public DropdownListener(JComboBox<String> dropdown, JPanel panel, Channels channels) {
            this.dropdown = dropdown;
            this.panel = panel;
            this.channels = channels;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = (String) dropdown.getSelectedItem();
            currentChannel = channels.getChannel(selected);
            updateTable(currentChannel);
            cardLayout.show(panel, "table");
        }
    }

    /**
     * Private class that extends DefaultListCellRenderer to center align text in JComboBox
     */
    private static class CenterTextComboBoxRenderer extends DefaultListCellRenderer {
        private final int alignment;

        /**
         * Constructor for CenterTextComboBoxRenderer
         * @param alignment int value representing alignment
         */
        public CenterTextComboBoxRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setHorizontalAlignment(alignment);
            return label;
        }
    }
}
