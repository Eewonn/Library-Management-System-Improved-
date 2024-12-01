package com.library.management.classes;

import com.library.management.database.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Author extends Person {
    private int authorId;
    private List<Book> books;

    // Constructor with parameters, inheriting from Person Class
    public Author(String name) {
        super(name);
        this.books = new ArrayList<>();
    }

    // Getter
    public String getName() {
        return super.getName();
    }

    // Setter
    public void setName(String name) {
        super.setName(name);
        updateAuthorNameInDatabase();
    }

    // Methods for managing books
    public void addBook(Book book) {
        if (!books.contains(book)) {
            books.add(book);
        }
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    public List<Book> getBooks() {
        return books;
    }

    // Load author name from the database
    public void loadAuthorFromDatabase() {
        String query = "SELECT name FROM authors WHERE author_id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                setName(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading author from database: " + e.getMessage());
        }
    }

    // Update author name in the database
    private void updateAuthorNameInDatabase() {
        String query = "UPDATE authors SET name = ? WHERE author_id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, getName());
            stmt.setInt(2, authorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating author name: " + e.getMessage());
        }
    }
}