package se.umu.cs.dv14vjn.radio;

import com.formdev.flatlaf.FlatDarkLaf;
import se.umu.cs.dv14vjn.radio.gui.GUI;

import javax.swing.*;

public class Client {
    public static void main(String[] args) {
        /* Use the FlatLaf Dark look and feel */
        FlatDarkLaf.setup();

        /* Start the GUI */
        SwingUtilities.invokeLater(GUI::new);
    }
}