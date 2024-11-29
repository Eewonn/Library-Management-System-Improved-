package com.library.management.classes;

import com.library.management.database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Member extends Person {
    private int memberId;               // Member ID field
    private List<String> borrowedBooks; // List of book titles as strings

    // Constructor for memberId and name
    public Member(int memberId, String name) {
        super(name); // Assuming Person has a constructor that accepts name
        this.memberId = memberId;
        this.borrowedBooks = new ArrayList<>();
    }

    // Constructor for name only
    public Member(String name) {
        super(name); // Calling the Person constructor
        this.borrowedBooks = new ArrayList<>();
    }

    // Constructor for all fields (used in your original code)
    public Member(int memberId, String name, String borrowedBooksStr) {
        super(name); // Assuming Person has a constructor that accepts name
        this.memberId = memberId;
        this.borrowedBooks = new ArrayList<>();
        if (borrowedBooksStr != null) {
            String[] booksArray = borrowedBooksStr.split(", ");
            for (String bookTitle : booksArray) {
                this.borrowedBooks.add(bookTitle);
            }
        }
    }

    // Getter for memberId
    public int getMemberId() {
        return memberId;
    }

    // Setter for memberId
    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    // Getter for borrowedBooks
    public List<String> getBorrowedBooks() {
        return borrowedBooks;
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
                setMemberId(rs.getInt("memberID"));
                setName(rs.getString("memberName"));

                // Split the concatenated book titles into a list
                String borrowedBooksStr = rs.getString("borrowedBooks");
                if (borrowedBooksStr != null) {
                    String[] booksArray = borrowedBooksStr.split(", ");
                    for (String bookTitle : booksArray) {
                        borrowedBooks.add(bookTitle);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading member from database: " + e.getMessage());
        }
    }

    // Method to borrow a book
    public void borrowBook(Book book) {
        if (!borrowedBooks.contains(book.getTitle())) {
            borrowedBooks.add(book.getTitle());
            System.out.println("Borrowed Book: " + book.getTitle());
        } else {
            System.out.println("You have already borrowed this book.");
        }
    }

    // Method to return a book
    public void returnBook(Book book) {
        if (borrowedBooks.contains(book.getTitle())) {
            borrowedBooks.remove(book.getTitle());
            System.out.println("Returned Book: " + book.getTitle());
        } else {
            System.out.println("You have not borrowed this book.");
        }
    }

    @Override
    public String toString() {
        return getName(); // Ensures only the member's name is displayed
    }

    /*@Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", name='" + getName() + '\'' +
                ", borrowedBooks=" + borrowedBooks +
                '}';
    }*/
    
}
