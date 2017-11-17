package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;

import java.io.File;

public class NewFolderController extends BaseController implements InitLayout {

    public enum Destination {CLIENT, SERVER}

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
    private Destination destinaton;

    public void setDestinaton(Destination destinaton) {
        this.destinaton = destinaton;
    }

    @FXML
    public void newFolder() {
        newName = fieldNewFolder.getText();
        Stage stage = (Stage) renameRootElement.getScene().getWindow();
        if (destinaton== Destination.SERVER) {
            if (newName.isEmpty()) {
                AlertWindow.warningMesage("Enter new name");

            } else {
                FileOperationPacket fop = new FileOperationPacket(PackageType.NEW_FOLDER, newName);
                mainApp.socketThread.sendPacket(fop);
                Log2File.writeLog("Created folder '" + newName + "'");
            }

        } else {
            File folder = new File(mainApp.getConfig().getPath()+"\\"+newName);
            if (!folder.exists()) {
                folder.mkdir();
            }
            mainApp.getSyncController().updateClientFileList(mainApp.getConfig().getPath(),
                    mainApp.getSyncController().getClientFileList());
        }
        stage.close();
    }


    @Override
    public void init() {
        //fieldNewName.setText(folderName);
    }
}
