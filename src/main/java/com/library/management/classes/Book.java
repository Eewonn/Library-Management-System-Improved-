package com.library.management.classes;

import com.library.management.database.databaseConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Book implements LibraryItem{
    private int bookId;
    private String title;
    private Author author;
    private String ISBN;
    private String publicationDate;
    private int availableCopies;

    // Constructor for existing books
    public Book(int bookId, String title, Author author, String ISBN, String publicationDate, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
        this.publicationDate = publicationDate;
        this.availableCopies = availableCopies;
    }

    // Constructor for new books
    public Book(String title, Author author, String ISBN, String publicationDate, int availableCopies) {
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
        this.publicationDate = publicationDate;
        this.availableCopies = availableCopies;
        this.bookId = -1; // Initially set to -1 since it hasn't been saved to the database yet
    }

    // Method to save the book to the database
    public boolean save() {
        if (this.bookId == -1) { // Only insert if bookId is not set
            this.bookId = insertBookIntoDatabase();
            return this.bookId != -1; // Return true if insertion was successful
        }
        return true; // Already saved
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public String getISBN() {
        return ISBN;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getBookId() {
        return bookId;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
        updateBookInDatabase();
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    @Override
    public String toString() {
        return title + " by " + (author != null ? author.getName() : "Unknown Author");
    }

    // Methods for borrowing and returning books
    @Override
    public void borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
            updateBookInDatabase();
        } else {
            System.out.println("No Available Copies to Borrow");
        }
    }

    @Override
    public void returnBook() {
        availableCopies++;
        updateBookInDatabase();
    }

    // Database interaction methods
    private int insertBookIntoDatabase() {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Books (title, ISBN, publication_date, available_copies) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, ISBN);
            stmt.setString(3, publicationDate);
            stmt.setInt(4, availableCopies);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting book: " + e.getMessage());
        }
        return -1;
    }

    private void updateBookInDatabase() {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Books SET title = ?, ISBN = ?, publication_date = ?, available_copies = ? WHERE book_id = ?")) {
            stmt.setString(1, title);
            stmt.setString(2, ISBN);
            stmt.setString(3, publicationDate);
            stmt.setInt(4, availableCopies);
            stmt.setInt(5, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
    }

    // Delete Book and Reset Sequence
    public void deleteBookFromDatabase() {
        String deleteBookQuery = "DELETE FROM Books WHERE book_id = ?";
        String resetSequenceQuery = "UPDATE sqlite_sequence SET seq = (SELECT MAX(book_id) FROM Books) WHERE name = 'Books'";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteBookQuery);
             PreparedStatement resetStmt = conn.prepareStatement(resetSequenceQuery)) {
            deleteStmt.setInt(1, bookId);
            deleteStmt.executeUpdate();
            resetStmt.executeUpdate();  // Reset the book sequence
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }
}