package com.aurora7795.terminal;

import com.aurora7795.purejavacommWrapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.util.List;

public class Controller {
    public TextArea responseTB;
    public Button sendBtn;
    public TextArea requestTB;
    public ChoiceBox<String> portListBox;
    public Button ConnectBtn;
    public Button readBtn;

    purejavacommWrapper serialPort;

    private BooleanProperty shouldBeDisabled;

    @FXML
    public void initialize() {
        shouldBeDisabled = new SimpleBooleanProperty(true);

        List<String> availableSerialPorts = purejavacommWrapper.getAvailableSerialPorts();
//        String[] tempList = new String[availableSerialPorts.Size()];
//        availableSerialPorts.toArray(tempList);
        ObservableList<String> portList = FXCollections.observableArrayList(availableSerialPorts);
        portListBox.setItems(portList);

        sendBtn.disableProperty().bind(shouldBeDisabled);
    }

    public void submit(ActionEvent actionEvent) {

        try {
            serialPort.Write(requestTB.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            responseTB.appendText(String.format("%s%s", serialPort.Read(), System.getProperty("line.separator")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connectBtnClick(ActionEvent actionEvent) {

        String port = portListBox.getValue();

        serialPort = new purejavacommWrapper(port, 9600);
        responseTB.appendText(String.format("Connected to %s%s", port, System.getProperty("line.separator")));
        shouldBeDisabled.setValue(false);
    }

    public void readClick(ActionEvent actionEvent) {
        try {
            responseTB.appendText(String.format("%s%s", serialPort.Read(), System.getProperty("line.separator")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getIdBtnClick(ActionEvent actionEvent) {
    }
}
