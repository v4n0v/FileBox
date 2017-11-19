package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class RenameController extends BaseController implements InitLayout{

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    private String currentName;
    private String newName;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }


    @FXML
    Button btnRename;
    @FXML
    TextField fieldNewName;
    @FXML
    HBox renameRootElement;



    public void renameFile() {
        newName = fieldNewName.getText();

        if (newName.isEmpty()) {
            AlertWindow.warningMesage("Enter new name");

        } else {
            FileOperationPacket renamePacket = new FileOperationPacket(PackageType.RENAME,
                    currentName + "<>" + newName);
            mainApp.socketThread.sendPacket(renamePacket);
            Log2File.writeLog("File '"+currentName+"' renamed to '"+newName+"'");
        }
        Stage stage = (Stage) renameRootElement.getScene().getWindow();

        stage.close();
    }


    @Override
    public void init() {
        fieldNewName.setText(currentName);
    }


}
