package com.example.carrentalsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActiveCustomerController {

    @FXML
    private TableView<Rental> rentalsTable;

    @FXML
    private TableColumn<Rental, Integer> idColumn;

    @FXML
    private TableColumn<Rental, String> carNameColumn;

    @FXML
    private TableColumn<Rental, String> customerNameColumn;

    @FXML
    private TableColumn<Rental, String> startDateColumn;

    @FXML
    private TableColumn<Rental, String> endDateColumn;

    @FXML
    private TableColumn<Rental, Long> rentalDaysColumn;

    private ObservableList<Rental> rentalsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Map TableColumn with Rental properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        carNameColumn.setCellValueFactory(new PropertyValueFactory<>("carName"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        rentalDaysColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDays"));

        // Load data from the rentals table
        loadRentals();
    }

    private void loadRentals() {
        String query = "SELECT * FROM rentals";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String carName = resultSet.getString("car_name");
                String customerName = resultSet.getString("customer_name");
                String startDate = resultSet.getString("start_date");
                String endDate = resultSet.getString("end_date");
                long rentalDays = resultSet.getLong("rental_days");

                rentalsList.add(new Rental(id, carName, customerName, startDate, endDate, rentalDays));
            }

            rentalsTable.setItems(rentalsList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rental data.");
        }
    }
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/dashboard.fxml", "Dashboard");
    }

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
