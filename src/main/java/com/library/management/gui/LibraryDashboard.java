package com.library.management.gui;

import com.library.management.database.*;
import com.library.management.classes.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class LibraryDashboard extends JFrame {
    // Static Attributes
    private static final Color BUTTON_COLOR = new Color(60, 106, 117);
    private static final Color BUTTON_HOVER_COLOR = new Color(80, 126, 137);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color CARD_BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color CARD_BORDER_COLOR = new Color(60, 106, 117);
    private static final Font USER_ROLE_FONT = new Font("Arial", Font.ITALIC, 12);
    private static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Font ICON_FONT = new Font("SansSerif", Font.PLAIN, 50);
    private static final Font VALUE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font DESC_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final int SIDEBAR_WIDTH = 200;

    // Attributes for Values of Cards
    private JLabel booksListedValueLabel;
    private JLabel authorsListedValueLabel;
    private JLabel membersListedValueLabel; // Declare membersListedValueLabel
    private User user;
    private Library library;

    // Constructor
    public LibraryDashboard(User user, Library library) {
        this.user = user;
        this.library = library;
        setupFrame();
        JPanel topBar = createTopBar(user);
        JPanel sidebar = createSidebar();
        JPanel mainPanel = createMainPanel();

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Call Update Count Method (Count for Books and Authors)
        updateCounts();

        setVisible(true);
    }

    // Set Up Frame
    private void setupFrame() {
        setTitle("Library Book Reservation Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // Create Top Bar
    private JPanel createTopBar(User user) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(getWidth(), 60));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Title label
        JLabel titleLabel = new JLabel("Library Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BUTTON_COLOR);
        topBar.add(titleLabel, BorderLayout.CENTER);

        // User Info
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(Color.WHITE);
        userInfoPanel.add(new JLabel("👤"));
        userInfoPanel.add(new JLabel(user.getUsername()));
        JLabel userRole = new JLabel("Admin");
        userRole.setFont(USER_ROLE_FONT);
        userInfoPanel.add(userRole);
        topBar.add(userInfoPanel, BorderLayout.WEST);

        // Time panel
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(Color.WHITE);
        JLabel timeLabel = new JLabel();
        updateTime(timeLabel);
        new Timer(1000, e -> updateTime(timeLabel)).start();
        timePanel.add(timeLabel);
        timePanel.add(createCloseButton());
        topBar.add(timePanel, BorderLayout.EAST);

        return topBar;
    }

    // Create Close Button
    private JButton createCloseButton() {
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(BUTTON_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(80, 30));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));
        return closeButton;
    }

    // Create Main Panel
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        String[][] stats = {
            {"0", "Books Listed", "📚", "#28A745"},
            {"0", "Borrowed Books", "📑", "#007BFF"},
            {"0", "Times Books Returned", "♻️", "#FFC107"},
            {"0", "Members Listed", "👤", "#DC3545"},
            {"0", "Authors Listed", "👨‍💻", "#17A2B8"},
            {"0", "Listed Categories", "📂", "#6F42C1"}
        };
    
        for (String[] stat : stats) {
            JPanel card = createCard(stat[0], stat[1], stat[2], Color.decode(stat[3]));
    
            // Handle the borrowed books label specifically
            if (stat[1].equals("Books Listed")) {
                booksListedValueLabel = new JLabel(stat[0], SwingConstants.CENTER);
                booksListedValueLabel.setFont(VALUE_FONT);
                card.add(booksListedValueLabel, BorderLayout.CENTER);
            } else if (stat[1].equals("Authors Listed")) {
                authorsListedValueLabel = new JLabel(stat[0], SwingConstants.CENTER);
                authorsListedValueLabel.setFont(VALUE_FONT);
                card.add(authorsListedValueLabel, BorderLayout.CENTER);
            } else if (stat[1].equals("Members Listed")) {
                membersListedValueLabel = new JLabel(stat[0], SwingConstants.CENTER);
                membersListedValueLabel.setFont(VALUE_FONT);
                card.add(membersListedValueLabel, BorderLayout.CENTER);
            } else if (stat[1].equals("Borrowed Books")) {
                borrowedBooksListedValueLabel = new JLabel(stat[0], SwingConstants.CENTER);
                borrowedBooksListedValueLabel.setFont(VALUE_FONT);
                card.add(borrowedBooksListedValueLabel, BorderLayout.CENTER); // Add borrowedBooksListedValueLabel here
            }
    
            mainPanel.add(card);
        }
    
        return mainPanel;
    }

    // Create Side Panel
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BUTTON_COLOR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Dashboard button first
        JButton dashboardButton = createMenuButton("Dashboard");
        dashboardButton.addActionListener(e -> openPage("Dashboard"));
        sidebar.add(dashboardButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Labels for New Windows (Books, Author, Member, Transaction, Logout)
        String[] buttonLabels = {"Books", "Member", "Transaction", "Logout"};
        for (String label : buttonLabels) {
            JButton button = createMenuButton(label);
            button.addActionListener(e -> openPage(label));
            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return sidebar;
    }

    // Custom Button For Side Panel
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 60));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BUTTON_FONT);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });
        
        return button;
    }

    // Dashboard Card Display 
    private JPanel createCard(String value, String description, String icon, Color bgColor) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BACKGROUND_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 2));
        card.setPreferredSize(new Dimension(200, 100));
        card .setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(ICON_FONT);
        iconLabel.setForeground(bgColor);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(VALUE_FONT);

        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(DESC_FONT);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    // Link to New Window (click->sidePanelButton)
    private void openPage(String page) {
        // Close the current window
        Window currentWindow = SwingUtilities.getWindowAncestor(this);

        switch (page) {
            case "Dashboard":
                // Always open a new instance of LibraryDashboard
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                new LibraryDashboard(user, library);
                break;

            case "Books":
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                currentWindow = new BooksPage(user);
                currentWindow.setVisible(true);
                break;      

            case "Member":
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                currentWindow = new MembersPage(user);
                currentWindow.setVisible(true);
                break;

            case "Transaction":
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                currentWindow = new TransactionsPage(user);
                currentWindow.setVisible(true);
                break;

            case "Logout":
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                currentWindow = new LibraryLogin(library);
                currentWindow.setVisible(true);
                break;

            default:
                break;
        }
    }   

    // Time Update Method
    private void updateTime(JLabel label) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a\nMMM dd, yyyy");
        label.setText(dateFormat.format(new Date()));
    }

    // Method To Count Book Data from Database
    private int getBookCountFromDatabase() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM books";

        try (Connection connection = databaseConnection.getConnection(); 
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return count;
    }
    
    // Method To Count Author Data from Database
    private int getAuthorCountFromDatabase() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM authors"; // Assuming you have a table named 'authors'

        try (Connection connection = databaseConnection.getConnection(); 
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return count;
    }

    // Method To Count Member Data from Database
    private int getMemberCountFromDatabase() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM members"; 

        try (Connection connection = databaseConnection.getConnection(); 
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return count;
    }

        // Method to count borrowed books from the database
    private int getBorrowedBookCountFromDatabase() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM BorrowedBooks";

        try (Connection connection = databaseConnection.getConnection(); 
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return count;
    }

    
    // Update Book and Author Count for Card Display
    private void updateCounts() {
        updateBookCount();
        updateAuthorCount();
        updateMemberCount();
        updateBorrowedBooksCount();  // Add this line to update borrowed books count
    }
    


    // Method to update the author count
    private void updateAuthorCount() {
        int authorCount = getAuthorCountFromDatabase();
        authorsListedValueLabel.setText(String.valueOf(authorCount));
    }
    
    // Update Book Count for Card Display
    private void updateBookCount() {
        int bookCount = getBookCountFromDatabase();
        booksListedValueLabel.setText(String.valueOf(bookCount));
    }

    // Update Member Count for Card Display
    private void updateMemberCount() {
        int memberCount = getMemberCountFromDatabase(); // Corrected to call getMemberCountFromDatabase
        membersListedValueLabel.setText(String.valueOf(memberCount));
    }

    private JLabel borrowedBooksListedValueLabel;  // Declare borrowedBooksListedValueLabel
    private void updateBorrowedBooksCount() {
        int borrowedBookCount = getBorrowedBookCountFromDatabase();
        borrowedBooksListedValueLabel.setText(String.valueOf(borrowedBookCount));
    }    
}