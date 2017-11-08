package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.core.FileBoxClientManager;
import ru.geekbrains.filebox.client.core.State;
import ru.geekbrains.filebox.library.AlertWindow;

public class LoginController extends  BaseController{


    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    @FXML
    VBox rootElement;
    FileBoxClientManager clientManager;
    public void loginToFileBox() {

        // предаем сылки на главный гласс и контроллер
        clientManager = new FileBoxClientManager(mainApp);
        clientManager.setClientController(clientController);

        // если поля не пусты
   //     if (!fieldLogin.getText().isEmpty() || !fieldLogin.getText().isEmpty()) {
//            clientManager.setLogin(fieldLogin.getText());
//            clientManager.setPassword(fieldPass.getText());
            clientManager.setLogin("admin");
            clientManager.setPassword("12345");

            // меняем статус скиента и соединяемся
            clientManager.state = State.LOGIN;
            clientManager.connect();

//        } else {
//            AlertWindow.warningMesage("Fill mail and password fields");
//        }

    }

    public void registerNew() {
//        Stage stage = (Stage) reg.getScene().getWindow();
//
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/registration_modal.fxml"));
//        Parent root1 = null;
//        try {
//            root1 = (Parent) fxmlLoader.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Stage registrStage = new Stage();
//        registrStage.initModality(Modality.WINDOW_MODAL);
//        registrStage.initOwner(stage);///
//        registrStage.setTitle("New user registration ");
//        registrStage.setScene(new Scene(root1));
//
//        registrStage.setResizable(false);
//        registrStage.show();
        clientManager = new FileBoxClientManager(mainApp);
        clientManager.setClientController(clientController);

        mainApp.showRegisterNewLayout();
    }
}
