package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Logger;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;

public class NewFolderController extends BaseController implements InitLayout {

    private String newName;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    Button btnRename;
    @FXML
    TextField fieldNewFolder;
    @FXML
    HBox renameRootElement;

    public void newFolder() {
        newName = fieldNewFolder.getText();

        if (newName.isEmpty()) {
            AlertWindow.warningMesage("Enter new name");

        } else {
            FileOperationPacket fop = new FileOperationPacket(PackageType.NEW_FOLDER, newName);

            mainApp.socketThread.sendPacket(fop);
            Logger.writeLog("Created folder '"+newName+"'");
        }
        Stage stage = (Stage) renameRootElement.getScene().getWindow();

        stage.close();
    }


    @Override
    public void init() {
        //fieldNewName.setText(folderName);
    }
}
