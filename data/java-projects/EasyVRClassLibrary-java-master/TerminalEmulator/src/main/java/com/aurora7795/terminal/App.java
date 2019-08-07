package com.aurora7795.terminal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    private FXMLLoader fxmlLoader;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        fxmlLoader = new FXMLLoader();

        URL location = getClass().getResource("/fxml/terminalEmulator.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        Parent root = fxmlLoader.load(location.openStream());
        primaryStage.setTitle("Terminal Emulator");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

//    @Override
//    public void stop() {
//        ((Controller) fxmlLoader.getController()).serialPort.Disconnect();
//    }
}
