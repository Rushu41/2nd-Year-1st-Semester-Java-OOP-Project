package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddCarController {

    @FXML
    private TextField carNameField;

    @FXML
    private TextField totalSeatsField;

    @FXML
    private TextField fuelTypeField;

    @FXML
    private TextField rentPriceField;

    @FXML
    private ImageView carImageView;

    @FXML
    private Label imagePlaceholder;

    @FXML
    private Label errorLabel;

    @FXML
    private Button clearImageButton;

    @FXML
    private StackPane customAlertPane;

    @FXML
    private Label alertIcon;

    @FXML
    private Label alertMessage;

    private byte[] carImage; // To store the uploaded image in byte array format.

    /**
     * Handles the "Add Vehicle" button click.
     */
    @FXML
    public void handleAddCar(ActionEvent event) {
        String carName = carNameField.getText().trim();
        String fuelType = fuelTypeField.getText().trim();
        int totalSeats;
        double rentPrice;

        // Validate input fields
        if (carName.isEmpty() || fuelType.isEmpty() || totalSeatsField.getText().isEmpty()
                || rentPriceField.getText().isEmpty() || carImage == null) {
            showCustomAlert("⚠", "All fields must be filled, and an image must be uploaded.");
            return;
        }

        try {
            totalSeats = Integer.parseInt(totalSeatsField.getText().trim());
            rentPrice = Double.parseDouble(rentPriceField.getText().trim());

            if (totalSeats <= 0 || rentPrice <= 0) {
                showCustomAlert("⚠", "Seats and rent price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showCustomAlert("⚠", "Seats and rent price must be valid numbers.");
            return;
        }

        // Insert the car into the database
        if (addCarToDatabase(carName, totalSeats, fuelType, rentPrice, carImage)) {
            showCustomAlert("✔", "Vehicle added successfully.");
            clearFields();
        } else {
            showCustomAlert("✖", "Failed to add the vehicle. Please try again.");
        }
    }

    /**
     * Handles the "Upload Image" button click.
     */
    @FXML
    public void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(carImageView.getScene().getWindow());

        if (file != null) {
            try {
                // Convert the image to byte array
                carImage = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(carImage);
                fis.close();

                // Set the image in the ImageView
                Image image = new Image(file.toURI().toString());
                carImageView.setImage(image);

                // Hide the placeholder
                imagePlaceholder.setVisible(false);

                // Enable the "Clear Image" button
                clearImageButton.setDisable(false);

                showCustomAlert("✔", "Photo uploaded successfully.");
            } catch (IOException e) {
                showCustomAlert("✖", "Failed to upload the image.");
            }
        } else {
            showCustomAlert("⚠", "No file selected. Please choose an image.");
        }
    }

    /**
     * Clears the uploaded image.
     */
    @FXML
    public void clearImage(ActionEvent event) {
        carImageView.setImage(null); // Remove the image from the ImageView
        imagePlaceholder.setVisible(true); // Show the "No Image Selected" placeholder
        carImage = null; // Reset the image byte array
        clearImageButton.setDisable(true); // Disable the "Clear Image" button

        showCustomAlert("✖", "The uploaded image has been cleared.");
    }

    /**
     * Shows a custom alert popup.
     */
    private void showCustomAlert(String icon, String message) {
        alertIcon.setText(icon);
        alertMessage.setText(message);

        // Make the popup visible
        customAlertPane.setVisible(true);

        // Get the current window's position
        Stage stage = (Stage) customAlertPane.getScene().getWindow();
        double x = stage.getX() + (stage.getWidth() - customAlertPane.getWidth()) / 16;
        double y = stage.getY() + (stage.getHeight() - customAlertPane.getHeight()) / 12;

        // Set the popup position
        customAlertPane.setLayoutX(x);
        customAlertPane.setLayoutY(y);
    }



    /**
     * Closes the custom alert popup.
     */
    @FXML
    public void closeCustomAlert() {
        customAlertPane.setVisible(false); // Hide the popup
    }

    /**
     * Inserts a car record into the database.
     */
    private boolean addCarToDatabase(String name, int totalSeats, String fuelType, double rentPrice, byte[] image) {
        String query = "INSERT INTO cars (name, total_seats, fuel_type, rent_price_per_day, photo, available) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setInt(2, totalSeats);
            statement.setString(3, fuelType);
            statement.setDouble(4, rentPrice);
            statement.setBytes(5, image); // Store the image as a BLOB

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            showCustomAlert("✖", "Database error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears all input fields.
     */
    private void clearFields() {
        carNameField.clear();
        totalSeatsField.clear();
        fuelTypeField.clear();
        rentPriceField.clear();
        carImageView.setImage(null);
        imagePlaceholder.setVisible(true);
        carImage = null;
        clearImageButton.setDisable(true);
        errorLabel.setText("");
    }

    /**
     * Navigates back to the dashboard.
     */
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

    /**
     * Navigates to a different page.
     */
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