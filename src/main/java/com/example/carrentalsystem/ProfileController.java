package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class ProfileController {

    @FXML
    private Circle photoCircle;
    @FXML
    private TextField userIdField, firstNameField, lastNameField, ageField, genderField, dateOfBirthField, usernameField;
    @FXML
    private TextField emailField, phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    private String loggedInUsername;
    private File selectedPhotoFile;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadUserData();  // Load user data using the username
    }

    private void loadUserData() {
        String loggedInUsername = UserSession.getLoggedInUsername();  // Get the username from UserSession
        String query = "SELECT * FROM users WHERE USERNAME = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, loggedInUsername);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                populateFields(resultSet);
                loadProfilePhotoFromBlob(resultSet.getBytes("photo"));
            } else {
                statusLabel.setText("User not found!");
            }

        } catch (SQLException e) {
            showError("Error loading user data: " + e.getMessage());
        }
    }

    private void populateFields(ResultSet resultSet) throws SQLException {
        userIdField.setText(resultSet.getString("user_id"));
        firstNameField.setText(resultSet.getString("First_Name"));
        lastNameField.setText(resultSet.getString("Last_Name"));
        ageField.setText(resultSet.getString("Age"));
        genderField.setText(resultSet.getString("Gender"));
        dateOfBirthField.setText(resultSet.getString("Date_Of_Birth"));
        usernameField.setText(resultSet.getString("USERNAME"));
        emailField.setText(resultSet.getString("Email"));
        phoneField.setText(resultSet.getString("Phone"));
        passwordField.setText(resultSet.getString("Password"));
    }

    private void loadProfilePhotoFromBlob(byte[] photoBlob) {
        if (photoBlob != null) {
            try {
                InputStream is = new ByteArrayInputStream(photoBlob);
                Image image = new Image(is);
                photoCircle.setFill(new ImagePattern(image));
            } catch (Exception e) {
                showError("Failed to load profile photo: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Profile photo not set.");
        }
    }

    @FXML
    public void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedPhotoFile = fileChooser.showOpenDialog(null);
        if (selectedPhotoFile != null) {
            // Show the selected photo in the UI
            Image image = new Image(selectedPhotoFile.toURI().toString());
            photoCircle.setFill(new ImagePattern(image));
        }
    }

    @FXML
    public void handleSaveChanges() {
        boolean profileUpdated = updateUserProfile();
        boolean photoUpdated = updateProfilePhoto();

        if (profileUpdated && photoUpdated) {
            statusLabel.setText("Profile updated successfully!");
        } else if (profileUpdated) {
            showError("Profile details updated, but the photo was not.");
        } else if (photoUpdated) {
            showError("Photo updated, but profile details were not.");
        } else {
            showError("No changes were made.");
        }
    }

    private boolean updateUserProfile() {
        String query = "UPDATE users SET Email = ?, Phone = ?, Password = ? WHERE USERNAME = ?";
        int rowsUpdated = executeUpdateQuery(query, preparedStatement -> {
            preparedStatement.setString(1, emailField.getText().trim());
            preparedStatement.setString(2, phoneField.getText().trim());
            preparedStatement.setString(3, passwordField.getText().trim());
            preparedStatement.setString(4, loggedInUsername);
        });

        return rowsUpdated > 0;
    }

    private boolean updateProfilePhoto() {
        if (selectedPhotoFile == null) {
            return true; // No photo update needed
        }

        String query = "UPDATE users SET photo = ? WHERE USERNAME = ?";
        int rowsUpdated = executeUpdateQuery(query, preparedStatement -> {
            try {
                byte[] photoBytes = Files.readAllBytes(selectedPhotoFile.toPath());
                preparedStatement.setBytes(1, photoBytes);
            } catch (IOException e) {
                showError("Error reading photo file: " + e.getMessage());
            }
            preparedStatement.setString(2, loggedInUsername);
        });

        return rowsUpdated > 0;
    }

    private int executeUpdateQuery(String query, PreparedStatementHandler handler) {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Pass the prepared statement to the handler for customization
            handler.handle(preparedStatement);

            // Execute the update query and return rows affected
            return preparedStatement.executeUpdate();

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
        return 0;
    }

    // Functional interface for prepared statement customization
    @FunctionalInterface
    private interface PreparedStatementHandler {
        void handle(PreparedStatement preparedStatement) throws SQLException;
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/userDashboard.fxml", "Dashboard");
    }

    @FXML
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
            showError("Error loading page: " + e.getMessage());
        }
    }
}
