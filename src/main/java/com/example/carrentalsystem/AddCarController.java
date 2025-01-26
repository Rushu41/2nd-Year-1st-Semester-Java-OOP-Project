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
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddCarController {

    @FXML
    private TextField carNameField;

    @FXML
    private TextField carTypeField;

    @FXML
    private TextField carPriceField;

    @FXML
    private Label errorLabel; // A label for error messages

    public void handleAddCar(ActionEvent event) {
        String carName = carNameField.getText();
        String carType = carTypeField.getText();
        double carPrice;

        if (carName.isEmpty() || carType.isEmpty() || carPriceField.getText().isEmpty()) {
            showError("All fields must be filled.");
            return;
        }

        try {
            carPrice = Double.parseDouble(carPriceField.getText());
            if (carPrice <= 0) {
                showError("Price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            return;
        }

        if (addCarToDatabase(carName, carType, carPrice)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Car added successfully.");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the car. Please try again.");
        }
    }

    private boolean addCarToDatabase(String name, String type, double price) {
        String query = "INSERT INTO cars (name, type, price, available) VALUES (?, ?, ?, 1)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, type);
            statement.setDouble(3, price);
            statement.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Handle the hover effect when mouse enters the button
    public void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12px 30px; -fx-background-radius: 20px;");
        button.setEffect(new DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.3)));  // Apply shadow effect on hover
    }

    // Handle the hover effect when mouse exits the button
    public void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12px 30px; -fx-background-radius: 20px;");
        button.setEffect(null);  // Remove shadow effect when mouse exits
    }

    private void clearFields() {
        carNameField.clear();
        carTypeField.clear();
        carPriceField.clear();
        errorLabel.setText("");  // Clear error message when fields are cleared
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
