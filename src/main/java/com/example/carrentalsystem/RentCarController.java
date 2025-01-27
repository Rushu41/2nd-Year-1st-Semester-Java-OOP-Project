package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private RadioButton hourlyRadioButton;

    @FXML
    private RadioButton dailyRadioButton;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label totalCostLabel;

    @FXML
    private TextField hourlyTextField;


    private String username; // Store the username for the logged-in user

    // Getter for the username
    public String getUsername() {
        return username;
    }

    // Setter for the username
    public void setUsername(String username) {
        this.username = username;
    }


    private ToggleGroup rentOptionGroup;

    @FXML
    public void initialize() {
        rentOptionGroup = new ToggleGroup();
        hourlyRadioButton.setToggleGroup(rentOptionGroup);
        dailyRadioButton.setToggleGroup(rentOptionGroup);

        // Default selection
        hourlyRadioButton.setSelected(true);
        handleRentalOptionChange();

        loadCarNames();
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
    @FXML
    public void handleCalculateCost() {
        String carName = carNameComboBox.getValue();

        if (carName == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a car.");
            return;
        }

        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT rent_price_per_day FROM cars WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, carName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double rentPricePerDay = resultSet.getDouble("rent_price_per_day");
                double totalCost = 0;

                if (hourlyRadioButton.isSelected()) {
                    String hoursText = hourlyTextField.getText();
                    if (hoursText == null || hoursText.isEmpty() || !hoursText.matches("\\d+")) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number of hours.");
                        return;
                    }
                    int hours = Integer.parseInt(hoursText);
                    totalCost = hours * (0.4 * rentPricePerDay ); // Hourly cost
                } else if (dailyRadioButton.isSelected()) {
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();

                    if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid start and end dates.");
                        return;
                    }

                    long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    totalCost = rentalDays * rentPricePerDay; // Daily cost
                }

                totalCostLabel.setText(String.format("Total Cost: $%.2f", totalCost));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Car details not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch car price.");
        }
    }

    @FXML
    public void handleRentalOptionChange() {
        boolean isHourly = hourlyRadioButton.isSelected();

        // Show/hide fields based on the rental option
        hourlyTextField.setVisible(isHourly);
        startDatePicker.setVisible(!isHourly);
        endDatePicker.setVisible(!isHourly);
    }
    @FXML
    public void handleRentCar(ActionEvent event) {
        String carName = carNameComboBox.getValue();
        String customerName = username; // Use logged-in username
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (carName == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a car.");
            return;
        }

        try (Connection connection = DatabaseConnector.connect()) {
            String insertQuery;
            PreparedStatement insertStatement;
            String rentalType;
            double totalCost = 0;

            if (hourlyRadioButton.isSelected()) {
                // Handle hourly rentals
                rentalType = "Hourly";
                String hoursText = hourlyTextField.getText();

                if (hoursText == null || hoursText.isEmpty() || !hoursText.matches("\\d+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number of hours.");
                    return;
                }

                int hours = Integer.parseInt(hoursText);
                double hourlyRate = 0.4 * getRentPricePerDay(carName); // Calculate hourly rate
                totalCost = hours * hourlyRate;

                // Use the current date as a default for start_date and end_date
                LocalDate defaultDate = LocalDate.now();

                // Insert into database with placeholder values for start_date and end_date
                insertQuery = "INSERT INTO rentals (car_name, customer_name, hour, rental_type, total_cost, start_date, end_date,rental_days) VALUES (?, ?, ?, ?, ?, ?, ?,0)";
                insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, carName);
                insertStatement.setString(2, customerName);
                insertStatement.setInt(3, hours);
                insertStatement.setString(4, rentalType);
                insertStatement.setDouble(5, totalCost);
                insertStatement.setObject(6, defaultDate); // Placeholder for start_date
                insertStatement.setObject(7, defaultDate); // Placeholder for end_date
            }
            else if (dailyRadioButton.isSelected()) {
                // Handle daily rentals
                rentalType = "Daily";

                if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid start and end dates.");
                    return;
                }

                long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                double dailyRate = getRentPricePerDay(carName);
                totalCost = rentalDays * dailyRate;

                // Insert into database with start and end dates and rental_days
                insertQuery = "INSERT INTO rentals (car_name, customer_name, start_date, end_date, rental_type, total_cost, rental_days) VALUES (?, ?, ?, ?, ?, ?, ?)";
                insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, carName);
                insertStatement.setString(2, customerName);
                insertStatement.setObject(3, startDate);
                insertStatement.setObject(4, endDate);
                insertStatement.setString(5, rentalType);
                insertStatement.setDouble(6, totalCost);
                insertStatement.setLong(7, rentalDays);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a rental option.");
                return;
            }

            // Execute the insert statement
            insertStatement.executeUpdate();

            // Remove the rented car from the cars table (mark unavailable)
            String deleteOrUpdateQuery = "UPDATE cars SET available = 0 WHERE name = ?";
            PreparedStatement deleteOrUpdateStatement = connection.prepareStatement(deleteOrUpdateQuery);
            deleteOrUpdateStatement.setString(1, carName);
            deleteOrUpdateStatement.executeUpdate();

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Car rented successfully!");

            // Clear input fields
            resetFields();

            // Refresh car table
            loadCarNames();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to rent the car.");
        }
    }


    // Method to reset input fields
    private void resetFields() {
        carNameComboBox.getSelectionModel().clearSelection();
        hourlyRadioButton.setSelected(true); // Default rental option
        hourlyTextField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        totalCostLabel.setText("");
    }



    private double getRentPricePerDay(String carName) throws SQLException {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT rent_price_per_day FROM cars WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, carName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("rent_price_per_day");
            }
        }
        return 0;
    }
    @FXML
    public void handleGenerateReceipt(ActionEvent event) {
        String carName = carNameComboBox.getValue();
        String customerName = username; // Use logged-in username
        double totalCost = 0;

        if (carName == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a car.");
            return;
        }

        try {
            double rentPricePerDay = getRentPricePerDay(carName);

            if (hourlyRadioButton.isSelected()) {
                String hoursText = hourlyTextField.getText();

                if (hoursText == null || hoursText.isEmpty() || !hoursText.matches("\\d+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number of hours.");
                    return;
                }

                int hours = Integer.parseInt(hoursText);
                totalCost = hours * (0.4 * rentPricePerDay ); // Hourly cost

                String receiptContent = """
            Rental Receipt
            -----------------------------
            Rental Type: Hourly
            Car Name: %s
            Customer Name: %s
            Hours Rented: %d
            Cost Per Hour: $%.2f
            -----------------------------
            Total Cost: $%.2f
            -----------------------------
            Thank you for your rental!
            """.formatted(carName, customerName, hours, (0.4 * rentPricePerDay ), totalCost);

                showReceipt(receiptContent);

            } else if (dailyRadioButton.isSelected()) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid start and end dates.");
                    return;
                }

                long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                totalCost = rentalDays * rentPricePerDay; // Daily cost

                String receiptContent = """
            Rental Receipt
            -----------------------------
            Rental Type: Daily
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
            """.formatted(carName, customerName, startDate, endDate, rentalDays, rentPricePerDay, totalCost);

                showReceipt(receiptContent);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a rental option.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch car price.");
        }
    }

    private void showReceipt(String receiptContent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/ReceiptView.fxml"));
            VBox receiptView = loader.load();

            ReceiptViewController controller = loader.getController();
            controller.setReceiptContent(receiptContent);

            Stage stage = new Stage();
            stage.setWidth(800); // Your fixed width
            stage.setHeight(600); // Your fixed height
            stage.setResizable(false); // Disable resizing

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
        navigateToPage(event, "/com/example/carrentalsystem/userDashboard.fxml", "Dashboard");
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
