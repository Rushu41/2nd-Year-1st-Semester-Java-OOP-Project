package com.example.carrentalsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyBookingsController {

    @FXML
    private TableView<Rental> rentalTable; // TableView to display rental data
    @FXML
    private TableColumn<Rental, String> carNameColumn; // Column for car names
    @FXML
    private TableColumn<Rental, String> customerNameColumn; // Column for customer names
    @FXML
    private TableColumn<Rental, String> startDateColumn; // Column for start dates
    @FXML
    private TableColumn<Rental, String> endDateColumn; // Column for end dates
    @FXML
    private TableColumn<Rental, Integer> rentalDaysColumn; // Column for rental days

    private ObservableList<Rental> rentalList = FXCollections.observableArrayList(); // List to hold rental data

    public void initialize() {
        // Set up the cell value factories for each column
        carNameColumn.setCellValueFactory(new PropertyValueFactory<>("carName"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        rentalDaysColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDays"));


    }

    public void loadUserBookings(String username) {
        username=UserSession.getLoggedInUsername();
        String query = "SELECT * FROM rentals WHERE customer_name = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Rental rental = new Rental(
                        resultSet.getInt("id"),
                        resultSet.getString("car_name"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("start_date"),
                        resultSet.getString("end_date"),
                        resultSet.getInt("rental_days")
                );
                rentalList.add(rental); // Add rental to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        rentalTable.setItems(rentalList); // Set the items in the TableView
    }
    @FXML
    public void handleBack(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/userDashboard.fxml", "Dashboard");
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


}