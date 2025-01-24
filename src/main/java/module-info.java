module com.example.carrentalsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires java.desktop;
    requires de.jensd.fx.glyphs.fontawesome;



    opens com.example.carrentalsystem to javafx.fxml;
    exports com.example.carrentalsystem;
}