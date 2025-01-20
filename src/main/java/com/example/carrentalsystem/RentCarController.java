package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RentCarController {

    @FXML
    private ComboBox<String> carNameComboBox;

    @FXML
    private ComboBox<String> customerNameComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    public void initialize() {
        loadCarNames();
        loadCustomerNames();
    }

    private void loadCarNames() {
        String query = "SELECT name FROM cars WHERE available = 1";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                carNameComboBox.getItems().add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load car names.");
        }
    }

    private void loadCustomerNames() {
        String query = "SELECT USERNAME FROM users";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                customerNameComboBox.getItems().add(resultSet.getString("USERNAME"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load customer names.");
        }
    }

    @FXML
    public void handleRentCar(ActionEvent event) {
        String carName = carNameComboBox.getValue();
        String customerName = customerNameComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (carName == null || customerName == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid car, customer, and date details.");
            return;
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double costPerDay = 200.0;
        double totalCost = rentalDays * costPerDay;

        if (rentCar(carName, customerName, startDate, endDate, rentalDays)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", String.format("Car rented successfully!\n\nTotal Cost: $%.2f", totalCost));
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to rent the car. Please try again.");
        }
    }

    private boolean rentCar(String carName, String customerName, LocalDate startDate, LocalDate endDate, long rentalDays) {
        String updateQuery = "UPDATE cars SET available = 0 WHERE name = ?";
        String insertQuery = "INSERT INTO rentals (car_name, customer_name, start_date, end_date, rental_days) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            // Update car availability
            updateStatement.setString(1, carName);
            int updateResult = updateStatement.executeUpdate();

            if (updateResult == 0) {
                return false; // No car updated, meaning the car is not available
            }

            // Insert rental record
            insertStatement.setString(1, carName);
            insertStatement.setString(2, customerName);
            insertStatement.setObject(3, startDate);
            insertStatement.setObject(4, endDate);
            insertStatement.setLong(5, rentalDays);
            insertStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void handleGenerateReceipt(ActionEvent event) {
        String carName = carNameComboBox.getValue();
        String customerName = customerNameComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (carName == null || customerName == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid car, customer, and date details.");
            return;
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double costPerDay = 200.0;
        double totalCost = rentalDays * costPerDay;

        String receiptContent = """
            Rental Receipt
            -----------------------------
            Car Name: %s
            Customer Name: %s
            Start Date: %s
            End Date: %s
            Rental Days: %d
            Cost Per Day: $%.2f
            -----------------------------
            Total Cost: $%.2f
            -----------------------------
            Thank you for your rental!
            """.formatted(carName, customerName, startDate, endDate, rentalDays, costPerDay, totalCost);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/ReceiptView.fxml"));
            VBox receiptView = loader.load();

            ReceiptViewController controller = loader.getController();
            controller.setReceiptContent(receiptContent);

            Stage stage = new Stage();
            stage.setTitle("Receipt");
            stage.setScene(new Scene(receiptView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the receipt view.");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

    @FXML
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
