package com.aurora7795.sampleapp;

import com.aurora7795.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

import java.util.List;

public class Controller {
    public ChoiceBox<String> portListBox;
    public Button ConnectBtn;
    public TextArea responseTB;
    public Button getIdBtn;
    public Button sendPhoneToneBtn;
    public Button startRecognitionBtn;

    purejavacommWrapper serialPort;
    Service<Void> ser;
    private EasyVRLibrary _tempVr;
    private BooleanProperty shouldBeDisabled;

    @FXML
    public void initialize() {
        shouldBeDisabled = new SimpleBooleanProperty(true);

        List<String> availableSerialPorts = purejavacommWrapper.getAvailableSerialPorts();
        ObservableList<String> portList = FXCollections.observableArrayList(availableSerialPorts);
        portListBox.setItems(portList);

        getIdBtn.disableProperty().bind(shouldBeDisabled);
        sendPhoneToneBtn.disableProperty().bind(shouldBeDisabled);
        startRecognitionBtn.disableProperty().bind(shouldBeDisabled);

        ser = new Service<Void>() {
            @Override
            protected Task createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                        //Logic for simple recognition activity

                        // Set a 5 second timeout for the recognition (optional)
                        _tempVr.SetTimeout(5);
                        _tempVr.SetMicDistance(Protocol.Distance.FAR_MIC);

                        updateMessage(String.format("Speak%s", System.getProperty("line.separator")));

                        //instruct the module to listen for a built in word from the 1st wordset
                        _tempVr.RecognizeWord(1);

                        //need to wait until HasFinished has completed before collecting results
                        while (!_tempVr.HasFinished()) {
                            updateMessage(".");
                        }

                        // Once HasFinished has returned true, we can ask the module for the index of the word it recognised. If you're new to using the EasyVR module,
                        // download the Easy VR Commander (http://www.veear.eu/downloads/) to interrogate the config of your module and see what the indexes correspond to
                        // Here is a standard setup at time of writing for an EASYVR 3 module:
                        // 0=Action,1=Move,2=Turn,3=Run,4=Look,5=Attack,6=Stop,7=Hello
                        int indexOfRecognisedWord = _tempVr.GetWord();

                        updateMessage(String.format("Response: %d%s", indexOfRecognisedWord, System.getProperty("line.separator")));
                      //  updateMessage(String.format("Recognition finished%s", System.getProperty("line.separator")));

                        return null;
                    }
                };
            }
        };
        ser.setOnSucceeded((WorkerStateEvent event) -> {
            // Anything which you want to update on javafx thread (GUI) after completion of background process.
        });

    }


    public void connectBtnClick(ActionEvent actionEvent) {

        String port = portListBox.getValue();
        ISerialPortWrapper serialPortWrapper = new purejavacommWrapper(port, 9600);
        _tempVr = new EasyVRLibrary(serialPortWrapper);

        responseTB.appendText(String.format("Connected to %s%s", port, System.getProperty("line.separator")));
        shouldBeDisabled.setValue(false);
    }


    public void getIdBtnClick(ActionEvent actionEvent) {
        ModuleId temp = _tempVr.GetId();
        responseTB.appendText(String.format("The return was %s%s", temp, System.getProperty("line.separator")));
        responseTB.appendText("");
    }

    public void sendPhoneToneBtnClick(ActionEvent actionEvent) {
        Boolean temp1 = _tempVr.PlayPhoneTone(3, 30);
        responseTB.appendText(String.format("The return was: %s%s", temp1, System.getProperty("line.separator")));
        responseTB.appendText("");
    }

    public void startRecogBtnClick(ActionEvent actionEvent) {
        responseTB.textProperty().bind(ser.messageProperty());

        if (!ser.isRunning()) {
            ser.reset();
            ser.start();
        }
    }


}
