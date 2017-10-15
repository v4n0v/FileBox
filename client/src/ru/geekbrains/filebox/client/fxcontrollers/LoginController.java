package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    @FXML
    VBox rootElement;

    public void tryToLogin() throws Exception {

        if (fieldLogin.getText().equals("1") && fieldPass.getText().equals("2")) {
            Stage mystage = (Stage)rootElement.getScene().getWindow();
            //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/loggedClient.fxml"));
            Parent root = fxmlLoader.load();
            mystage.setTitle("Main");
            mystage.setScene(new Scene(root, 465, 630));
            mystage.setResizable(false);
            ClientController cm = (ClientController)fxmlLoader.getController();
            cm.str = "Java";
            mystage.show();
        }
    }
}
