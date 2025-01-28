package com.example.carrentalsystem;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.FileWriter;
import java.io.IOException;

public class RentCarReceiptViewController {
    @FXML
    private TextArea rentCarReceiptTextArea;

    // Method to set receipt content
    public void setRentCarReceiptContent(String rentCarReceiptContent) {
        rentCarReceiptTextArea.setText(rentCarReceiptContent);
    }

    @FXML
    public void handleDownloadRentCarReceipt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("RentCarReceipt.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        var file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(rentCarReceiptTextArea.getText());
                showAlert("Success", "Rent Car Receipt downloaded successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to download the rent car receipt.");
            }
        }
    }

    @FXML
    public void handleBackButton() {
        try {
            // Load the previous scene (e.g., main menu or previous screen)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userDashboard.fxml")); // Adjust the FXML path as necessary
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) rentCarReceiptTextArea.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to go back to the previous screen.");
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}