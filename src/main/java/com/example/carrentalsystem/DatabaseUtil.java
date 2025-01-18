package com.example.carrentalsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseUtil {
    public static void insertUser(String firstName, String lastName, String gender, String email, String username, String password, int age) {
        String insertQuery = "INSERT INTO users (first_name, last_name, gender, email, username, password, age) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, gender);
            pstmt.setString(4, email);
            pstmt.setString(5, username);
            pstmt.setString(6, password); // Store the plain password
            pstmt.setInt(7, age);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while inserting user data into the database.");
        }
    }
}
