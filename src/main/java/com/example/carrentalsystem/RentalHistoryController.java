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

public class RentalHistoryController {

    @FXML
    private TableView<RentalHistory> rentalHistoryTable;

    @FXML
    private TableColumn<RentalHistory, String> carNameColumn;

    @FXML
    private TableColumn<RentalHistory, String> carTypeColumn;

    @FXML
    private TableColumn<RentalHistory, String> customerNameColumn;

    @FXML
    private TableColumn<RentalHistory, String> returnDateColumn;



    private ObservableList<RentalHistory> rentalHistoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        carNameColumn.setCellValueFactory(new PropertyValueFactory<>("carName"));
        carTypeColumn.setCellValueFactory(new PropertyValueFactory<>("carType"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));

        loadRentalHistoryFromDatabase();
    }

    private void loadRentalHistoryFromDatabase() {
        String query = "SELECT car_name, car_type, customer_name, return_date FROM return_car";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String carName = resultSet.getString("car_name");
                String carType = resultSet.getString("car_type");
                String customerName = resultSet.getString("customer_name");
                String returnDate = resultSet.getString("return_date");

                rentalHistoryList.add(new RentalHistory(carName, carType, customerName, returnDate));
            }

            rentalHistoryTable.setItems(rentalHistoryList);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load rental history.");
            e.printStackTrace();
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
