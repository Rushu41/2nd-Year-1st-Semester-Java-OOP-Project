package com.example.carrentalsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/car_rental_system";
    private static final String USER = "root"; // Replace with your username
    private static final String PASSWORD = "12212108"; // Replace with your password

    public static Connection connect() {
        try {
            // Ensure driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to the database
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }
}
