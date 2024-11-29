package com.library.management.gui;

import com.library.management.classes.Book;
import com.library.management.classes.Member;
import com.library.management.classes.User;
import com.library.management.classes.Library;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionsPage extends LibraryDashboard {
    // Static Attributes
    //private static final Color THEME_COLOR = new Color(60, 106, 117);
    private static final Color TABLE_TEXT_COLOR = Color.WHITE;
    private static final Color TABLE_HEADER_COLOR = new Color(60, 106, 117);
    private static final Color TABLE_BACKGROUND_COLOR = new Color(60, 106, 117);
    private static final Color BUTTON_COLOR = new Color(80, 120, 130);
    private static final Color COMBOBOX_COLOR = new Color(240, 240, 240);
    

    // Attributes
    private JComboBox<Book> booksComboBox;
    private JComboBox<Member> membersComboBox;
    private Library library;
    private DefaultTableModel tableModel;
    private JTable transactionsTable;
    private List<Book> bookList;
    private List<Member> memberList;

    // Constructor
    public TransactionsPage(User user) {
        super(user, getLibrary());
        this.library = getLibrary();
        this.bookList = new ArrayList<>();
        this.memberList = new ArrayList<>();
        setTitle("Transactions Management");
        setupUI();
        loadBooksAndMembers();
        loadTransactionsFromDatabase();
        setVisible(true);
    }

    // Static method to initialize the library
    public static Library getLibrary() {
        try {
            return new Library();
        } catch (SQLException e) {
            System.err.println("Error initializing Library: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to initialize the library: " + e.getMessage());
            return null; // Return null if initialization fails
        }
    }

    // GUI SET UP METHODS
    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create dropdowns for selecting books and members
        booksComboBox = new JComboBox<>();
        membersComboBox = new JComboBox<>();
        customizeComboBox(booksComboBox);
        customizeComboBox(membersComboBox);

        // Create buttons for borrowing and returning books
        JButton borrowButton = createCustomButton("Borrow Book");
        borrowButton.addActionListener(e -> borrowBook());
        JButton returnButton = createCustomButton("Return Book");
        returnButton.addActionListener(e -> returnBook());

        // Create transaction panel
        JPanel transactionPanel = new JPanel();
        transactionPanel.add(new JLabel("Select Book:"));
        transactionPanel.add(booksComboBox);
        transactionPanel.add(new JLabel("Select Member:"));
        transactionPanel.add(membersComboBox);
        transactionPanel.add(borrowButton);
        transactionPanel.add(returnButton);

        // Create a table for displaying transactions
        String[] columnNames = {"Transaction Type", "Member", "Book", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        transactionsTable = createTransactionsTable();

        // Create a JScrollPane for the table
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        scrollPane.setMaximumSize(new Dimension((int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.5), Integer.MAX_VALUE));

        // Create a panel to add padding around the JScrollPane
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 10, 50));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(transactionPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    // Customize JComboBox appearance
    private void customizeComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(COMBOBOX_COLOR);
        comboBox.setForeground(Color.BLACK);
        comboBox.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_COLOR));
    }


    // Load books and members from the database
    private void loadBooksAndMembers() {
        if (this.library == null) {
            JOptionPane.showMessageDialog(this, "Library is not initialized.");
            dispose();
            return;
        }

        try {
            List<Book> books = library.getAllBooks();
            for (Book book : books) {
                booksComboBox.addItem(book);
                bookList.add(book);
            }

            List<Member> members = library.getAllMembers();
            for (Member member : members) {
                membersComboBox.addItem(member);
                memberList.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage());
        }
    }

    // Load transactions from the database
    private void loadTransactionsFromDatabase() {
        if (this.library == null) {
            JOptionPane.showMessageDialog(this, "Library is not initialized.");
            return;
        }

        try {
            // Clear existing rows in the table
            tableModel.setRowCount(0);

            // Retrieve transactions from the database
            List<Object[]> transactions = library.getAllTransactions();

            // Populate the table with transactions
            for (Object[] transaction : transactions) {
                tableModel.addRow(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load transactions: " + e.getMessage());
        }
    }

    // Modify borrowBook to update database and refresh transactions
    private void borrowBook() {
        Book selectedBook = (Book) booksComboBox.getSelectedItem();
        Member selectedMember = (Member) membersComboBox.getSelectedItem();

        if (selectedBook != null && selectedMember != null) {
            try {
                if (library.borrowBook(selectedMember, selectedBook)) {
                    loadTransactionsFromDatabase();
                    JOptionPane.showMessageDialog(this, selectedMember.getName() + " borrowed " + selectedBook.getTitle());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to borrow the book. It may not be available.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to borrow the book: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select both a book and a member.");
        }
    }

    // Modify returnBook to update database and refresh transactions
    private void returnBook() {
        Book selectedBook = (Book) booksComboBox.getSelectedItem();
        Member selectedMember = (Member) membersComboBox.getSelectedItem();

        if (selectedBook != null && selectedMember != null) {
            try {
                if (library.returnBook(selectedMember, selectedBook)) {
                    loadTransactionsFromDatabase();
                    JOptionPane.showMessageDialog(this, selectedMember.getName() + " returned " + selectedBook.getTitle());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to return the book. It may not be borrowed by the member.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to return the book: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select both a book and a member.");
        }
    }

    // Create a custom button with styling
    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Create the transactions table with custom renderer
    private JTable createTransactionsTable() {
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(TABLE_BACKGROUND_COLOR);
                c.setForeground(TABLE_TEXT_COLOR);
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(TABLE_TEXT_COLOR);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        return table;
    }
} 