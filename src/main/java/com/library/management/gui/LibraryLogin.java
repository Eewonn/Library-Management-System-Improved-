package com.library.management.gui;

import com.library.management.database.*;
import com.library.management.classes.*;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LibraryLogin extends JFrame {
    // Static Attributes
    private static final Color TITLE_COLOR = new Color(60, 106, 117);
    private static final Color BACKGROUND_COLOR = new Color(47, 54, 64);
    private static final Color INPUT_BACKGROUND_COLOR = Color.WHITE;
    private static final Color INPUT_FOREGROUND_COLOR = new Color(50, 50, 50);
    private static final Color BUTTON_COLOR = new Color(60, 106, 117);
    private static final Font TITLE_FONT = new Font("Roboto", Font.BOLD, 36);
    private static final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 14);

    // Attributes for Input fields of username and password
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Constructor
    public LibraryLogin() {
        setupFrame();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
        setVisible(true);
    }

    // Frame setup
    private void setupFrame() {
        setTitle("Library Book Reservation Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);
    }

    // Main Panel
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Add Title Panel
        JPanel titlePanel = createTitlePanel();
        GridBagConstraints gbc = createGbc(0, 0, 1.0, 0.3);
        mainPanel.add(titlePanel, gbc);

        // Add Input Container
        JPanel inputContainer = createInputContainer();
        gbc = createGbc(0, 1, 1.0, 0.6);
        mainPanel.add(inputContainer, gbc);

        // Add Close Button
        JButton closeButton = createButton("CLOSE", e -> System.exit(0));
        gbc = createGbc(0, 2, 1.0, 0.1);
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(closeButton, gbc);

        return mainPanel;
    }

    // Create Title Panel
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(TITLE_COLOR);
        titlePanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 
                (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.3)));

        // Title Label
        JLabel titleLabel = new JLabel("LIBRARY MANAGEMENT SYSTEM", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        return titlePanel;
    }

    // Create Input Container
    private JPanel createInputContainer() { 
        JPanel inputContainer = new JPanel(new GridBagLayout());
        inputContainer.setBackground(INPUT_BACKGROUND_COLOR);
        inputContainer.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        usernameField = new JTextField(20);
        addInputField(inputContainer, gbc, "Username:", usernameField);
        
        // Password
        passwordField = new JPasswordField(20);
        addInputField(inputContainer, gbc, "Password:", passwordField);

        // Sign-In Button
        JButton signInButton = createButton("SIGN IN", e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Use SwingWorker to perform authentication in the background
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return databaseConnection.authenticateUser (username, password);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            User loggedInUser  = new User(username);
                            new LibraryDashboard(loggedInUser ).setVisible(true);
                            dispose(); 
                        } else {
                            JOptionPane.showMessageDialog(LibraryLogin.this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex ) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(LibraryLogin.this, "An error occurred during login", "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        });
        
        gbc.gridy = 3;
        inputContainer.add(signInButton, gbc);

        // Sign-Up Button
        JButton signUpButton = createButton("SIGN UP", e -> openSignUpDialog());
        gbc.gridy = 4;
        inputContainer.add(signUpButton, gbc);

        return inputContainer;
    }

    // Custom Input Field Design
    private void addInputField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy = panel.getComponentCount() / 2;
        JLabel label = createLabel(labelText, new Color(80, 80, 80));
        panel.add(label, gbc);

        gbc.gridx = 1;
        textField.setToolTipText("Enter " + labelText);
        textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        textField.setBackground(INPUT_BACKGROUND_COLOR);
        textField.setForeground(INPUT_FOREGROUND_COLOR);
        panel.add(textField, gbc);
    }   

    // Custom Label Design
    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    // Custom Button Design
    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    // GridBag Constraints
    private GridBagConstraints createGbc(int gridx, int gridy, double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    // Sign-Up Dialog For User Registration
    private void openSignUpDialog() {
        // Setup
        JDialog signUpDialog = new JDialog(this, "Sign Up", true);
        signUpDialog.setLayout(new GridBagLayout());
        signUpDialog.setSize(400, 200);
        signUpDialog.setLocationRelativeTo(this);
        
        JTextField newUsernameField = new JTextField(20);
        JPasswordField newPasswordField = new JPasswordField(20);

        JButton registerButton = createButton("Register", e -> {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());
    
            if (registerUser (newUsername, newPassword)) {
                JOptionPane.showMessageDialog(signUpDialog, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                signUpDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(signUpDialog, "Registration failed. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        // Layout for the dialog
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1; 
        signUpDialog.add(createLabel("Username:", Color.BLACK), gbc);
        // Username Field
        gbc.gridx = 1;
        gbc.gridwidth = 19;
        signUpDialog.add(newUsernameField, gbc);
    
        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 1; 
        gbc.gridwidth = 1; 
        signUpDialog.add(createLabel("Password:", Color.BLACK), gbc);
        // Password Field
        gbc.gridx = 1;
        gbc.gridwidth = 19; 
        signUpDialog.add(newPasswordField, gbc);
    
        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 20;
        signUpDialog.add(registerButton, gbc);
    
        signUpDialog.setVisible(true);
    }

    // Register User -> Database 
    private boolean registerUser (String username, String password)
    {
        // SQL Insert User Registration Data
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}