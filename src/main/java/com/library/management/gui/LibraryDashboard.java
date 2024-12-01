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
    private JLabel membersListedValueLabel;
    private JLabel borrowedBooksListedValueLabel;

    // Attributes for parameters
    private User user;

    // Constructor
    public LibraryDashboard(User user) {
        this.user = user;
        setupFrame();
        JPanel topBar = createTopBar(user);
        JPanel sidebar = createSidebar();
        JPanel mainPanel = createMainPanel();

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Call Update Count Method 
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
            {"0", "Authors Listed", "👤", "#DC3545"},
            {"0", "Members Listed", "👨‍💻", "#17A2B8"},
        };

        for (String[] stat : stats) {
            JPanel card = createCard(stat[0], stat[1], stat[2], Color.decode(stat[3]));
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
                card.add(borrowedBooksListedValueLabel, BorderLayout.CENTER);
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
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
    
        String[] buttonLabels = {"Dashboard", "Books", "Member", "Transaction", "Logout"};
        for (String label : buttonLabels) {
            JButton button = createMenuButton(label);
            if ((label.equals("Dashboard") && this.getClass() == LibraryDashboard.class) ||
                (label.equals("Books") && this.getClass() == BooksPage.class) ||
                (label.equals("Member") && this.getClass() == MembersPage.class) ||
                (label.equals("Transaction") && this.getClass() == TransactionsPage.class)) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            button.addActionListener(e -> openPage(label));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(180, 50));
            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    
        sidebar.add(Box.createVerticalGlue());
    
        return sidebar;
    }
    
    // Create Menu Button
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 60));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BUTTON_FONT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getBackground() == BUTTON_COLOR) {
                    button.setBackground(BUTTON_HOVER_COLOR);
                }
            }
    
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getBackground() == BUTTON_HOVER_COLOR && 
                    !isCurrentPage(text)) {
                    button.setBackground(BUTTON_COLOR);
                }
            }
        });
        
        return button;
    }
    
    // Check Current Page
    private boolean isCurrentPage(String buttonText) {
        if (buttonText.equals("Dashboard") && this.getClass() == LibraryDashboard.class) return true;
        if (buttonText.equals("Books") && this.getClass() == BooksPage.class) return true;
        if (buttonText.equals("Member") && this.getClass() == MembersPage.class) return true;
        if (buttonText.equals("Transaction") && this.getClass() == TransactionsPage.class) return true;
        return false;
    }

    // Create Card
    private JPanel createCard(String value, String description, String icon, Color bgColor) {
        JPanel card = new JPanel ();
        card.setBackground(CARD_BACKGROUND_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 2));
        card.setPreferredSize(new Dimension(200, 100));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

    // Open Page
    private void openPage(String page) {
        Window newWindow = null;

        switch (page) {
            case "Dashboard":
                newWindow = new LibraryDashboard(user);
                break;

            case "Books":
                newWindow = new BooksPage(user);
                break;

            case "Member":
                newWindow = new MembersPage(user);
                break;

            case "Transaction":
                newWindow = new TransactionsPage(user);
                break;

            case "Logout":
                newWindow = new LibraryLogin();
                break;

            default:
                return;
        }

        if (newWindow != null) {
            newWindow.setVisible(true);
            SwingUtilities.invokeLater(this::dispose);
        }
    }

    // Update Time
    private void updateTime(JLabel label) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a\n MMM dd, yyyy");
        label.setText(dateFormat.format(new Date()));
    }

    // Update Counts
    private void updateCounts() {
        updateBookCount();
        updateAuthorCount();
        updateMemberCount();
        updateBorrowedBooksCount();
    }

    // Update Book Count
    private void updateBookCount() {
        int bookCount = getBookCountFromDatabase();
        booksListedValueLabel.setText(String.valueOf(bookCount));
    }

    // Update Author Count
    private void updateAuthorCount() {
        int authorCount = getAuthorCountFromDatabase();
        authorsListedValueLabel.setText(String.valueOf(authorCount));
    }

    // Update Member Count
    private void updateMemberCount() {
        int memberCount = getMemberCountFromDatabase();
        membersListedValueLabel.setText(String.valueOf(memberCount));
    }

    // Update Borrowed Books Count
    private void updateBorrowedBooksCount() {
        int borrowedBookCount = getBorrowedBookCountFromDatabase();
        borrowedBooksListedValueLabel.setText(String.valueOf(borrowedBookCount));
    }

    // Database Methods
    private int getBookCountFromDatabase() {
        return getCountFromDatabase("books");
    }

    private int getAuthorCountFromDatabase() {
        return getCountFromDatabase("authors");
    }

    private int getMemberCountFromDatabase() {
        return getCountFromDatabase("members");
    }

    private int getBorrowedBookCountFromDatabase() {
        return getCountFromDatabase("BorrowedBooks");
    }

    private int getCountFromDatabase(String tableName) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + tableName;

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
}