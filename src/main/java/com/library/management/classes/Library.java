
package com.library.management.classes;

import com.library.management.database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.util.Map;
import java.util.HashMap;


import java.time.LocalDate;

public class Library {
    private JComboBox<Book> bookComboBox;
    private JComboBox<Member> memberComboBox;
    private JTable transactionTable;
    private List<Book> books;
    private List<Member> members;
    private Map<Member, Book> borrowedBooks = new HashMap<>();


    public Library() throws SQLException { 
        try {
            this.books = getAllBooksFromDatabase();
            this.members = getAllMembersFromDatabase();
            
            // Initialize combo boxes and table
            bookComboBox = new JComboBox<>(new DefaultComboBoxModel<>(books.toArray(new Book[0])));
            memberComboBox = new JComboBox<>(new DefaultComboBoxModel<>(members.toArray(new Member[0])));
            transactionTable = new JTable();
            System.out.println("Library initialized with " + books.size() + " books and " + members.size() + " members.");
        } catch (SQLException e) {
            System.err.println("Error initializing Library: " + e.getMessage());
            throw e;
        }
    }

    public class TransactionTableModel extends AbstractTableModel {
        private List<Object[]> transactions;
        private String[] columnNames = {"Transaction Type", "Member Name", "Book Title", "Borrow Date"};
        
        public TransactionTableModel(List<Object[]> transactions) {
            this.transactions = transactions;
        }

        @Override
        public int getRowCount() {
            return transactions.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object[] row = transactions.get(rowIndex);
            return row[columnIndex];
        }
    }


    // Getters
    public List<Book> getAllBooks() throws SQLException {
        return new ArrayList<>(books);
    }

    public List<Member> getAllMembers() throws SQLException {
        return new ArrayList<>(members);
    }

    // Method to update a book's available copies
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE Books SET available_copies = ? WHERE book_id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, book.getAvailableCopies());
            pstmt.setInt(2, book.getBookId());
            pstmt.executeUpdate();

            // Update the book in the list as well
            for (Book b : books) {
                if (b.getBookId() == book.getBookId()) {
                    b.setAvailableCopies(book.getAvailableCopies());
                    System.out.println("Book updated in the library.");
                    return;
                }
            }
            // If the book was not found in the list (for some reason)
            System.out.println("Book not found in the current library list.");
        } catch (SQLException e) {
            System.err.println("Error updating book in database: " + e.getMessage());
            throw e; // Rethrow the exception
        }
    }

    // Methods to add and remove books
    public void addBook(Book book) {
        if (!books.contains(book)) {
            books.add(book);
            insertBookIntoDatabase(book); // Add this method to insert the book into the database
            System.out.println("Book added to the library.");
        } else {
            System.out.println("This book is already in the library.");
        }
    }

    public void removeBook(Book book) {
        if (books.contains(book)) {
            books.remove(book);
            deleteBookFromDatabase(book); // Add this method to delete the book from the database
            System.out.println("Book removed from the library.");
        } else {
            System.out.println("This book is not in the library.");
        }
    }

    // Methods to add and remove members
    public void addMember(Member member) {
        if (!members.contains(member)) {
            members.add(member);
            insertMemberIntoDatabase(member);
            System.out.println("Member added to the library.");
        } else {
            System.out.println("This member is already registered.");
        }
    }

    public void removeMember(Member member) {
        if (members.contains(member)) {
            members.remove(member);
            deleteMemberFromDatabase(member);
            System.out.println("Member removed from the library.");
        } else {
            System.out.println("This member is not registered in the library.");
        }
    }

    // Private methods to interact with the database
    private List<Book> getAllBooksFromDatabase() throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Books");
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No books found in the database.");
            }
            while (rs.next()) {
                String title = rs.getString("title");
                String isbn = rs.getString("ISBN");
                String publicationDate = rs.getString("publication_date");
                int availableCopies = rs.getInt("available_copies");
                int authorId = rs.getInt("author_id");

                String authorName = getAuthorNameFromDatabase(authorId);
                if (authorName == null) {
                    System.err.println("Warning: No author found for author_id=" + authorId);
                    continue; // Skip this book if no author is found
                }

                Author author = new Author(authorName);
                Book book = new Book(title, author, isbn, publicationDate, availableCopies);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting books from database: " + e.getMessage());
            throw e; // Rethrow the exception
        }
        return books;
    }

        
    private List<Member> getAllMembersFromDatabase() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT member_id, member_name FROM Members"; // Ensure the table and columns are correct
    
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            if (!rs.isBeforeFirst()) { // No data in ResultSet
                System.out.println("No members found in the database.");
            }
    
            while (rs.next()) {
                int memberId = rs.getInt("member_id"); // Adjust column name to match your table
                String memberName = rs.getString("member_name");
                
                // Log fetched member details
                System.out.println("Fetched member: ID=" + memberId + ", Name=" + memberName);
    
                // Ensure the Member class has an appropriate constructor
                Member member = new Member(memberId, memberName);
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Error getting members from database: " + e.getMessage());
            throw e; // Rethrow exception for higher-level handling
        }
    
        return members;
    }
    

    private String getAuthorNameFromDatabase(int authorId) {
        String authorName = null;
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM Authors WHERE author_id = ?")) {
            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    authorName = rs.getString("name");
                } else {
                    System.err.println("No author found for author_id=" + authorId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting author name from database: " + e.getMessage());
        }
        return authorName;
    }

    private void insertBookIntoDatabase(Book book) {
        if (bookExistsInDatabase(book.getISBN())) {
            System.out.println("Book with ISBN " + book.getISBN() + " already exists. Skipping insertion.");
            return; // Skip insertion if the book already exists
        }

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Books (title, ISBN, publication_date, available_copies) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getISBN());
            stmt.setString(3, book.getPublicationDate());
            stmt.setInt(4, book.getAvailableCopies());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting book into database: " + e.getMessage());
        }
    }

    private boolean bookExistsInDatabase(String isbn) {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Books WHERE ISBN = ?")) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) > 0; // Return true if the book exists
        } catch (SQLException e) {
            System.err.println("Error checking if book exists in database: " + e.getMessage());
        }
        return false;
    }

    private void deleteBookFromDatabase(Book book) {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Books WHERE book_id = ?")) {
            stmt.setInt(1, book.getBookId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting book from database: " + e.getMessage());
        }
    }

    private void insertMemberIntoDatabase(Member member) {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Members (member_name) VALUES (?)")) {
            stmt.setString(1, member.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting member into database: " + e.getMessage());
        }
    }

    private void deleteMemberFromDatabase(Member member) {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Members WHERE member_name = ?")) {
            stmt.setString(1, member.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting member from database: " + e.getMessage());
        }
    }

    // Example usage of the updateBook method
    public void updateBookExampleUsage() {
        try {
            // Example: Update the available copies of a book
            Book bookToUpdate = books.get(0);  // Assuming you have at least one book in the list
            bookToUpdate.setAvailableCopies(bookToUpdate.getAvailableCopies() + 1);  // Increment the available copies
            updateBook(bookToUpdate);
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
    }

    public boolean borrowBook(Member member, Book book) {
        if (book.getAvailableCopies() > 0) {
            book.borrowBook(); // Decrease available copies
            borrowedBooks.put(member, book); // Track borrowed book
            return true;
        } else {
            System.out.println("Book is not available for borrowing.");
            return false;
        }
    }

        // Return a book
        public void returnBook() {
            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            Member selectedMember = (Member) memberComboBox.getSelectedItem();

            if (selectedBook != null && selectedMember != null) {
                selectedMember.returnBook(selectedBook); // Update in-memory state
                deleteBorrowedRecordFromDatabase(selectedMember, selectedBook); // Remove from database
                refreshTable(); // Update UI
                JOptionPane.showMessageDialog(null, "Book returned successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Please select both a book and a member.");
            }
        }

        // Insert record into the database when a book is borrowed
        public void insertBorrowedRecordInDatabase(Member member, Book book) {
            String sql = "INSERT INTO BorrowedBooks (member_id, book_id, borrow_date) VALUES (?, ?, ?)";
            try (Connection conn = databaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, member.getMemberId());
                stmt.setInt(2, book.getBookId());
                stmt.setString(3, LocalDate.now().toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error inserting borrowed record into database: " + e.getMessage());
            }
        }

        // Delete record from the database when a book is returned
        private void deleteBorrowedRecordFromDatabase(Member member, Book book) {
            String sql = "DELETE FROM BorrowedBooks WHERE member_id = ? AND book_id = ?";
            try (Connection conn = databaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, member.getMemberId());
                stmt.setInt(2, book.getBookId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error deleting borrowed record from database: " + e.getMessage());
            }
        }

    private void refreshTable() {
        try {
            transactionTable.setModel(new TransactionTableModel(getTransactionData()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Object[]> getTransactionData() throws SQLException {
        return getAllTransactions();
    }
        

    public List<Object[]> getAllTransactions() throws SQLException {
        List<Object[]> transactions = new ArrayList<>();
        String sql = "SELECT t.status AS transaction_type, m.member_name, b.title, t.borrow_date " +
                     "FROM Transactions t " +
                     "JOIN Members m ON t.member_id = m.member_id " +
                     "JOIN Books b ON t.book_id = b.book_id";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String transactionType = rs.getString("transaction_type");
                String memberName = rs.getString("member_name");
                String bookTitle = rs.getString("title");
                String borrowDate = rs.getString("borrow_date");  // Or choose return_date based on your requirement
                transactions.add(new Object[]{transactionType, memberName, bookTitle, borrowDate});
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            throw e;
        }
        return transactions;
    }
    
}