package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The ClinicManagerMain class serves as the entry point for the Clinic Manager application.
 * It initializes the JavaFX application and loads the main user interface.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class ClinicManagerMain extends Application {

    /**
     * The start method is called when the JavaFX application is launched.
     *
     * It initializes the main stage, loads the FXML layout, and sets the scene for the application.
     *
     * @param stage the primary stage for this application, onto which the application scene can be set
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClinicManagerMain.class.getResource("clinic-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 425);
        stage.setTitle("Clinic Manager");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method serves as the entry point for the Java application.
     *
     * It launches the JavaFX application by invoking the launch method.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }
}