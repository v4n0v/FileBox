package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.core.ClientPreferences;



public class OptionsController extends BaseController{
    public void setPrefereces(ClientPreferences prefereces) {
        this.prefereces = prefereces;
    }

    ClientPreferences prefereces;

    @FXML
    TextField inboxPath;
    @FXML
    Button folder;
    @FXML
    Button okBtn;
    @FXML
    Button cancelBtn;
    @Override
    public void init() {
            inboxPath.setText(prefereces.getClientFolder());
            loginConfig();
    }

    private void loginConfig() {
        System.out.println("loadConfig");
    }

    @FXML
    private void okAndSave(){
        System.out.println("saveConfig");
    }

}
