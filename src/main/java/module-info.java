module com.example.carrentalsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;


    opens com.example.carrentalsystem to javafx.fxml;
    exports com.example.carrentalsystem;
}