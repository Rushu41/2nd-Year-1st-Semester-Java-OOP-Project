// AddCarController.java
package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Button uploadImageButton;

    @FXML
    private ImageView carImageView;

    @FXML
    private Label carNameLabel;



    @FXML
    private Label errorLabel;

    private byte[] carImage;  // To store the image in byte array

    @FXML
    public void handleAddCar(ActionEvent event) {
        String carName = carNameField.getText().trim();
        String fuelType = fuelTypeField.getText().trim();
        int totalSeats;
        double rentPrice;

        // Validate inputs
        if (carName.isEmpty() || fuelType.isEmpty() || totalSeatsField.getText().isEmpty()
                || rentPriceField.getText().isEmpty() || carImage == null) {
            showError("All fields must be filled and a photo must be uploaded.");
            return;
        }

        try {
            totalSeats = Integer.parseInt(totalSeatsField.getText().trim());
            rentPrice = Double.parseDouble(rentPriceField.getText().trim());

            if (totalSeats <= 0 || rentPrice <= 0) {
                showError("Seats and rent price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Seats and rent price must be valid numbers.");
            return;
        }

        // Insert car into the database
        if (addCarToDatabase(carName, totalSeats, fuelType, rentPrice, carImage)) {
            carNameLabel.setText("Car's Name: " + carName);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Car added successfully.");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the car. Please try again.");
        }
    }
    public void handleImageUpload(ActionEvent event) {
        if (carNameLabel == null) {
            System.out.println("carNameLabel is null! Check FXML file and fx:id.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (file != null) {
            try {
                // Convert the image file to a byte array
                carImage = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(carImage);
                fis.close();

                // Display the image in the ImageView
                Image image = new Image(file.toURI().toString());
                carImageView.setImage(image);

                // Set the car's name in the label
                carNameLabel.setText("Car's Name: " + carNameField.getText().trim());

                showAlert(Alert.AlertType.INFORMATION, "Success", "Photo uploaded successfully.");
            } catch (IOException e) {
                showError("Failed to read the image file.");
            }
        }
    }


    private boolean addCarToDatabase(String name, int totalSeats, String fuelType, double rentPrice, byte[] image) {
        String query = "INSERT INTO cars (name, total_seats, fuel_type, rent_price_per_day, photo, available) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setInt(2, totalSeats);
            statement.setString(3, fuelType);
            statement.setDouble(4, rentPrice);
            statement.setBytes(5, image); // Insert photo as a BLOB

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
            return false;
        }
    }

    private void clearFields() {
        carNameField.clear();
        totalSeatsField.clear();
        fuelTypeField.clear();
        rentPriceField.clear();
        errorLabel.setText("");
        carImage = null;
        carImageView.setImage(null);
        carNameLabel.setText("");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
    }

    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
