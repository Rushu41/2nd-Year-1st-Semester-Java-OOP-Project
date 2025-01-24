package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button adminLoginButton;

    @FXML
    private Button userLoginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateAdminCredentials(username, password)) {
            goToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Admin Dashboard");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid admin username or password.");
        }
    }

    @FXML
    private void handleUserLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateUserCredentials(username, password)) {
            goToPage(event, "/com/example/carrentalsystem/userDashboard.fxml", "User Dashboard");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        goToPage(event, "/com/example/carrentalsystem/signUp.fxml", "Sign Up");
    }

    public void handleForgetPassword(Event event) {
        goToPageMouse(event, "/com/example/carrentalsystem/forgotPassword.fxml", "Forgot Password");
    }


    private boolean validateAdminCredentials(String username, String password) {
        try (InputStream inputStream = getClass().getResourceAsStream("/com/example/carrentalsystem/admin.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Admin credentials file not found.");
                return false;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 2) {
                    String csvUsername = credentials[0].trim();
                    String csvPassword = credentials[1].trim();
                    if (csvUsername.equals(username) && csvPassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to read admin credentials file.");
        }

        return false;
    }

    private boolean validateUserCredentials(String username, String password) {
        String query = "SELECT * FROM users WHERE USERNAME = ? AND Password = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not validate user credentials.");
        }

        return false;
    }

    private void goToPage(ActionEvent event, String fxmlFile, String title) {
        try {
            URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "File Not Found", "Could not find " + fxmlFile);
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the page: " + fxmlFile);
        }
    }

    private void goToPageMouse(Event event, String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the page.");
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
