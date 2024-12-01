package com.library.management.classes;

import com.library.management.database.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Member extends Person {
    private int memberId;
    private List<Book> borrowedBooks;

    // Constructors
    public Member(int memberId, String name, String borrowedBooks) {
        super(name);
        this.memberId = memberId;
        this.borrowedBooks = new ArrayList<>();
        
        // Populate borrowedBooks from the string
        if (borrowedBooks != null && !borrowedBooks.isEmpty()) {
            String[] titles = borrowedBooks.split(", ");
            for (String title : titles) {
                Book book = findBookByTitle(title); 
                if (book != null) {
                    this.borrowedBooks.add(book);
                }
            }
        }
    }

    public Member(String name) {
        super(name);
        this.borrowedBooks = new ArrayList<>();
    }

    // Getters
    public List<Book> getBorrowedBooks() {
        return borrowedBooks != null ? borrowedBooks : new ArrayList<>();
    }

    public int getMemberId() {
        return memberId;
    }

    // Setter
    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return getName();
    }

    // Method to load member data from the database
    public void loadMemberFromDatabase(int memberId) {
        String query = "SELECT m.member_id AS memberID, m.member_name AS memberName, " +
                       "GROUP_CONCAT(b.title, ', ') AS borrowedBooks " +
                       "FROM members m " +
                       "LEFT JOIN BorrowedBooks bb ON m.member_id = bb.member_id " +
                       "LEFT JOIN Books b ON bb.book_id = b.book_id " +
                       "WHERE m.member_id = ? " +
                       "GROUP BY m.member_id, m.member_name";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.memberId = rs.getInt("memberID");
                setName(rs.getString("memberName"));

                // Clear existing borrowed books
                this.borrowedBooks.clear();

                // Split the concatenated book titles into a list
                String borrowedBooksStr = rs.getString("borrowedBooks");
                if (borrowedBooksStr != null) {
                    String[] booksArray = borrowedBooksStr.split(", ");
                    for (String bookTitle : booksArray) {
                        Book book = findBookByTitle(bookTitle);
                        if (book != null) {
                            this.borrowedBooks.add(book);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading member from database: " + e.getMessage());
        }
    }

    // Method to find a Book by title
    private Book findBookByTitle(String title) {
        String query = "SELECT b.title, b.ISBN, b.publication_date, b.available_copies, b.book_id " +
                       "FROM Books b WHERE b.title = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Author author = null; 
                String ISBN = rs.getString("ISBN");
                String publicationDate = rs.getString("publication_date");
                int availableCopies = rs.getInt("available_copies");
                int bookId = rs.getInt("book_id");
                return new Book(bookId, title, author, ISBN, publicationDate, availableCopies);
            }
        } catch (SQLException e) {
            System.err.println("Error finding book by title: " + e.getMessage());
        }
        return null;
    }

    // Method to borrow a book
    public void borrowBook(Book book) {
        if (!borrowedBooks.contains(book)) {
            borrowedBooks.add(book);
            book.borrowBook();
            System.out.println("Borrowed Book: " + book.getTitle());
        } else {
            System.out.println("You have already borrowed this book.");
        }
    }

    // Method to return a book
    public void returnBook(Book book) {
        if (borrowedBooks.contains(book)) {
            borrowedBooks.remove(book);
            book.returnBook();
            System.out.println("Returned Book: " + book.getTitle());
        } else {
            System.out.println("You have not borrowed this book.");
        }
    }
}