package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;

public class BaseController {
    protected FileBoxClientStart mainApp;
      Stage stage;
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

    public void close() {
        stage.close();
    }
}
