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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class PaymentController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    private String paymentMethod = "";
    private String cardNumber = "";
    private String expiryDate = "";
    private String cvv = "";
    private String bkashPhone = "";
    private String bkashPin = "";
    private String nagadPhone = "";
    private String nagadPin = "";

    @FXML
    public void initialize() {
        // Set the username from UserSession and make the field non-editable

        usernameField.setText(UserSession.getLoggedInUsername());
        usernameField.setEditable(false);
    }
    @FXML
    private Button saveButton;


    private RentCarController rentCarController;

    public void setRentCarController(RentCarController rentCarController) {
        this.rentCarController = rentCarController;
    }


    @FXML
    private void handleSavePayment(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (insertPaymentDetails(username, email)) {
            try {
                // Load the RentCar FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/rentCar.fxml"));
                Parent rentCarPage = loader.load();

                // Get the controller and set necessary data
                RentCarController rentCarController = loader.getController();

                // Set the username
                rentCarController.setUsername(username);

                // Disable logout button and enable generate receipt button
                rentCarController.disableLogoutButton();
//                rentCarController.enableGenerateReceiptButton();

                // Switch to the RentCar scene
                Scene rentCarScene = new Scene(rentCarPage);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(rentCarScene);
                stage.setTitle("Rent Car");
                stage.show();

                // Show success message
                showSuccessAlert("Payment Successful", "Your payment has been processed successfully.");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to return to rent car page.");
            }
        } else {
            showAlert("Error", "Failed to save payment details.");
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCreditCardPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        paymentMethod = "Credit Card";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/CreditCardDetails.fxml"));
            Parent creditCardPage = loader.load();

            CreditCardDetailsController creditCardController = loader.getController();
            creditCardController.setPaymentController(this);

            // Pass the username and email to the CreditCardDetailsController
            creditCardController.setUserDetails(usernameField.getText(), emailField.getText());

            Scene creditCardScene = new Scene(creditCardPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(creditCardScene);
            stage.setTitle("Credit Card Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBkashPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        paymentMethod = "Bkash";
        showQRCodePopup("Bkash QR Code", "/com/example/carrentalsystem/images/bkash_qr.jpg");
    }

    @FXML
    private void handleNagadPayment(ActionEvent event) {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return;
        }
        paymentMethod = "Nagad";
        showQRCodePopup("Nagad QR Code", "/com/example/carrentalsystem/images/nagad_qr.png");
    }

    private void showQRCodePopup(String title, String qrCodeImagePath) {
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
                System.out.println("Payment successful via " + paymentMethod);
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleGenerateReceipt(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (insertPaymentDetails(username, email)) {
            String receiptContent = String.format(
                    "Receipt:\nUsername: %s\nEmail: %s\nPayment Method: %s\nTotal Cost: $80\nPayment Successful!",
                    username, email, paymentMethod
            );

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/ReceiptView.fxml"));
                Parent receiptPage = loader.load();

                ReceiptViewController receiptController = loader.getController();
                receiptController.setReceiptContent(receiptContent);

                Scene receiptScene = new Scene(receiptPage);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(receiptScene);
                stage.setTitle("Receipt");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Failed to save payment details.");
        }
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


    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/subscription.fxml")); // Replace with your main screen FXML
            Parent mainScreen = loader.load();

            Scene mainScene = new Scene(mainScreen);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(mainScene);
            stage.setTitle("Main Screen");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean insertPaymentDetails(String username, String email) {
        String url = "jdbc:mysql://localhost:3306/car_rental_system";
        String user = "root";
        String password = "12212108";

        String query = "INSERT INTO payments (username, email, payment_method, card_number, expiry_date, cvv, bkash_phone,nagad_phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, paymentMethod);
            statement.setString(4, cardNumber);
            statement.setString(5, expiryDate);
            statement.setString(6, cvv);
            statement.setString(7, bkashPhone);

            statement.setString(8, nagadPhone);


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

    public void setCreditCardDetails(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        System.out.println("Credit Card payment processed.");
    }

}