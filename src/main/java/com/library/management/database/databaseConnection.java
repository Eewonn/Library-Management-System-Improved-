package com.library.management.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;

public class databaseConnection {
    private static final String LOCAL_URL = "jdbc:sqlite:src/main/resources/library.db"; // Adjusted for resources
    private static final String RESOURCE_DB_PATH = "/library.db"; // Path inside resources

    // SQL Queries
    private static final String SQL_AUTHENTICATE_USER = "SELECT * FROM users WHERE username = ? AND password = ?";
    private static final String SQL_ADD_MEMBER = "INSERT INTO members (member_name, email, phone) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_MEMBER = "UPDATE members SET member_name = ?, email = ?, phone = ? WHERE member_id = ?";
    private static final String SQL_DELETE_MEMBER = "DELETE FROM members WHERE member_id = ?";
    private static final String SQL_VIEW_MEMBERS = "SELECT * FROM members";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");

            // Check if running from a JAR
            if (isRunningFromJar()) {
                return getConnectionFromResource();
            } else {
                return DriverManager.getConnection(LOCAL_URL);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            throw new SQLException(e);
        }
    }

    private static boolean isRunningFromJar() {
        String classpath = databaseConnection.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return classpath.endsWith(".jar");
    }

    private static Connection getConnectionFromResource() throws SQLException {
        try {
            URL dbUrl = databaseConnection.class.getResource(RESOURCE_DB_PATH);
            if (dbUrl == null) {
                throw new SQLException("Database file not found in classpath.");
            }

            // Extract the resource to a temporary file
            Path tempDbPath = Files.createTempFile("library", ".db");
            try (InputStream dbStream = dbUrl.openStream()) {
                Files.copy(dbStream, tempDbPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Build the SQLite JDBC URL from the temporary file
            String jdbcUrl = "jdbc:sqlite:" + tempDbPath.toAbsolutePath().toString();
            return DriverManager.getConnection(jdbcUrl);
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            throw new SQLException(e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace(); // Print the stack trace for debugging
            }
        }
    }

    public static boolean authenticateUser (String username, String password) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_AUTHENTICATE_USER)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace(); // Print the stack trace for debugging
            return false;
        }
    }

    public static void addMember(String name, String email, String phone) {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SQL_ADD_MEMBER)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
            System.out.println("Member added successfully.");
        } catch (SQLException e) {
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }

    public static void updateMember(int id, String name, String email, String phone) {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_MEMBER)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            System.out.println("Member updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }

    public static void deleteMember(int id) {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_MEMBER)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Member deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }

    public static void viewMembers() {
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(SQL_VIEW_MEMBERS); 
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("ID | Name | Email | Phone");
            while (rs.next()) {
                System.out.println(rs.getInt("member_id") + " | " +
                                   rs.getString("member_name") + " | " +
                                   rs.getString("email") + " | " +
                                   rs.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }
}