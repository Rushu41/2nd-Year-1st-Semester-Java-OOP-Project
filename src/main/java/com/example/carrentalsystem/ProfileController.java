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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {

    @FXML
    private Circle photoCircle;
    @FXML
    private TextField userIdField, firstNameField, lastNameField, ageField, genderField, dateOfBirthField, usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    private String loggedInUsername;
    private File selectedPhotoFile;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadUserData();
    }

    private void loadUserData() {
        String query = "SELECT * FROM users WHERE USERNAME = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, loggedInUsername);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userIdField.setText(resultSet.getString("user_id"));
                firstNameField.setText(resultSet.getString("First_Name"));
                lastNameField.setText(resultSet.getString("Last_Name"));
                ageField.setText(String.valueOf(resultSet.getInt("Age")));
                genderField.setText(resultSet.getString("Gender"));
                dateOfBirthField.setText(resultSet.getString("Date_Of_Birth"));
                usernameField.setText(resultSet.getString("USERNAME"));
                passwordField.setText(resultSet.getString("Password"));

                String photoPath = resultSet.getString("photo");
                if (photoPath != null && !photoPath.isEmpty()) {
                    File file = new File(photoPath);
                    if (file.exists()) {
                        Image profileImage = new Image(file.toURI().toString());
                        photoCircle.setFill(new ImagePattern(profileImage));
                    }
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Error loading user data: " + e.getMessage());
        }
    }

    @FXML
    public void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedPhotoFile = fileChooser.showOpenDialog(null);
        if (selectedPhotoFile != null) {
            Image image = new Image(selectedPhotoFile.toURI().toString());
            photoCircle.setFill(new ImagePattern(image));
        }
    }

    @FXML
    public void handleSaveChanges() {
        String query = "UPDATE users SET First_Name = ?, Last_Name = ?, Age = ?, Gender = ?, Date_Of_Birth = ?, USERNAME = ?, Password = ?, photo = ? WHERE USERNAME = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, firstNameField.getText());
            preparedStatement.setString(2, lastNameField.getText());
            preparedStatement.setInt(3, Integer.parseInt(ageField.getText()));
            preparedStatement.setString(4, genderField.getText());
            preparedStatement.setString(5, dateOfBirthField.getText());
            preparedStatement.setString(6, usernameField.getText());
            preparedStatement.setString(7, passwordField.getText());
            preparedStatement.setString(8, selectedPhotoFile != null ? selectedPhotoFile.getAbsolutePath() : null);
            preparedStatement.setString(9, loggedInUsername);

            preparedStatement.executeUpdate();
            statusLabel.setText("Profile updated successfully!");
        } catch (SQLException | NumberFormatException e) {
            statusLabel.setText("Failed to update profile: " + e.getMessage());
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
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
