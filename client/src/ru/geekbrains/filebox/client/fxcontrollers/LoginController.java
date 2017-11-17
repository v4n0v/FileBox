package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ru.geekbrains.filebox.client.core.ClientConnectionManager;
import ru.geekbrains.filebox.client.core.State;
import ru.geekbrains.filebox.library.AlertWindow;

public class LoginController extends  BaseController{


    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    @FXML
    VBox rootElement;
    private ClientConnectionManager clientManager;
    public void loginToFileBox() {

        // предаем сылки на главный гласс и контроллер
        clientManager = new ClientConnectionManager(mainApp);
        clientManager.setClientController(clientController);

        // если поля не пусты
        if (!fieldLogin.getText().isEmpty() || !fieldLogin.getText().isEmpty()) {
            clientManager.setLogin(fieldLogin.getText());
            clientManager.setPassword(fieldPass.getText());
//            clientManager.setLogin("admin");
//            clientManager.setPassword("12345");

            // меняем статус скиента и соединяемся
            clientManager.state = State.LOGIN;
            clientManager.connect();

        } else {
            AlertWindow.warningMesage("Fill mail and password fields");
        }

    }

    public void registerNew() {
        clientManager = new ClientConnectionManager(mainApp);
        clientManager.setClientController(clientController);

        mainApp.showRegisterNewLayout();
    }
}
