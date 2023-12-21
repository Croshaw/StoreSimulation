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
    opens me.ero.storesimulationapp.simulation to com.google.gson;
    opens me.ero.storesimulationapp.simulation.store_api to com.google.gson;
    opens me.ero.storesimulationapp.simulation.store_api.store to com.google.gson;
    opens me.ero.storesimulationapp.simulation.store_api.human to com.google.gson;
    opens me.ero.storesimulationapp.simulation.store_api.util to com.google.gson;
    exports me.ero.storesimulationapp;
}