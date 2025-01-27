package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInputField;

    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = UserSession.getLoggedInUsername();
    }

    @FXML
    public void initialize() {
        loggedInUsername = UserSession.getLoggedInUsername();
        System.out.println("Debug: FeedbackController initialized with loggedInUsername = " + loggedInUsername);

        // Load previous feedback messages
        loadPreviousFeedback();

        // Initial chatbot greeting
        chatArea.appendText("Chatbot: Hi! How can I help you today?\n");
    }

    @FXML
    public void handleSend(ActionEvent event) {
        String userMessage = userInputField.getText().trim();
        if (!userMessage.isEmpty()) {
            // Display user message in chat
            chatArea.appendText("You: " + userMessage + "\n");

            // Save feedback to database
            saveFeedback(loggedInUsername, userMessage);

            // Respond with a chatbot message
            chatArea.appendText("Chatbot: Thank you for your feedback! Our admin will respond soon.\n");
        }
        userInputField.clear(); // Clear input field
    }

    private void saveFeedback(String username, String feedback) {
        // Ensure the username is valid
        username = UserSession.getLoggedInUsername();
        System.out.println("Debug: loggedInUsername is: " + username); // Debugging statement

        if (username == null || username.isEmpty()) {
            chatArea.appendText("Chatbot: Error: User is not logged in.\n");
            return;
        }

        // Query to check if feedback already exists for the user
        String checkQuery = "SELECT feedback FROM feedback WHERE username = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {

            checkStmt.setString(1, username);
            ResultSet resultSet = checkStmt.executeQuery();

            // If feedback already exists, update it
            if (resultSet.next()) {
                String existingFeedback = resultSet.getString("feedback");
                String updatedFeedback = existingFeedback + "\n" + feedback;

                // Update the existing feedback with the new message
                String updateQuery = "UPDATE feedback SET feedback = ? WHERE username = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, updatedFeedback);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                }
            } else {
                // If no feedback exists, insert a new entry
                String insertQuery = "INSERT INTO feedback (username, feedback) VALUES (?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, feedback);
                    insertStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            chatArea.appendText("Chatbot: Error saving feedback. Please try again later.\n");
            e.printStackTrace();
        }
    }


    private void loadPreviousFeedback() {
        String query = "SELECT feedback, admin_reply FROM feedback WHERE username = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, loggedInUsername);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String feedback = resultSet.getString("feedback");
                String adminReply = resultSet.getString("admin_reply");

                chatArea.appendText("You: " + feedback + "\n");
                if (adminReply != null) {
                    chatArea.appendText("Admin: " + adminReply + "\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            chatArea.appendText("Chatbot: Error loading previous feedback.\n");
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

}