package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    /**
     * Handles the login button click event.
     *
     * @param event the ActionEvent triggered by the button click.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate credentials against the CSV file
        if (validateCredentials(username, password)) {
            goToDashboard(event); // Navigate to the dashboard
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    /**
     * Validates the credentials against the "admin.csv" file.
     *
     * @param username the username entered by the user.
     * @param password the password entered by the user.
     * @return true if credentials are valid; false otherwise.
     */
    private boolean validateCredentials(String username, String password) {
        // Correct path to access admin.csv in resources
        try (InputStream inputStream = getClass().getResourceAsStream("/com/example/carrentalsystem/admin.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Check if the resource file exists
            if (inputStream == null) {
                System.err.println("Resource file 'admin.csv' not found in the classpath.");
                showAlert(Alert.AlertType.ERROR, "Error", "Credentials file not found.");
                return false;
            }

            // Read file line by line and validate credentials
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 2) {
                    String csvUsername = credentials[0].trim();
                    String csvPassword = credentials[1].trim();
                    if (csvUsername.equals(username) && csvPassword.equals(password)) {
                        return true; // Valid credentials
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to read credentials file.");
        }

        return false; // Return false if no match is found
    }

    /**
     * Navigates to the dashboard scene after successful login.
     *
     * @param event the ActionEvent triggered by the button click.
     */
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/dashboard.fxml"));
            Scene dashboardScene = new Scene(fxmlLoader.load());

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the dashboard page.");
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param alertType the type of alert (e.g., ERROR, INFORMATION).
     * @param title     the title of the alert dialog.
     * @param message   the message to display in the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
