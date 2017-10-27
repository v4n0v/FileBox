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

//        Stage mystage = (Stage) rootElement.getScene().getWindow();
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/logged_client.fxml"));
//        Parent root = fxmlLoader.load();
//
//        ClientController cm = (ClientController) fxmlLoader.getController();
//        if (!fieldLogin.getText().isEmpty()||!fieldLogin.getText().isEmpty()) {
//            cm.login = fieldLogin.getText();
//            cm.password = fieldPass.getText();
//            cm.connect();
//            mystage.hide();
//        } else {
//            Alert alert = new Alert(Alert.AlertType.WARNING);
//            alert.setTitle("Warning");
//            alert.setHeaderText(null);
//            alert.setContentText("Fill mail and password fields");
//            alert.showAndWait();
//        }
    }


}
