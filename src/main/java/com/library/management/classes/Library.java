package com.library.management.classes;

import com.library.management.database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books;
    private List<Member> members;

    // Constructor
    public Library() throws SQLException {
        this.books = getAllBooksFromDatabase();
        this.members = getAllMembersFromDatabase();
    }

    // Getters
    public List<Book> getAllBooks() throws SQLException {
        return new ArrayList<>(books);
    }

    public List<Member> getAllMembers() throws SQLException {
        return new ArrayList<>(members);
    }

    // Methods to add and remove books
    public void addBook(Book book) {
        if (!books.contains(book)) {
            books.add(book);
            insertBookIntoDatabase(book);
            System.out.println("Book added to the library.");
        } else {
            System.out.println("This book is already in the library.");
        }
    }

    public void removeBook(Book book) {
        if (books.contains(book)) {
            books.remove(book);
            deleteBookFromDatabase(book);
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

    // Borrow and return book methods
    public boolean borrowBook(Member member, Book book) {
        if (books.contains(book) && book.getAvailableCopies() > 0 && member != null) {
            Connection conn = null; 
            try {
                conn = databaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Decrease available copies of the book
                String updateBookQuery = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?";
                try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookQuery)) {
                    updateBookStmt.setInt(1, book.getBookId());
                    updateBookStmt.executeUpdate();
                }

                // Insert borrowing record
                insertBorrowingRecordInDatabase(member, book, conn);

                conn.commit();
                member.borrowBook(book); 
                return true; 
            } catch (SQLException e) {
                System.err.println("Error during borrowing book: " + e.getMessage());
               
                if (conn != null) {
                    try {
                        conn.rollback(); 
                    } catch (SQLException rollbackEx) {
                        System.err.println("Error during rollback: " + rollbackEx.getMessage());
                    }
                }
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close(); 
                    } catch (SQLException closeEx) {
                        System.err.println("Error closing connection: " + closeEx.getMessage());
                    }
                }
            }
        } else {
            return false; 
        }
    }

    public boolean returnBook(Member member, Book book) {
        Connection conn = null;
        try {
            conn = databaseConnection.getConnection();
            conn.setAutoCommit(false); 

            // Check if the book is actually borrowed by the member
            String checkBorrowQuery = "SELECT COUNT(*) FROM BorrowedBooks bb " +
                    "JOIN Members m ON bb.member_id = m.member_id " +
                    "WHERE bb.book_id = ? AND m.member_name = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkBorrowQuery)) {
                checkStmt.setInt(1, book.getBookId());
                checkStmt.setString(2, member.getName());

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Book not borrowed by this member
                        conn.rollback();
                        return false; 
                    }
                }
            }

            // Increase available copies of the book
            String updateBookQuery = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?";
            try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookQuery)) {
                updateBookStmt.setInt(1, book.getBookId());
                updateBookStmt.executeUpdate();
            }

            // Delete the borrowing record
            deleteBorrowingRecordFromDatabase(member, book, conn);

            conn.commit();

            // Update member's and book's internal state
            member.returnBook(book);
            book.returnBook();

            return true; 
        } catch (SQLException e) {
            System.err.println("Error during returning book: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close(); 
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    // Database interaction methods
    private List<Book> getAllBooksFromDatabase() throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Books");
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) { 
                System.out.println("No books found in the database.");
            }
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String isbn = rs.getString("ISBN");
                String publicationDate = rs.getString("publication_date");
                int availableCopies = rs.getInt("available_copies");
                int authorId = rs.getInt("author_id");

                // Get author's name from the Authors table
                String authorName = getAuthorNameFromDatabase(authorId);
                if (authorName == null) {
                    System.err.println("Warning: No author found for author_id=" + authorId);
                    continue; 
                }

                Author author = new Author(authorName);
                Book book = new Book(bookId, title, author, isbn, publicationDate, availableCopies);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error getting books from database: " + e.getMessage());
            throw e;
        }
        return books;
    }

    private List<Member> getAllMembersFromDatabase() throws SQLException {
        List<Member> members = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Members");
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) { 
                System.out.println("No members found in the database.");
            }
            while (rs.next()) {
                String name = rs.getString("member_name");
                Member member = new Member(name);
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Error getting members from database: " + e.getMessage());
            throw e; 
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

    private void insertBorrowingRecordInDatabase(Member member, Book book, Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO BorrowedBooks (book_id, member_id, borrow_date, return_date) VALUES (?, (SELECT member_id FROM Members WHERE member_name = ?), ?, ?)")) {
            stmt.setInt(1, book.getBookId());
            stmt.setString(2, member.getName());
            stmt.setString(3, LocalDate.now().toString());
            stmt.setString(4, LocalDate.now().plusWeeks(2).toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting borrowing record: " + e.getMessage());
        }
    }

    private void deleteBorrowingRecordFromDatabase(Member member, Book book, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM BorrowedBooks WHERE book_id = ? AND member_id = (SELECT member_id FROM Members WHERE member_name = ?)")) {
            stmt.setInt(1, book.getBookId());
            stmt.setString(2, member.getName());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Deleted borrowing record, rows affected: " + rowsAffected);
        }
    }

    private void insertBookIntoDatabase(Book book) {
        if (bookExistsInDatabase(book.getISBN())) {
            System.out.println("Book with ISBN " + book.getISBN() + " already exists. Skipping insertion.");
            return;
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
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
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

    // Method to get all transactions (borrowed books)
    public List<Object[]> getAllTransactions() throws SQLException {
        List<Object[]> transactions = new ArrayList<>();
        String query = "SELECT bb.borrow_id, bb.borrow_date, bb.return_date, " +
                "m.member_name, b.title " +
                "FROM BorrowedBooks bb " +
                "JOIN Members m ON bb.member_id = m.member_id " +
                "JOIN Books b ON bb.book_id = b.book_id";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] transaction = new Object[5];
                transaction[0] = rs.getInt("borrow_id");
                transaction[1] = rs.getString("member_name");
                transaction[2] = rs.getString("title");
                transaction[3] = rs.getString("borrow_date");
                transaction[4] = rs.getString("return_date");
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions from database: " + e.getMessage());
            throw e;
        }

        return transactions;
    }
}