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
        navigateToPage(event, "/com/example/carrentalsystem/rentCar.fxml", "Rent Car");
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
        navigateToPage(event, "/com/example/carrentalsystem/myBookings.fxml", "My Bookings");
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
