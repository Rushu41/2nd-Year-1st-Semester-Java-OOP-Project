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
import java.sql.*;
import java.nio.file.StandardCopyOption;

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

        @FXML
        public void initialize() {
            if (statusLabel == null) {
                System.out.println("statusLabel is null!");
            } else {
                System.out.println("statusLabel is initialized.");
            }
        }

        public void setLoggedInUsername(String username) {
            this.loggedInUsername = username;
            loadUserData();  // Load user data using the username
        }

        private void loadUserData() {
            String loggedInUsername = UserSession.getLoggedInUsername();  // Get the username from UserSession
            System.out.println("Logged-in username: " + loggedInUsername); // Debugging output

            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                showError("Logged-in username is null or empty!");
                return;
            }

            String query = "SELECT * FROM users WHERE USERNAME = ?";
            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, loggedInUsername);
                System.out.println("Executing query: " + preparedStatement); // Debugging output

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    populateFields(resultSet);
                    loadProfilePhotoFromBlob(resultSet.getBytes("photo"));
                } else {
                    showError("User not found in the database for username: " + loggedInUsername);
                }

            } catch (SQLException e) {
                showError("Error loading user data: " + e.getMessage());
                e.printStackTrace(); // Debugging output
            } catch (Exception e) {
                showError("Unexpected error: " + e.getMessage());
                e.printStackTrace(); // Debugging output
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
                    e.printStackTrace(); // Debugging output
                }
            } else {
                System.out.println("Profile photo is not set for this user.");
                if (statusLabel != null) {
                    statusLabel.setText("Profile photo not set.");
                }
            }
        }

        @FXML
        public void handlePhotoUpload() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

            // Let the user choose a file
            selectedPhotoFile = fileChooser.showOpenDialog(null);

            if (selectedPhotoFile != null) {
                // Display the selected photo in the UI
                Image image = new Image(selectedPhotoFile.toURI().toString());
                photoCircle.setFill(new ImagePattern(image));
                if (statusLabel != null) {
                    statusLabel.setText("Photo selected: " + selectedPhotoFile.getName());
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("No photo selected.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        }

        @FXML
        public void handleSaveChanges() {
            boolean profileUpdated = updateUserProfile();
            boolean photoUpdated = updateProfilePhoto();

            if (profileUpdated && photoUpdated) {
                if (statusLabel != null) {
                    statusLabel.setText("Profile updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            } else if (profileUpdated) {
                showError("Profile details updated, but the photo was not.");
            } else if (photoUpdated) {
                showError("Photo updated, but profile details were not.");
            } else {
                showError("No changes were made.");
            }
        }

        private boolean updateUserProfile() {
            loggedInUsername = usernameField.getText();
            String query = "UPDATE users SET Email = ?, Phone = ?, Password = ? WHERE USERNAME = ?";

            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                if (emailField.getText().trim().isEmpty() && phoneField.getText().trim().isEmpty() && passwordField.getText().trim().isEmpty()) {
                    showError("At least one field (Email, Phone, or Password) must be updated.");
                    return false;
                }

                preparedStatement.setString(1, emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                preparedStatement.setString(2, phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                preparedStatement.setString(3, passwordField.getText().trim().isEmpty() ? null : passwordField.getText().trim());
                preparedStatement.setString(4, loggedInUsername);

                int rowsUpdated = preparedStatement.executeUpdate();
                System.out.println("Rows Updated: " + rowsUpdated);

                return rowsUpdated > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error updating profile: " + e.getMessage());
                return false;
            }
        }

        private boolean updateProfilePhoto() {
            if (selectedPhotoFile == null) {
                System.out.println("No photo selected. Skipping photo update.");
                return true; // No photo update needed
            }
            loggedInUsername=usernameField.getText();

            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                showError("Cannot update photo: logged-in username is null or empty.");
                return false;
            }

            String query = "UPDATE users SET photo = ? WHERE USERNAME = ?";
            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                try {
                    byte[] photoBytes = Files.readAllBytes(selectedPhotoFile.toPath());
                    preparedStatement.setBytes(1, photoBytes);
                } catch (IOException e) {
                    showError("Error reading photo file: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }

                preparedStatement.setString(2, loggedInUsername);

                int rowsUpdated = preparedStatement.executeUpdate();
                System.out.println("Photo update query executed. Rows updated: " + rowsUpdated);

                return rowsUpdated > 0;

            } catch (SQLException e) {
                showError("Database error while updating photo: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        private void showError(String message) {
            if (statusLabel != null) {
                statusLabel.setText(message);
                statusLabel.setStyle("-fx-text-fill: red;");
            } else {
                System.err.println("statusLabel is null! Message: " + message);
            }
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
                stage.setWidth(1000); // Your fixed width
                stage.setHeight(800); // Your fixed height
                stage.setResizable(false); // Disable resizing

                stage.setTitle(title);
                stage.show();
            } catch (IOException e) {
                showError("Error loading page: " + e.getMessage());
            }
        }
    }
