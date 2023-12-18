module me.ero.storesimulationapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires eu.hansolo.tilesfx;
    requires com.google.gson;

    opens me.ero.storesimulationapp to javafx.fxml;
    exports me.ero.storesimulationapp;
}