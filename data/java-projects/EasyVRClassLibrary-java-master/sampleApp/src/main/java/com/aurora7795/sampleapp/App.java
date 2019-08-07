package com.aurora7795.sampleapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Created by Martin Bradford Gago on 15/02/2017.
 */
public class App extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader();

        URL location = getClass().getResource("/fxml/sample.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        Parent root = fxmlLoader.load(location.openStream());
        primaryStage.setTitle("Easy VR Sample Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }
}
