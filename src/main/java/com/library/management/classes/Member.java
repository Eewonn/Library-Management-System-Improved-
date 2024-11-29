package com.library.management.classes;

import com.library.management.database.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Member extends Person {
    private List<String> borrowedBooks;  // List of book titles as strings

    // Constructor
    public Member(int memberId2, String name, String borrowedBooks2) {
        super(name); // Assuming Person has a constructor that accepts name
        this.borrowedBooks = new ArrayList<>();
    }

    public Member(String name) {
   
    }

    // Getter for borrowedBooks
    public List<String> getBorrowedBooks() {
        return borrowedBooks;
    }

    // Setter for memberId
    public void setMemberId(int memberId) {
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

    @Override
    public String toString() {
        return getName();
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

	public int getMemberId() {
		throw new UnsupportedOperationException("Unimplemented method 'getMemberId'");
	}
}
