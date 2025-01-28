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
    private String selectedCarName;
    private String enteredHours;
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private String totalCost;
    @FXML
    private Button generateReceiptButton;
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;

    @FXML
    private Button rentCarButton;

    private static boolean paymentPending = false;
    private static boolean paymentCompleted = false;
    private static boolean receiptGenerated = false;

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

    private ToggleGroup rentOptionGroup; // Declare rentOptionGroup at the class level.
    private String username; // Store the username for the logged-in user

    // Getter for the username
    public String getUsername() {
        return username;
    }

    // Setter for the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        rentOptionGroup = new ToggleGroup();
        hourlyRadioButton.setToggleGroup(rentOptionGroup);
        dailyRadioButton.setToggleGroup(rentOptionGroup);

        hourlyRadioButton.setSelected(true); // Default
        handleRentalOptionChange();

        loadCarNames();
        updateButtonStates();

        // Restore state if available
        restoreState();
    }

    @FXML
    public void handleConfirmBill(ActionEvent event) {
        if (paymentPending) {
            showAlert(Alert.AlertType.INFORMATION, "No Payment Pending", "No payment is currently pending.");
            return;
        }

        // Save current state before navigating
        saveState();

        // Navigate to payment page
        navigateToPage(event, "/com/example/carrentalsystem/rentcarpayment.fxml", "Payment");
    }

    private void saveState() {
        selectedCarName = carNameComboBox.getValue();
        enteredHours = hourlyTextField.getText();
        selectedStartDate = startDatePicker.getValue();
        selectedEndDate = endDatePicker.getValue();
        totalCost = totalCostLabel.getText();
    }

    private void restoreState() {
        if (selectedCarName != null) carNameComboBox.setValue(selectedCarName);
        if (enteredHours != null) hourlyTextField.setText(enteredHours);
        if (selectedStartDate != null) startDatePicker.setValue(selectedStartDate);
        if (selectedEndDate != null) endDatePicker.setValue(selectedEndDate);
        if (totalCost != null) totalCostLabel.setText(totalCost);
    }

    private void updateButtonStates() {
        // Initial state: only back and logout enabled
        if (!paymentPending && !paymentCompleted && !receiptGenerated) {
            backButton.setDisable(false);
            logoutButton.setDisable(false);
            rentCarButton.setDisable(true);
            generateReceiptButton.setDisable(true);

        }
        // After clicking confirm bill and before payment
        else if (paymentPending && !paymentCompleted) {
            backButton.setDisable(false);
            logoutButton.setDisable(false);
            rentCarButton.setDisable(true);
            generateReceiptButton.setDisable(true);

        }

        // After payment completed
        else if (paymentCompleted && !receiptGenerated) {
            backButton.setDisable(false);
            logoutButton.setDisable(false);
            rentCarButton.setDisable(true);
            generateReceiptButton.setDisable(false);

        }
        // After generating receipt
        else if (paymentCompleted && receiptGenerated) {
            backButton.setDisable(false);
            logoutButton.setDisable(false);
            rentCarButton.setDisable(false);
            generateReceiptButton.setDisable(true);
        }
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
    private RentCarController rentCarController;

    public void setRentCarController(RentCarController rentCarController) {
        this.rentCarController = rentCarController;
    }

    @FXML
    public void handleSaveAndReturn(ActionEvent event) {
        rentCarController.completePayment(); // Notify rentCarController that payment is done
        rentCarController.restoreState(); // Restore the state
        navigateBack(event); // Return to the rentcar.fxml
    }
    public void completePayment() {
        paymentPending = false;
        paymentCompleted = true;
        updateButtonStates(); // Update button states based on the new status
    }

    private void navigateBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentcar.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Rent Car");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to the Rent Car page.");
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
                    if (hours >= 24) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "The number of hours must be less than 24.");
                        return;
                    }
                    totalCost = hours * (0.4 * rentPricePerDay); // Hourly cost
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
        if (!receiptGenerated) {
            showAlert(Alert.AlertType.WARNING, "Receipt Required", "Please generate receipt first.");
            return;
        }

        String carName = carNameComboBox.getValue();
        String customerName = username;
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
                rentalType = "Hourly";
                String hoursText = hourlyTextField.getText();

                if (hoursText == null || hoursText.isEmpty() || !hoursText.matches("\\d+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number of hours.");
                    return;
                }

                int hours = Integer.parseInt(hoursText);
                double hourlyRate = 0.4 * getRentPricePerDay(carName);
                totalCost = hours * hourlyRate;

                LocalDate defaultDate = LocalDate.now();
                insertQuery = "INSERT INTO rentals (car_name, customer_name, hour, rental_type, total_cost, start_date, end_date, rental_days) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
                insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, carName);
                insertStatement.setString(2, customerName);
                insertStatement.setInt(3, hours);
                insertStatement.setString(4, rentalType);
                insertStatement.setDouble(5, totalCost);
                insertStatement.setObject(6, defaultDate);
                insertStatement.setObject(7, defaultDate);
            } else if (dailyRadioButton.isSelected()) {
                rentalType = "Daily";

                if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid start and end dates.");
                    return;
                }

                long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                double dailyRate = getRentPricePerDay(carName);
                totalCost = rentalDays * dailyRate;

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

            insertStatement.executeUpdate();

            String updateQuery = "UPDATE cars SET available = 0 WHERE name = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, carName);
            updateStatement.executeUpdate();

            paymentPending = false;
            paymentCompleted = false;
            receiptGenerated = false;
            updateButtonStates();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Car rented successfully!");

            // Only update the car list without clearing fields
            loadCarNames();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to rent the car.");
        }
    }


    // Method to be called when returning from payment page with "Save" button
    public void handlePaymentSave() {
        paymentCompleted = true;
        updateButtonStates();
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
        if (!paymentCompleted) {
            showAlert(Alert.AlertType.WARNING, "Payment Required", "Please complete the payment first.");
            return;
        }

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
                totalCost = hours * (0.4 * rentPricePerDay); // Hourly cost

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
            """.formatted(carName, customerName, hours, (0.4 * rentPricePerDay), totalCost);

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

            // After generating receipt
            receiptGenerated = true;
            updateButtonStates();

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
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
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
        if (paymentPending) {
            showAlert(Alert.AlertType.WARNING, "Payment Required", "Please complete the payment first.");
            return;
        }
        navigateToPage(event, "/com/example/carrentalsystem/userDashboard.fxml", "Dashboard");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (paymentPending) {
            showAlert(Alert.AlertType.WARNING, "Payment Required", "Please complete the payment first.");
            return;
        }
        navigateToPage(event, "/com/example/carrentalsystem/login.fxml", "Login");
    }

    private void navigateToPage(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Pass state to the controller if returning
            if (fxmlPath.contains("rentcar.fxml")) {
                RentCarController controller = fxmlLoader.getController();
                controller.restoreState();
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to be called after successful payment
    public static void resetPaymentStatus() {
        paymentPending = false;
    }
}