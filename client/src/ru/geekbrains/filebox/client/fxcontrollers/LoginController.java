package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    public VBox getRootElement() {
        return rootElement;
    }

    @FXML
    VBox rootElement;

    public void loginToFileBox() throws Exception {

        Stage mystage = (Stage) rootElement.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/loggedClient.fxml"));
        Parent root = fxmlLoader.load();

        ClientController cm = (ClientController) fxmlLoader.getController();
        if (!fieldLogin.getText().isEmpty()||!fieldLogin.getText().isEmpty()) {
            cm.login = fieldLogin.getText();
            cm.password = fieldPass.getText();


          cm.connect();
           //cm.authorize(cm.login, cm.password);


//            if (cm.isAuthorized()) {
                if (cm.state == ClientController.State.CONNECTED) {
                    mystage.setTitle("FileBoxClient");
                    mystage.setScene(new Scene(root, 465, 630));
                    mystage.setResizable(false);

                    mystage.show();
                } else return;
//            } else {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Error");
//                alert.setHeaderText(null);
//                alert.setContentText("Wrong email or password");
//                alert.showAndWait();
//
//            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Fill mail and password fields");
            alert.showAndWait();
        }
    }


}
