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

public class RentCarCreditCardDetailsController {

    @FXML
    private TextField rentCarCardNumberField;

    @FXML
    private DatePicker rentCarExpiryDatePicker;

    @FXML
    private TextField rentCarCvvField;

    private RentCarPaymentController rentCarPaymentController;
    private String rentCarUsername;
    private String rentCarEmail;

    public void setRentCarPaymentController(RentCarPaymentController rentCarPaymentController) {
        this.rentCarPaymentController = rentCarPaymentController;
    }

    public void setRentCarUserDetails(String rentCarUsername, String rentCarEmail) {
        this.rentCarUsername = rentCarUsername;
        this.rentCarEmail = rentCarEmail;
    }

    @FXML
    private void handleRentCarSubmit(ActionEvent event) {
        String rentCarCardNumber = rentCarCardNumberField.getText();
        LocalDate rentCarExpiryDate = rentCarExpiryDatePicker.getValue();
        String rentCarCvv = rentCarCvvField.getText();

        if (rentCarCardNumber.isEmpty() || rentCarExpiryDate == null || rentCarCvv.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        String formattedRentCarExpiryDate = rentCarExpiryDate.format(formatter);

        if (rentCarPaymentController != null) {
            rentCarPaymentController.setRentCarCreditCardDetails(rentCarCardNumber, formattedRentCarExpiryDate, rentCarCvv);
        }

        // Navigate back to the Payment FXML view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCarPayment.fxml"));
            Parent rentCarPaymentPage = loader.load();

            Scene rentCarPaymentScene = new Scene(rentCarPaymentPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(rentCarPaymentScene);
            stage.setTitle("Rent Car Payment Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the payment details view.");
        }
    }

    @FXML
    private void handleRentCarBackToPayment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCarPayment.fxml"));
            Parent rentCarPaymentPage = loader.load();

            Scene rentCarPaymentScene = new Scene(rentCarPaymentPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(rentCarPaymentScene);
            stage.setTitle("Rent Car Payment Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToRentCarReceiptView(ActionEvent event, String rentCarReceiptContent) {
        try {
            // Load the Receipt View FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCarReceiptView.fxml"));
            Parent rentCarReceiptPage = loader.load();

            // Get the ReceiptViewController and set the receipt content
            RentCarReceiptViewController rentCarReceiptViewController = loader.getController();
            rentCarReceiptViewController.setRentCarReceiptContent(rentCarReceiptContent);

            // Switch to the Receipt View Scene
            Scene rentCarReceiptScene = new Scene(rentCarReceiptPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(rentCarReceiptScene);
            stage.setTitle("Rent Car Rental Receipt");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the rent car receipt view.");
        }
    }

    private String generateRentCarReceiptContent(String rentCarCardNumber, String rentCarExpiryDate, String rentCarCvv, String rentCarUsername, String rentCarEmail) {
        return "Rent Car Rental Receipt\n\n" +
                "User Details:\n" +
                "Username: " + rentCarUsername + "\n" +
                "Email: " + rentCarEmail + "\n\n" +
                "Payment Details:\n" +
                "Card Number: " + rentCarCardNumber + "\n" +
                "Expiry Date: " + rentCarExpiryDate + "\n" +
                "CVV: " + rentCarCvv + "\n\n" +
                "Thank you for your rental!";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}