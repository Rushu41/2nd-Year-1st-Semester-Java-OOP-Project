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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActiveCustomerController {

    @FXML
    private TableView<ObservableList<Object>> customerDataTable;

    @FXML
    private TableColumn<ObservableList<Object>, String> usernameColumn;

    @FXML
    private TableColumn<ObservableList<Object>, String> carNameColumn;

    @FXML
    private TableColumn<ObservableList<Object>, String> rentingStatusColumn;

    @FXML
    private TableColumn<ObservableList<Object>, String> returnCarStatusColumn;

    @FXML
    private TableColumn<ObservableList<Object>, Double> billColumn;

    @FXML
    private TableColumn<ObservableList<Object>, Integer> totalRentingColumn;

    private ObservableList<ObservableList<Object>> customerDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get(0)));
        carNameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get(1)));
        rentingStatusColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get(2)));
        returnCarStatusColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get(3)));
        billColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get(4)));
        totalRentingColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get(5)));

        loadCustomerData();
    }

    private void loadCustomerData() {
        String query = """
            SELECT 
                r.customer_name AS customer_name,
                GROUP_CONCAT(DISTINCT COALESCE(r.car_name, rc.car_name)) AS car_names,
                CASE WHEN COUNT(rc.return_date) = 0 THEN 'Yes' ELSE 'No' END AS renting_status,
                CASE WHEN COUNT(rc.return_date) > 0 THEN 'Yes' ELSE 'No' END AS return_car_status,
                SUM(IFNULL(r.rental_days * c.price, 0)) AS bill,
                COALESCE(cm.total_renting, 0) AS total_renting
            FROM rentals r
            LEFT JOIN return_car rc 
                ON r.car_name = rc.car_name AND r.customer_name = rc.customer_name
            LEFT JOIN cars c ON r.car_name = c.name
            LEFT JOIN customer cm ON cm.username = r.customer_name
            GROUP BY r.customer_name;
            """;

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Clear existing data
            customerDataList.clear();

            // Fetch and add data
            while (resultSet.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                row.add(resultSet.getString("customer_name")); // Customer name
                row.add(resultSet.getString("car_names")); // Aggregated car names
                row.add(resultSet.getString("renting_status")); // Renting status
                row.add(resultSet.getString("return_car_status")); // Return car status
                row.add(resultSet.getDouble("bill")); // Total bill
                row.add(resultSet.getInt("total_renting")); // Total renting count
                customerDataList.add(row);
            }

            // Bind data to TableView
            customerDataTable.setItems(customerDataList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customer data.");
            e.printStackTrace();
        }
    }
    private void updateOrInsertCustomer(String customerName) {
        String selectQuery = "SELECT total_renting FROM customer WHERE username = ?";
        String updateQuery = "UPDATE customer SET total_renting = total_renting + 1 WHERE username = ?";
        String insertQuery = "INSERT INTO customer (username, total_renting) VALUES (?, 1)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            selectStatement.setString(1, customerName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                updateStatement.setString(1, customerName);
                updateStatement.executeUpdate();
            } else {
                insertStatement.setString(1, customerName);
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update or insert customer data.");
            e.printStackTrace();
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
