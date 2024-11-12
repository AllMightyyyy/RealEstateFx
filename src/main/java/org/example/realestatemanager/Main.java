package org.example.realestatemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.realestatemanager.utils.DatabaseInitializer;

import java.io.IOException;

/**
 * Main class for the Property Management App.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database
        DatabaseInitializer.initializeDatabase();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1305, 684);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            primaryStage.setTitle("Property Management App");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}