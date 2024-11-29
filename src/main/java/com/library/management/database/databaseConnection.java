package com.library.management.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class databaseConnection {
    private static final String URL = "jdbc:sqlite:src/main/java/com/library/management/database/library.db";

    // Get Connection Method
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    // Close Connection Method
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Authenticate User Method
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = getConnection(); 
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }

    // Member CRUD Methods

    // Add Member
    public static void addMember(String name, String email, String phone) {
        String sql = "INSERT INTO members (member_name, email, phone) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
            System.out.println("Member added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
    }

    // Update Member
    public static void updateMember(int id, String name, String email, String phone) {
        String sql = "UPDATE members SET member_name = ?, email = ?, phone = ? WHERE member_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            System.out.println("Member updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
        }
    }

    // Delete Member
    public static void deleteMember(int id) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Member deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting member: " + e.getMessage());
        }
    }

    // View All Members
    public static void viewMembers() {
        String sql = "SELECT * FROM members";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("ID | Name | Email | Phone");
            while (rs.next()) {
                System.out.println(rs.getInt("member_id") + " | " +
                                   rs.getString("member_name") + " | " +
                                   rs.getString("email") + " | " +
                                   rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving members: " + e.getMessage());
        }
    }
}
