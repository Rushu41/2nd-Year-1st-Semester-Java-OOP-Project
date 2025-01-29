package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    public Label availableCarsLabel;
    public Label activeRentalsLabel;
    public Label totalCustomersLabel;
    public Label totalIncomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        try (Connection connection = DatabaseConnector.connect()) {
            availableCarsLabel.setText(String.valueOf(getAvailableCars(connection)));
            activeRentalsLabel.setText(String.valueOf(getActiveRentals(connection)));
            totalCustomersLabel.setText(String.valueOf(getTotalCustomers(connection)));
            totalIncomeLabel.setText("$" + getTotalIncome(connection));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getAvailableCars(Connection connection) throws SQLException {
        String query = "SELECT COUNT(name) FROM cars WHERE available = 1";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private int getActiveRentals(Connection connection) throws SQLException {
        String query = "SELECT COUNT(car_name) FROM rentals";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private int getTotalCustomers(Connection connection) throws SQLException {
        String query = "SELECT COUNT(car_name) FROM return_car";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private double getTotalIncome(Connection connection) throws SQLException {
        String query = "SELECT " +
                "(SELECT COALESCE(SUM(price), 0) FROM return_car) + " +  // Income from car returns
                "(SELECT COALESCE(COUNT(*), 0) * 80 FROM users WHERE is_subscribed = 1) " +  // Subscription income
                "AS total_income";  // Combined total alias

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble("total_income") : 0.0;
        }
    }

    public void handleAvailableCars(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/availableCars.fxml", "Available Cars");
    }

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
            stage.setWidth(1000);
            stage.setHeight(800);
            stage.setResizable(false);
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
