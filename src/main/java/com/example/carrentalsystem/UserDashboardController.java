package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDashboardController {

    @javafx.fxml.FXML
    private Circle userPhotoCircle;

    private String loggedInUsername; // Store logged-in user's username

    private String username; // Store the username for the logged-in user

    // Getter for the username
    public String getUsername() {
        return username;
    }

    // Setter for the username
    public void setUsername(String username) {
        this.username = username;
    }

    public void initialize() {
        loadUserProfilePhoto();
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    private void loadUserProfilePhoto() {
        String query = "SELECT photo FROM users WHERE USERNAME = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, loggedInUsername);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String photoPath = resultSet.getString("photo");
                if (photoPath != null && !photoPath.isEmpty()) {
                    File file = new File(photoPath);
                    if (file.exists()) {
                        Image profileImage = new Image(file.toURI().toString());
                        userPhotoCircle.setFill(new ImagePattern(profileImage));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleProfile(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/profile.fxml", "Profile");
    }

    public void handleRentCar(ActionEvent event) {
        // Pass the username to RentCArController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCar.fxml"));
            Scene scene = new Scene(loader.load());

            RentCarController rentCarController = loader.getController();
            rentCarController.setUsername(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("rent car");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleReturnCar(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/returnCar.fxml", "Return Car");
    }

    public void handleSubscription(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/subscription.fxml", "Subscription");
    }

    public void handleFeedback(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/feedback.fxml", "Feedback");
    }

    public void handleMyBookings(ActionEvent event) {
        // Pass the username to MyBookingsController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/myBookings.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller for the MyBookings.fxml
            MyBookingsController myBookingsController = loader.getController();

            // Pass the username to the MyBookingsController
            myBookingsController.loadUserBookings(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("My Bookings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleLogout(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/login.fxml", "Login");
    }

    private void navigateToPage(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            if (fxmlFile.contains("profile.fxml")) {
                ProfileController controller = loader.getController();
                controller.setLoggedInUsername(loggedInUsername);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
