package com.example.carrentalsystem;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private AnchorPane adminPane, userPane;

    @FXML
    private TextField adminUsernameField, userUsernameField;

    @FXML
    private PasswordField adminPasswordField, userPasswordField;

    @FXML
    private Button adminLoginButton, userLoginButton;

    @FXML
    public void initialize() {
        // Set user login page as the default view
        adminPane.setTranslateX(-1000); // Hide the admin pane
        userPane.setTranslateX(0); // Show the user pane
    }

    @FXML
    private void switchToAdmin() {
        animatePane(adminPane, 0);
        animatePane(userPane, 1000);
    }

    @FXML
    private void switchToUser() {
        animatePane(adminPane, -1000);
        animatePane(userPane, 0);
    }

    private void animatePane(AnchorPane pane, double targetX) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), pane);
        transition.setToX(targetX);
        transition.play();
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        String username = adminUsernameField.getText();
        String password = adminPasswordField.getText();

        if (validateAdminCredentials(username, password)) {
            goToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Admin Dashboard");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid admin username or password.");
        }
    }

    @FXML
    private void handleUserLogin(ActionEvent event) {
        String username = userUsernameField.getText();
        String password = userPasswordField.getText();

        if (validateCredentials(username, password, "users")) {
            // Set the username in UserSession class
            UserSession.setLoggedInUsername(username);

            try {
                // Navigate to the user dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/userDashboard.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Get the controller for the user dashboard
                UserDashboardController userDashboardController = loader.getController();

                // Pass the username to the UserDashboardController
                userDashboardController.setUsername(username);
                stage.setWidth(1000); // Your fixed width
                stage.setHeight(800); // Your fixed height
                stage.setResizable(false); // Disable resizing

                stage.setScene(scene);
                stage.setTitle("User Dashboard");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the page: userDashboard.fxml");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid user username or password.");
        }
    }

    private boolean validateAdminCredentials(String username, String password) {
        try (InputStream inputStream = getClass().getResourceAsStream("/com/example/carrentalsystem/local_database/admin.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                System.err.println("Resource file 'admin.csv' not found in the classpath.");
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

    private boolean validateCredentials(String username, String password, String tableName) {
        String query = "SELECT * FROM " + tableName + " WHERE USERNAME = ? AND PASSWORD = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not validate credentials.");
        }
        return false;
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        goToPage(event, "/com/example/carrentalsystem/signUp.fxml", "Sign Up");
    }

    @FXML
    public void handleForgetPassword(Event event) {
        goToPageMouse(event, "/com/example/carrentalsystem/forgotPassword.fxml", "Forgot Password");
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the page.");
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