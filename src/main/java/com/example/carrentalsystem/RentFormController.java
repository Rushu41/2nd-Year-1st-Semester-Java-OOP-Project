package com.example.carrentalsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RentFormController {

    @FXML
    private Label carNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private RadioButton hourlyRadioButton;

    @FXML
    private RadioButton dailyRadioButton;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button rentButton;

    private int carId;
    private double rentPricePerDay;

    public void setCarDetails(int carId, String carName, double rentPricePerDay, String username) {
        this.carId = carId;
        this.rentPricePerDay = rentPricePerDay;
        carNameLabel.setText("Car: " + carName);
        usernameLabel.setText("User: " + username);
    }

    @FXML
    private void rentCar() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        boolean isHourly = hourlyRadioButton.isSelected();

        if (startDate == null || (!isHourly && (endDate == null || startDate.isAfter(endDate)))) {
            showAlert("Invalid Input", "Please provide valid dates.");
            return;
        }

        double totalCost = 0.0;
        if (isHourly) {
            totalCost = 0.4 * rentPricePerDay;
        } else {
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            totalCost = days * rentPricePerDay;
        }

        String query = "INSERT INTO rentals (car_id, user_name, start_date, end_date, total_cost, rental_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, carId);
            preparedStatement.setString(2, usernameLabel.getText().split(": ")[1]);
            preparedStatement.setDate(3, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(4, isHourly ? null : java.sql.Date.valueOf(endDate));
            preparedStatement.setDouble(5, totalCost);
            preparedStatement.setString(6, isHourly ? "Hourly" : "Daily");

            preparedStatement.executeUpdate();
            showAlert("Success", "Car rented successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to rent the car.");
        }
    }
    @FXML
    public void handleGenerateReceipt() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert("Invalid Input", "Please provide valid rental dates.");
            return;
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalCost = rentalDays * rentPricePerDay;

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
            """.formatted(carNameLabel.getText().split(": ")[1], usernameLabel.getText().split(": ")[1],
                startDate, endDate, rentalDays, rentPricePerDay, totalCost);

        // Open Receipt View
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/ReceiptView.fxml"));
            VBox receiptView = loader.load();

            ReceiptViewController controller = loader.getController();
            controller.setReceiptContent(receiptContent);

            Stage stage = new Stage();
            stage.setTitle("Rental Receipt");
            stage.setScene(new Scene(receiptView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the receipt view.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
