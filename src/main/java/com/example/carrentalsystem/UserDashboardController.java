package com.example.carrentalsystem;

import javafx.animation.*;
import javafx.util.Duration;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class UserDashboardController {
    private List<Image> carImages = new ArrayList<>();
    private List<String> carNames = new ArrayList<>();
    private List<String> carFuelTypes = new ArrayList<>();
    private List<Double> carRentPrices = new ArrayList<>();

    @FXML
    public ImageView rotatingCarImage;
    private int currentImageIndex = 0;

    @FXML
    private Circle userPhotoCircle;

    @FXML
    private Label usernameLabel;

    @FXML
    private FlowPane carFlowPane;

    @FXML
    private VBox carInfoBox;

    @FXML
    private Label carNameLabel;

    @FXML
    private Label carFuelTypeLabel;

    @FXML
    private Label carRentPriceLabel;

    private String loggedInUsername;
    private String username; // Store the username for the logged-in user

    // Getter for the username
    public String getUsername() {
        return username;
    }

    // Setter for the username
    public void setUsername(String username) {
        this.username = username;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        usernameLabel.setText("Welcome, " + username);
    }

    @FXML
    public void initialize() {
//        loadUserProfilePhoto();
        loadCars();
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
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Image defaultImage = new Image(getClass().getResource("/com/example/carrentalsystem/images/default_profile.jpg").toString());
            userPhotoCircle.setFill(new ImagePattern(defaultImage));
        } catch (NullPointerException e) {
            System.err.println("Default profile image not found.");
        }
    }

    private void loadCars() {
        String query = "SELECT name, fuel_type, rent_price_per_day, photo FROM cars WHERE available = 1";
        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String fuelType = resultSet.getString("fuel_type");
                double rentPrice = resultSet.getDouble("rent_price_per_day");
                Blob photoBlob = resultSet.getBlob("photo");

                if (photoBlob != null) {
                    InputStream inputStream = photoBlob.getBinaryStream();
                    Image carImage = new Image(inputStream);
                    carImages.add(carImage);
                    carNames.add(name);
                    carFuelTypes.add(fuelType);
                    carRentPrices.add(rentPrice);
                }
            }

            if (!carImages.isEmpty()) {
                startImageRotation();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startImageRotation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (!carImages.isEmpty()) {
                currentImageIndex = (currentImageIndex + 1) % carImages.size();
                rotatingCarImage.setImage(carImages.get(currentImageIndex));
                updateCarInfo(currentImageIndex);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateCarInfo(int index) {
        if (index >= 0 && index < carNames.size()) {
            carNameLabel.setText(carNames.get(index));
            carFuelTypeLabel.setText(carFuelTypes.get(index));
            carRentPriceLabel.setText(+carRentPrices.get(index) + "/day");
            carInfoBox.setVisible(true);
        } else {
            carInfoBox.setVisible(false);
        }
    }

    public void handleProfile(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/profile.fxml", "Profile");
    }

    public void handleRentCar(ActionEvent event) {
        // Pass the username to RentCarController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCar.fxml"));
            Scene scene = new Scene(loader.load());

            RentCarController rentCarController = loader.getController();
            rentCarController.setUsername(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Rent Car");
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
            stage.setResizable(false); // Disable resizing
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSubscription(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/subscription.fxml", "Subscription");
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
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
            stage.setResizable(false); // Disable resizing
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleFeedback(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/feedback.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the FeedbackController instance
            FeedbackController feedbackController = loader.getController();

            // Pass the logged-in username to the controller
            feedbackController.setLoggedInUsername(loggedInUsername);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Feedback");
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
            stage.setResizable(false); // Disable resizing
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
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
            stage.setResizable(false); // Disable resizing
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}