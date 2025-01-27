package com.example.carrentalsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AvailableCarsController {

    @FXML
    private TableView<Car> carsTable;

    @FXML
    private TableColumn<Car, String> carNameColumn;

    @FXML
    private TableColumn<Car, Integer> carSeatsColumn;

    @FXML
    private TableColumn<Car, String> carTypeColumn;

    @FXML
    private TableColumn<Car, Double> priceColumn;

    private ObservableList<Car> carList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind TableColumns to Car properties
        carNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        carTypeColumn.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
//        carSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("rentPricePerDay"));

        loadCarsFromDatabase();  // Load car data from the database
    }

    public void loadCarsFromDatabase() {
        carList.clear();  // Clear the current list of cars
        String query = "SELECT name, fuel_type, total_seats, rent_price_per_day FROM cars WHERE available = 1";
        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String fuelType = resultSet.getString("fuel_type");
                int seats = resultSet.getInt("total_seats");
                double rentPricePerDay = resultSet.getDouble("rent_price_per_day");

                // Add a new Car object to the list
                carList.add(new Car(name, seats, fuelType, rentPricePerDay));
            }

            carsTable.setItems(carList);  // Set the list to the TableView

        } catch (SQLException e) {
            e.printStackTrace();
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
            stage.setWidth(800); // Your fixed width
            stage.setHeight(600); // Your fixed height
            stage.setResizable(false); // Disable resizing

            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
