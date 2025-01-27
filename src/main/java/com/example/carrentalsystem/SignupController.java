package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class SignupController {

    @FXML
    private TextField userIdField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> genderComboBox; // Changed to ComboBox

    @FXML
    private DatePicker dateOfBirthPicker;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button signupButton;

    private Stage stage;
    private Scene scene;

    @FXML
    public void initialize() {
        // Generate random 5-digit user ID
        String userId = generateUserId();
        userIdField.setText(userId);
        userIdField.setEditable(false); // Prevent user from editing the user ID

        // Populate the gender combo box
        genderComboBox.getItems().addAll("Male", "Female", "Transgender");
        genderComboBox.setPromptText("Select Gender");
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        String userId = userIdField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String age = ageField.getText();
        String gender = genderComboBox.getValue(); // Get selected gender
        LocalDate dob = dateOfBirthPicker.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!areInputsValid(userId, firstName, lastName, age, gender, dob, username, password, confirmPassword)) {
            return;
        }

        try {
            int ageInt = Integer.parseInt(age);

            // Insert user into the database
            insertUser(userId, firstName, lastName, ageInt, gender, dob.toString(), username, password);

            statusLabel.setText("Signup successful! Redirecting to login...");
            statusLabel.setStyle("-fx-text-fill: green;");

            redirectToLoginPage(event);

        } catch (NumberFormatException e) {
            statusLabel.setText("Age must be a number.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            statusLabel.setText("Error saving data: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (IOException e) {
            statusLabel.setText("Error loading login page.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private boolean areInputsValid(String userId, String firstName, String lastName, String age, String gender, LocalDate dob, String username, String password, String confirmPassword) {
        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || gender == null ||
                dob == null || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill out all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }

        return true;
    }

    private void insertUser(String userId, String firstName, String lastName, int age, String gender, String dob, String username, String password) throws SQLException {
        String query = "INSERT INTO users (user_id, First_Name, Last_Name, Age, Gender, Date_Of_Birth, USERNAME, Password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setInt(4, age);
            preparedStatement.setString(5, gender);
            preparedStatement.setString(6, dob);
            preparedStatement.setString(7, username);
            preparedStatement.setString(8, password);
            preparedStatement.executeUpdate();
        }
    }

    private void redirectToLoginPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrentalsystem/login.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private String generateUserId() {
        int randomId = 10000 + (int) (Math.random() * 90000);
        return String.valueOf(randomId);
    }
}
