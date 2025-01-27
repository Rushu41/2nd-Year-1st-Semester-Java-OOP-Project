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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminFeedbackController {

    @FXML
    private TableView<Feedback> feedbackTable;

    @FXML
    private TableColumn<Feedback, String> usernameColumn;

    @FXML
    private TableColumn<Feedback, String> feedbackColumn;

    @FXML
    private TableColumn<Feedback, String> replyColumn;

    @FXML
    private TextField replyField;

    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        feedbackColumn.setCellValueFactory(new PropertyValueFactory<>("feedback"));
        replyColumn.setCellValueFactory(new PropertyValueFactory<>("reply"));

        loadFeedback();
    }

    private void loadFeedback() {
        String query = "SELECT username, feedback, admin_reply FROM feedback";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            feedbackList.clear();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String feedback = resultSet.getString("feedback");
                String reply = resultSet.getString("admin_reply");
                feedbackList.add(new Feedback(username, feedback, reply));
            }
            feedbackTable.setItems(feedbackList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load feedback.");
        }
    }

    @FXML
    private void handleReply() {
        Feedback selectedFeedback = feedbackTable.getSelectionModel().getSelectedItem();
        if (selectedFeedback == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a feedback to reply.");
            return;
        }

        String reply = replyField.getText().trim();
        if (reply.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Reply", "Please enter a reply.");
            return;
        }

        String query = "UPDATE feedback SET admin_reply = ? WHERE username = ? AND feedback = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, reply);
            statement.setString(2, selectedFeedback.getUsername());
            statement.setString(3, selectedFeedback.getFeedback());

            statement.executeUpdate();

            selectedFeedback.setReply(reply); // Update the table
            feedbackTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Reply Sent", "Your reply has been sent.");
            replyField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not send reply.");
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