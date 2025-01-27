package com.example.carrentalsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure the FXML file is located in src/main/resources/login.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalsystem/login.fxml"));

        if (fxmlLoader == null) {
            throw new IOException("FXML file not found. Ensure 'login.fxml' is in the 'resources' folder.");
        }

        // Load the FXML file and set up the scene
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Car Rental System");
        stage.setScene(scene);
        // Set the stage size and prevent resizing
        stage.setWidth(800); // Your fixed width
        stage.setHeight(600); // Your fixed height
        stage.setResizable(false); // Disable resizing

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
