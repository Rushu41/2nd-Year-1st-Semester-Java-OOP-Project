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

public class CreditCardDetailsController {

    @FXML
    private TextField cardNumberField;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private TextField cvvField;

    private PaymentController paymentController;

    public void setPaymentController(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @FXML
    private void handleSubmit() {
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

        Stage stage = (Stage) cardNumberField.getScene().getWindow();
        stage.close();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
