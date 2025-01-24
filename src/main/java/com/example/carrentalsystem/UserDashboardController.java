package com.example.carrentalsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;


import java.io.IOException;

public class UserDashboardController {



    public void handleProfile(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/profile.fxml", "Profile");
    }

    public void handleRentCar(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/rentCar.fxml", "Rent Car");
    }

    public void handleReturnCar(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/returnCar.fxml", "Return Car");
    }

    public void handleSubscription(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/subscription.fxml", "Subscription");
    }

    public void handleFeedback(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/feedback.fxml", "Feedback");
    }

    public void handleMyBookings(ActionEvent event) {
        navigateToPage(event, "/com/example/carrentalsystem/myBookings.fxml", "My Bookings");
    }

    private void navigateToPage(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
