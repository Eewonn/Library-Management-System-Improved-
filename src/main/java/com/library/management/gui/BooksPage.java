package com.library.management.gui;

import com.library.management.database.*;
import com.library.management.classes.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class BooksPage extends LibraryDashboard {
    // Static Attributes
    private static final Color THEME_COLOR = new Color(60, 106, 117);
    private static final Color DARKER_THEME_COLOR = Color.BLACK;
    private static final Color TABLE_TEXT_COLOR = Color.WHITE;
    private static final Color TABLE_HEADER_COLOR = new Color(60, 106, 117);
    private static final Color TABLE_BACKGROUND_COLOR = new Color(60, 106, 117);
    private static final Color PLACEHOLDER_COLOR = Color.GRAY;

    // Attributes
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private List<Book> bookList;
    private List<JTextField> inputFields;
    private JTextField searchField;

    // Constructor
    public BooksPage(User user) {
        super(user);
        bookList = new ArrayList<>();
        setTitle("Books Management");
        setupUI();
        loadBooksFromDatabase();
        setVisible(true);
    }

    // GUI SET UP METHODS
    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Title", "Author", "ISBN", "Publication Date", "Available Copies"};
        tableModel = new DefaultTableModel(columnNames, 0);
        booksTable = createBooksTable();

        // Create a search bar
        searchField = new JTextField("Search by Title or Author");
        searchField.setToolTipText("Search by Title or Author");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setForeground(PLACEHOLDER_COLOR);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search by Title or Author")) {
                    searchField.setForeground(Color.BLACK);
                    searchField.setText("");
                }
            }
        
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(PLACEHOLDER_COLOR); // Reset to placeholder color
                    searchField.setText("Search by Title or Author");
                }
            }
        });
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterBooks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterBooks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterBooks();
            }
        });
        
        // Create a panel to hold the search field with padding
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 150, 0, 150)); // Add padding around the search panel

        // Create a JScrollPane for the table
        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        scrollPane.setMaximumSize(new Dimension((int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.5), Integer.MAX_VALUE));

        // Customize the scrollbar
        customizeScrollBar(scrollPane);

        // Create a panel to add padding around the JScrollPane
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 50));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        
        // Create buttons to add, remove, and update books
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Create books table
    private JTable createBooksTable() {
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(THEME_COLOR);
                } else {
                    c.setBackground(TABLE_BACKGROUND_COLOR);
                }
                c.setForeground(TABLE_TEXT_COLOR);
                return c;
            }
        };

        // Center align the column headers
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Center align the cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set table color
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(TABLE_TEXT_COLOR);

        // Set font size for the table header
        Font headerFont = new Font("Arial", Font.BOLD, 18); // Change 16 to your desired font size
        table.getTableHeader().setFont(headerFont);

        // Set font size for the table
        Font tableFont = new Font("Arial", Font.PLAIN , 16); // Change 14 to your desired font size
        table.setFont(tableFont);

        table.setRowHeight(30);

        table.getTableHeader().setBorder(new LineBorder(DARKER_THEME_COLOR, 1));
        table.setBackground(TABLE_BACKGROUND_COLOR);
        table.setForeground(TABLE_TEXT_COLOR);

        return table;
    }

    // Method to customize the scrollbar
    private void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = THEME_COLOR; // Set the color of the scrollbar thumb
                this.trackColor = Color.WHITE; // Set the color of the scrollbar track
            }
        });

        horizontalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = THEME_COLOR; // Set the color of the scrollbar thumb
                this.trackColor = Color.WHITE; // Set the color of the scrollbar track
            }
        });
    }

    // Create Custom Button Panel
    private JPanel createButtonPanel() {
        // Create buttons
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Book");
        JButton updateButton = new JButton("Update Book");

        // Set common properties for all buttons
        for (JButton button : new JButton[]{addButton, removeButton, updateButton}) {
            button.setBackground(THEME_COLOR);
            button.setForeground(Color.WHITE);
            button.setPreferredSize(new Dimension(120, 40));
            button.setMinimumSize(new Dimension(120, 40));
            button.setMaximumSize(new Dimension(120, 40));
        }

        // Add action listeners
        addButton.addActionListener(e -> showBookInputDialog("Add", null, THEME_COLOR, DARKER_THEME_COLOR));
        removeButton.addActionListener(e -> removeBook());
        updateButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                Book selectedBook = bookList.get(selectedRow);
                showBookInputDialog("Update", selectedBook, THEME_COLOR, DARKER_THEME_COLOR);
            } else {
                showError("Please select a book to update");
            }
        });

        // Create button panel and add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);

        // Add padding to the bottom of the button panel
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0)); // 10px padding at the bottom

        return buttonPanel;
    }

    // Input dialog for inserting and updating book values
    private void showBookInputDialog(String action, Book book, Color themeColor, Color darkerThemeColor) {
        JDialog dialog = new JDialog(this, action + " Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        inputFields = new ArrayList<>();
        String[] labels = {"Title:", "Author Name:", "ISBN:", "Publication Date:", "Available Copies:"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            dialog.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            JTextField textField = new JTextField(20);
            if (book != null) {
                switch (i) {
                    case 0: textField.setText(book.getTitle()); break;
                    case 1: textField.setText(book.getAuthor().getName()); break;
                    case 2: textField.setText(book.getISBN()); break;
                    case 3: textField.setText(book.getPublicationDate()); break;
                    case 4: textField.setText(String.valueOf(book.getAvailableCopies())); break;
                }
            }
            inputFields.add(textField);
            textField.setBackground(Color.WHITE);
            textField.setForeground(darkerThemeColor);
            dialog.add(textField, gbc);
        }

        JButton confirmButton = new JButton(action);
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                if (action.equals("Add")) {
                    addBook();
                } else {
                    updateBook(book);
                }
                dialog.dispose();
            }
        });
        confirmButton.setBackground(THEME_COLOR);
        confirmButton.setForeground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        dialog.add(confirmButton, gbc);

        dialog.setBackground(themeColor);
        dialog.setForeground(darkerThemeColor);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Add the filterBooks method
    private void filterBooks() {
        String query = searchField.getText().toLowerCase();
        tableModel.setRowCount(0); // Clear the table

        for (Book book : bookList) {
            String title = book.getTitle().toLowerCase(); // Convert title to lowercase for comparison
            String authorName = book.getAuthor() != null ? book.getAuthor().getName().toLowerCase() : ""; // Convert author name to lowercase for comparison

            // Check if the book matches the search query
            if (title.contains(query) || authorName.contains(query)) {
                tableModel.addRow(new Object[]{
                    book.getTitle(), // Original title
                    book.getAuthor() != null ? book.getAuthor().getName() : "", // Original author name
                    book.getISBN(),
                    book.getPublicationDate(),
                    book.getAvailableCopies()
                });
            }
        }
    }

    // Database Methods
    private void loadBooksFromDatabase() {
        String query = "SELECT b.book_id, b.title, b.author_id, b.ISBN, b.publication_date, b.available_copies, a.name AS author_name " +
                       "FROM Books b " +
                       "JOIN Authors a ON b.author_id = a.author_id";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String authorName = resultSet.getString("author_name");
                String isbn = resultSet.getString("ISBN");
                String publicationDate = resultSet.getString("publication_date");
                int availableCopies = resultSet.getInt("available_copies");

                Author author = new Author(authorName);
                Book book = new Book(title, author, isbn, publicationDate, availableCopies);
                bookList.add(book);
                tableModel.addRow(new Object[]{title, authorName, isbn, publicationDate, availableCopies});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load books from the database.");
        }
    }

    // Add book to database/Table Output
    private void addBook() {
        if (validateInputs()) {
            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                String authorName = inputFields.get(1).getText();
                int authorId = insertAuthor(connection, authorName);
                Book newBook = createBookFromInputs();
                insertBookToDatabase(connection, newBook, authorId);
                connection.commit();

                bookList.add(newBook);
                tableModel.addRow(new Object[]{
                    newBook.getTitle(),
                    authorName,
                    newBook.getISBN(),
                    newBook.getPublicationDate(),
                    newBook.getAvailableCopies()
                });

                clearFields();
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Create Book Values from User Input
    private Book createBookFromInputs() {
        String title = inputFields.get(0).getText();
        String authorName = inputFields.get(1).getText();
        String isbn = inputFields.get(2).getText();
        String pubDate = inputFields.get(3).getText();
        int availableCopies = Integer.parseInt(inputFields.get(4).getText());

        Author author = new Author(authorName);
        return new Book(title, author, isbn, pubDate, availableCopies);
    }

    // Insert Author into Database
    private int insertAuthor(Connection connection, String authorName) throws SQLException {
        String checkAuthorQuery = "SELECT author_id FROM Authors WHERE name = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkAuthorQuery)) {
            checkStmt.setString(1, authorName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("author_id");
                }
            }
        }

        String insertAuthorQuery = "INSERT INTO Authors (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertAuthorQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, authorName);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating author failed, no ID obtained.");
                }
            }
        }
    }

    // Insert Book into Database
    private void insertBookToDatabase(Connection connection, Book book, int authorId) throws SQLException {
        String insertBookQuery = "INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertBookQuery)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setInt(2, authorId);
            pstmt.setString(3, book.getISBN());
            pstmt.setString(4, book.getPublicationDate());
            pstmt.setInt(5, book.getAvailableCopies());
            pstmt.executeUpdate();
        }
    }

    // Update Book into Output Table
    private void updateBook (Book book) {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1 && validateInputs()) {
            try (Connection connection = databaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                String authorName = inputFields.get(1).getText();
                Book updatedBook = createBookFromInputs();
                updateBookInDatabase(connection, updatedBook, authorName, selectedRow);
                connection.commit();

                bookList.set(selectedRow, updatedBook);
                tableModel.setValueAt(updatedBook.getTitle(), selectedRow, 0);
                tableModel.setValueAt(authorName, selectedRow, 1);
                tableModel.setValueAt(updatedBook.getISBN(), selectedRow, 2);
                tableModel.setValueAt(updatedBook.getPublicationDate(), selectedRow, 3);
                tableModel.setValueAt(updatedBook.getAvailableCopies(), selectedRow, 4);

                clearFields();
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Please select a book to update");
        }
    }

    // Update Book In Database
    private void updateBookInDatabase(Connection connection, Book book, String authorName, int selectedRow) throws SQLException {
        String updateBookQuery = "UPDATE Books SET title = ?, author_id = ?, ISBN = ?, publication_date = ?, available_copies = ? WHERE book_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateBookQuery)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setInt(2, insertAuthor(connection, authorName));
            pstmt.setString(3, book.getISBN());
            pstmt.setString(4, book.getPublicationDate());
            pstmt.setInt(5, book.getAvailableCopies());

            int bookId = getBookIdFromRow(selectedRow);
            pstmt.setInt(6, bookId);

            pstmt.executeUpdate();
        }
    }

    // Remove Book from Database/Table Output
    private void removeBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                int bookId = getBookIdFromRow(selectedRow);
                try (Connection connection = databaseConnection.getConnection();
                     PreparedStatement pstmt = connection.prepareStatement("DELETE FROM Books WHERE book_id = ?")) {
                    pstmt.setInt(1, bookId);
                    pstmt.executeUpdate();
                }

                bookList.remove(selectedRow);
                tableModel.removeRow(selectedRow);
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Please select a book to remove");
        }
    }

    // Find Book Id
    private int getBookIdFromRow(int selectedRow) throws SQLException {
        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String isbn = (String) tableModel.getValueAt(selectedRow, 2);

        String query = "SELECT book_id FROM Books WHERE title = ? AND ISBN = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, title);
            pstmt.setString(2, isbn);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("book_id");
                } else {
                    throw new SQLException("Book not found in database");
                }
            }
        }
    }

    // Utility Methods
    private void clearFields() {
        for (JTextField field : inputFields) {
            field.setText("");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Input Validation Methods
    private boolean validateInputs() {
        try {
            String title = inputFields.get(0).getText();
            String authorName = inputFields.get(1).getText();
            String isbn = inputFields.get(2).getText();
            String pubDate = inputFields.get(3).getText();
            int availableCopies = Integer.parseInt(inputFields.get(4).getText());

            if (title.isEmpty() || authorName.isEmpty() || isbn.isEmpty() || pubDate.isEmpty() || availableCopies < 0) {
                showError("Please fill in all fields correctly");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for available copies");
            return false;
        }
    }
}