package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;

import java.awt.*;

public class BaseController {
    protected FileBoxClientStart mainApp;
    protected Stage stage;
    protected ClientController clientController;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setMainApp(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
    }

    public void init(){


    }
 //   @FXML
    public void close(){

        stage.close();
    }
}
