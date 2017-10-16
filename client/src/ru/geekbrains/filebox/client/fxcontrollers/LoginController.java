package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import java.io.IOException;
import java.net.Socket;

public class LoginController {


    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    @FXML
    VBox rootElement;

    public void loginToFileBox() throws Exception {

        Stage mystage = (Stage) rootElement.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/loggedClient.fxml"));
        Parent root = fxmlLoader.load();

        ClientController cm = (ClientController) fxmlLoader.getController();
        cm.connect();
        cm.login=fieldLogin.getText();
        cm.password=fieldPass.getText();
        if (cm.state == ClientController.State.CONNECTED) {
            mystage.setTitle("Main");
            mystage.setScene(new Scene(root, 465, 630));
            mystage.setResizable(false);

            mystage.show();
        } else return;
    }


}
