package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerController {

    @FXML
    private TextField userIdField; // User ID field

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField ageField;

    @FXML
    private TextField genderField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button signupButton;

    private Stage stage;
    private Scene scene;

    @FXML
    public void handleSignup(ActionEvent event) {
        // Retrieve input values
        String userId = userIdField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String age = ageField.getText();
        String gender = genderField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || gender.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill out all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Convert age to integer
            int ageInt = Integer.parseInt(age);

            // Insert data into the database
            insertUser(userId, firstName, lastName, ageInt, gender, username, password);

            // Success message
            statusLabel.setText("Signup successful! Redirecting to login...");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Redirect to login page
            redirectToDashboardPage(event);

        } catch (NumberFormatException e) {
            statusLabel.setText("Age must be a number.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            statusLabel.setText("Error saving data: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (IOException e) {
            statusLabel.setText("Error loading login page.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void insertUser(String userId, String firstName, String lastName, int age, String gender, String username, String password) throws SQLException {
        String query = "INSERT INTO users (user_id, First_Name, Last_Name, Age, Gender, USERNAME, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setInt(4, age);
            preparedStatement.setString(5, gender);
            preparedStatement.setString(6, username);
            preparedStatement.setString(7, password);
            preparedStatement.executeUpdate();
        }
    }

    private void redirectToDashboardPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrentalsystem/dashboard.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setWidth(1000); // Your fixed width
        stage.setHeight(800); // Your fixed height
        stage.setResizable(false); // Disable resizing

        stage.setScene(scene);
        stage.show();
    }
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

    public void handleLogout(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/login.fxml", "Login");
    }

    private void navigateToPage(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
