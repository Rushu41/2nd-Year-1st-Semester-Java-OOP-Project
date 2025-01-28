package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class RentCarPaymentController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    private String rentCarPaymentMethod = "";
    private String rentCarCardNumber = "";
    private String rentCarExpiryDate = "";
    private String rentCarCvv = "";
    private String rentCarBkashPhone = "";
    private String rentCarBkashPin = "";
    private String rentCarNagadPhone = "";
    private String rentCarNagadPin = "";

    @FXML
    public void initialize() {
        // Set the username from UserSession and make the field non-editable
        usernameField.setText(UserSession.getLoggedInUsername());
        usernameField.setEditable(false);
    }

    @FXML
    private void handleRentCarCreditCardPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        rentCarPaymentMethod = "Credit Card";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCarCreditCardDetails.fxml"));
            Parent rentCarCreditCardPage = loader.load();

            RentCarCreditCardDetailsController rentCarCreditCardController = loader.getController();
            rentCarCreditCardController.setRentCarPaymentController(this);

            // Pass the username and email to the RentCarCreditCardDetailsController
            rentCarCreditCardController.setRentCarUserDetails(usernameField.getText(), emailField.getText());

            Scene rentCarCreditCardScene = new Scene(rentCarCreditCardPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(rentCarCreditCardScene);
            stage.setTitle("Rent Car Credit Card Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRentCarBkashPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        rentCarPaymentMethod = "Bkash";
        showRentCarQRCodePopup("Bkash QR Code", "/com/example/carrentalsystem/images/bkash_qr.jpg");
    }

    @FXML
    private void handleRentCarNagadPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        rentCarPaymentMethod = "Nagad";
        showRentCarQRCodePopup("Nagad QR Code", "/com/example/carrentalsystem/images/nagad_qr.png");
    }

    private void showRentCarQRCodePopup(String title, String qrCodeImagePath) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Scan the QR code to complete your payment.");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, closeButton);

        ImageView qrCodeImageView = new ImageView(new Image(getClass().getResourceAsStream(qrCodeImagePath)));
        qrCodeImageView.setFitHeight(300);
        qrCodeImageView.setFitWidth(300);
        qrCodeImageView.setPreserveRatio(true);

        VBox vbox = new VBox(qrCodeImageView);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(0));
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                System.out.println("Payment successful via " + rentCarPaymentMethod);
            }
            return null;
        });

        dialog.showAndWait();
    }
    @FXML
    private void handleRentCarGenerateReceipt(ActionEvent event) {
        String rentCarUsername = usernameField.getText();
        String rentCarEmail = emailField.getText();

        if (rentCarUsername.isEmpty() || rentCarEmail.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (insertRentCarPaymentDetails(rentCarUsername, rentCarEmail)) {
            String rentCarReceiptContent = String.format(
                    "Receipt:\nUsername: %s\nEmail: %s\nPayment Method: %s\nTotal Cost: $80\nPayment Successful!",
                    rentCarUsername, rentCarEmail, rentCarPaymentMethod
            );

            try {
                URL fxmlLocation = getClass().getResource("/com/example/carrentalsystem/rentCarReceiptView.fxml");
                if (fxmlLocation == null) {
                    throw new FileNotFoundException("FXML file not found");
                }
                System.out.println("FXML Location: " + fxmlLocation);
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent rentCarReceiptPage = loader.load();

                RentCarReceiptViewController rentCarReceiptController = loader.getController();
                rentCarReceiptController.setRentCarReceiptContent(rentCarReceiptContent);

                Scene rentCarReceiptScene = new Scene(rentCarReceiptPage);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(rentCarReceiptScene);
                stage.setTitle("Rent Car Receipt");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Failed to save payment details.");
        }
    }
    @FXML
    private void handleRentCarBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCar.fxml")); // Replace with your main screen FXML
            Parent rentCarMainScreen = loader.load();

            Scene rentCarMainScene = new Scene(rentCarMainScreen);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(rentCarMainScene);
            stage.setTitle("Rent Car Main Screen");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean insertRentCarPaymentDetails(String rentCarUsername, String rentCarEmail) {
        String url = "jdbc:mysql://localhost:3306/car_rental_system";
        String user = "root";
        String password = "12212108";

        String query = "INSERT INTO payments (username, email, payment_method, card_number, expiry_date, cvv, bkash_phone, nagad_phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, rentCarUsername);
            statement.setString(2, rentCarEmail);
            statement.setString(3, rentCarPaymentMethod);
            statement.setString(4, rentCarCardNumber);
            statement.setString(5, rentCarExpiryDate);
            statement.setString(6, rentCarCvv);
            statement.setString(7, rentCarBkashPhone);
            statement.setString(8, rentCarNagadPhone);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setRentCarCreditCardDetails(String rentCarCardNumber, String rentCarExpiryDate, String rentCarCvv) {
        this.rentCarCardNumber = rentCarCardNumber;
        this.rentCarExpiryDate = rentCarExpiryDate;
        this.rentCarCvv = rentCarCvv;
        System.out.println("Rent Car Credit Card payment processed.");
    }
}