package com.example.carrentalsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnCarController {

    @FXML
    private TableView<Car> rentedCarsTable;

    @FXML
    private TableColumn<Car, String> carNameColumn;

    @FXML
    private TableColumn<Car, String> carTypeColumn;

    @FXML
    private TableColumn<Car, Double> priceColumn;

    @FXML
    private TableColumn<Car, String> customerNameColumn;

    @FXML
    private Button returnCarButton;

    private ObservableList<Car> rentedCarList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        carNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        carTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        loadRentedCarsFromDatabase();
    }

    private void loadRentedCarsFromDatabase() {
        String query = "SELECT r.car_name, c.type AS car_type, c.price AS car_price, r.customer_name " +
                "FROM rentals r " +
                "INNER JOIN cars c ON r.car_name = c.name";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String carName = resultSet.getString("car_name");
                String carType = resultSet.getString("car_type");
                double carPrice = resultSet.getDouble("car_price");
                String customerName = resultSet.getString("customer_name");

                rentedCarList.add(new Car(carName, carType, carPrice, customerName));
            }

            rentedCarsTable.setItems(rentedCarList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rented cars.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReturnCar(ActionEvent event) {
        Car selectedCar = rentedCarsTable.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            showAlert(Alert.AlertType.WARNING, "No Car Selected", "Please select a car to return.");
            return;
        }

        try (Connection connection = DatabaseConnector.connect()) {
            connection.setAutoCommit(false);

            // Retrieve the customer's name from the rentals table
            String customerName = selectedCar.getCustomerName();

            if (customerName == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to find the customer associated with the car.");
                connection.rollback();
                return;
            }

            // Remove the car from the rentals table
            String deleteRentalQuery = "DELETE FROM rentals WHERE car_name = ?";
            try (PreparedStatement deleteRentalStmt = connection.prepareStatement(deleteRentalQuery)) {
                deleteRentalStmt.setString(1, selectedCar.getName());
                deleteRentalStmt.executeUpdate();
            }

            // Update the car availability in the cars table
            String updateCarQuery = "UPDATE cars SET available = 1 WHERE name = ?";
            try (PreparedStatement updateCarStmt = connection.prepareStatement(updateCarQuery)) {
                updateCarStmt.setString(1, selectedCar.getName());
                updateCarStmt.executeUpdate();
            }

            // Insert the returned car details into the return_cars table
            String insertReturnCarQuery = "INSERT INTO return_car (car_name, car_type, price, customer_name, return_date) VALUES (?, ?, ?, ?, CURRENT_DATE)";
            try (PreparedStatement insertReturnCarStmt = connection.prepareStatement(insertReturnCarQuery)) {
                insertReturnCarStmt.setString(1, selectedCar.getName());
                insertReturnCarStmt.setString(2, selectedCar.getType());
                insertReturnCarStmt.setDouble(3, selectedCar.getPrice());
                insertReturnCarStmt.setString(4, customerName);
                insertReturnCarStmt.executeUpdate();
            }

            connection.commit();
            rentedCarList.remove(selectedCar);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Car returned successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to return the car. Please try again.");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/login.fxml", "Login");
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
