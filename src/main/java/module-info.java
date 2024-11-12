module org.example.realestatemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;

    opens org.example.realestatemanager to javafx.fxml;
    opens org.example.realestatemanager.contoller to javafx.fxml;

    exports org.example.realestatemanager;
    exports org.example.realestatemanager.contoller;
    exports org.example.realestatemanager.entity;
}
