
package com.example.carrentalsystem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignupController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField ageField;
    @FXML
    private ComboBox<String> genderComboBox;
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

    public SignupController() {
    }

    @FXML
    public void initialize() {
        this.genderComboBox.getItems().addAll(new String[]{"Male", "Female", "Transgender"});
        this.genderComboBox.setPromptText("Select Gender");
    }

    private void insertUser(String firstName, String lastName, int age, String gender, String dob, String username, String password) throws SQLException {
        String query = "INSERT INTO users (First_Name, Last_Name, Age, Gender, Date_Of_Birth, USERNAME, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = DatabaseConnector.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setInt(3, age);
            preparedStatement.setString(4, gender);
            preparedStatement.setString(5, dob);
            preparedStatement.setString(6, username);
            preparedStatement.setString(7, password);

            // Debugging: Print the query and parameters
            System.out.println("Executing query: " + query);
            System.out.println("Parameters: " + firstName + ", " + lastName + ", " + age + ", " + gender + ", " + dob + ", " + username + ", " + password);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted."); // Check if rows are being inserted
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage()); // Print SQL errors
            throw e; // Re-throw the exception to handle it in the calling method
        }
    }

    private boolean areInputsValid(String firstName, String lastName, String age, String gender, LocalDate dob, String username, String password, String confirmPassword) {
        if (!firstName.isEmpty() && !lastName.isEmpty() && !age.isEmpty() && gender != null && dob != null && !username.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                this.statusLabel.setText("Passwords do not match!");
                this.statusLabel.setStyle("-fx-text-fill: red;");
                return false;
            } else {
                return true;
            }
        } else {
            this.statusLabel.setText("Please fill out all fields.");
            this.statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();
        String age = this.ageField.getText();
        String gender = this.genderComboBox.getValue();
        LocalDate dob = this.dateOfBirthPicker.getValue();
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
        String confirmPassword = this.confirmPasswordField.getText();

        if (this.areInputsValid(firstName, lastName, age, gender, dob, username, password, confirmPassword)) {
            try {
                int ageInt = Integer.parseInt(age);
                this.insertUser(firstName, lastName, ageInt, gender, dob.toString(), username, password);
                this.statusLabel.setText("Signup successful! Redirecting to login...");
                this.statusLabel.setStyle("-fx-text-fill: green;");
                this.redirectToLoginPage(event);
            } catch (NumberFormatException e) {
                this.statusLabel.setText("Age must be a number.");
                this.statusLabel.setStyle("-fx-text-fill: red;");
            } catch (SQLException e) {
                this.statusLabel.setText("Error saving data: " + e.getMessage());
                this.statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace(); // Print the full stack trace for debugging
            } catch (IOException e) {
                this.statusLabel.setText("Error loading login page.");
                this.statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace(); // Print the full stack trace for debugging
            }
        }
    }

    public void handleBack(ActionEvent event) {
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
    private void redirectToLoginPage(ActionEvent event) throws IOException {
        Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("/com/example/carrentalsystem/login.fxml"));
        this.stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        this.scene = new Scene(root);
        this.stage.setWidth((double)1000.0F);
        this.stage.setHeight((double)800.0F);
        this.stage.setResizable(false);
        this.stage.setScene(this.scene);
        this.stage.show();
    }
}