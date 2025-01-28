package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    public void handleAvailableCars(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/availableCars.fxml", "Available Cars");
    }

//    public void handleRentCar(ActionEvent event) {
//        navigateToPage(event, "/com/example/carrentalsystem/rentCar.fxml", "Rent Car");
//    }

    public void handleAddCar(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/addCar.fxml", "Add Car");
    }


    public void handleActiveCustomer(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/ActiveCustomer.fxml", "Active Customers");
    }

    public void handleLogout(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/login.fxml", "Login");
    }

    public void handleReturnCar(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/returnCar.fxml", "Return Car");
    }


    public void handleFeedback(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/adminFeedback.fxml", "Admin Feedback");
    }

    public void handleRentalHistory(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/rentalHistory.fxml", "Rental History");
    }

    private void navigateToPage(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene newScene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.setTitle(title);
            stage.setWidth(1000); // Your fixed width
            stage.setHeight(800); // Your fixed height
            stage.setResizable(false); // Disable resizing

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load " + title + " page.");
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
