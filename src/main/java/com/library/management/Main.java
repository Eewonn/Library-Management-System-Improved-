package com.library.management;

import javax.swing.SwingUtilities;
import com.library.management.gui.LibraryLogin;
import com.library.management.classes.Library;
import java.sql.SQLException;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Library library = new Library(); // Initialize the Library instance
                new LibraryLogin(library).setVisible(true); // Pass it to LibraryLogin
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to initialize library: " + e.getMessage());
            }
        });
    }
}
