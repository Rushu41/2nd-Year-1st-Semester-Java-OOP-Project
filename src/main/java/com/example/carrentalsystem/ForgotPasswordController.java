package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField dobField;

    @FXML
    private TextField newPasswordField;

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String username = usernameField.getText();
        String dob = dobField.getText();
        String newPassword = newPasswordField.getText();

        if (username.isEmpty() || dob.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }

        if (validateUserDOB(username, dob)) {
            if (updatePassword(username, newPassword)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
                navigateToLogin(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or date of birth.");
        }
    }

    private boolean validateUserDOB(String username, String dob) {
        String query = "SELECT * FROM users WHERE USERNAME = ? AND Date_Of_Birth = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, dob);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not validate user.");
        }

        return false;
    }

    private boolean updatePassword(String username, String newPassword) {
        String query = "UPDATE users SET Password = ? WHERE USERNAME = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newPassword);
            statement.setString(2, username);
            int rowsUpdated = statement.executeUpdate();

            return rowsUpdated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update password.");
        }

        return false;
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the login page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
