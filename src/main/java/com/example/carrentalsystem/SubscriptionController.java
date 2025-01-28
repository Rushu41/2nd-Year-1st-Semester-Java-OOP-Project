package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SubscriptionController {

    public void handleSubscribe(ActionEvent event) {
        try {
            // Get the logged-in username from the UserSession
            String username = UserSession.getLoggedInUsername();

            // Update the subscription status in the database
            if (updateSubscriptionStatus(username, 1)) {
                // Load the payment.fxml file
                Parent paymentPage = FXMLLoader.load(getClass().getResource("/com/example/carrentalsystem/payment.fxml"));
                Scene paymentScene = new Scene(paymentPage);

                // Get the current stage
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Set the payment scene to the stage
                stage.setScene(paymentScene);
                stage.setTitle("Payment");
                stage.show();
            } else {
                // Show an error message if the update fails
                showAlert("Subscription Error", "Failed to update subscription status.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updateSubscriptionStatus(String username, int isSubscribed) {
        String query = "UPDATE users SET is_subscribed = ? WHERE username = ?";
        try (Connection connection = DatabaseConnector.connect();
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
