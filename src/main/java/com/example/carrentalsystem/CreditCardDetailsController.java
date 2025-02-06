package com.example.carrentalsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreditCardDetailsController {

    @FXML
    private TextField cardNumberField;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private TextField cvvField;

    private PaymentController paymentController;
    private String username;
    private String email;

    public void setPaymentController(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    public void setUserDetails(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String cardNumber = cardNumberField.getText();
        LocalDate expiryDate = expiryDatePicker.getValue();
        String cvv = cvvField.getText();

        if (cardNumber.isEmpty() || expiryDate == null || cvv.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        String formattedExpiryDate = expiryDate.format(formatter);

        if (paymentController != null) {
            paymentController.setCreditCardDetails(cardNumber, formattedExpiryDate, cvv);
        }
        String url = "jdbc:mysql://localhost:3306/car_rental_system";
        String user = "root";
        String password = "12212108";

        String query = "INSERT INTO payments (username, email, payment_method) " +
                "VALUES (?, ?, ?)";
        String paymentMethod="Credit Card";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, UserSession.getLoggedInUsername());
            statement.setString(2, email);
            statement.setString(3, paymentMethod);

            int rowsInserted = statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();

        }

        // Update the subscription status to 1 (subscribed)
        if (updateSubscriptionStatus(username, 1)) {
            // Generate receipt content with username and email
            String receiptContent = generateReceiptContent(cardNumber, formattedExpiryDate, cvv, username, email);

            // Navigate to the Receipt View
            navigateToReceiptView(event, receiptContent);
        } else {
            showAlert("Error", "Failed to update subscription status.");
        }
    }

    @FXML
    private void handleBackToPayment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/payment.fxml"));
            Parent paymentPage = loader.load();

            Scene paymentScene = new Scene(paymentPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(paymentScene);
            stage.setTitle("Payment Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToReceiptView(ActionEvent event, String receiptContent) {
        try {
            // Load the Receipt View FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/receiptview.fxml"));
            Parent receiptPage = loader.load();

            // Get the ReceiptViewController and set the receipt content
            ReceiptViewController receiptViewController = loader.getController();
            receiptViewController.setReceiptContent(receiptContent);

            // Switch to the Receipt View Scene
            Scene receiptScene = new Scene(receiptPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(receiptScene);
            stage.setTitle("Rental Receipt");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the receipt view.");
        }
    }

    private String generateReceiptContent(String cardNumber, String expiryDate, String cvv, String username, String email) {
        return "Rental Receipt\n\n" +
                "User Details:\n" +
                "Username: " + username + "\n" +
                "Email: " + email + "\n\n" +
                "Payment Method: Credit card\n" +

                "Thank you for your rental!";
    }

    private boolean updateSubscriptionStatus(String username, int isSubscribed) {
        String query = "UPDATE users SET is_subscribed = ? WHERE username = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_system", "root", "12212108");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, isSubscribed);
            statement.setString(2, username);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}