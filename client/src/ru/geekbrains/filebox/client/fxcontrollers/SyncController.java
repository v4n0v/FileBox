package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.geekbrains.filebox.client.core.FileBoxClientManager;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Logger;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;
import ru.geekbrains.filebox.network.packet.packet_container.FileListContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;


import java.io.File;
import java.util.ArrayList;

public class SyncController extends BaseController {
    ObservableList<FileListXMLElement> clientFileList = FXCollections.observableArrayList();
    ObservableList<FileListXMLElement> serverFileList = FXCollections.observableArrayList();
    FileBoxClientManager clientManager;


    @FXML
    private TableView<FileListXMLElement> tblClientContent;
    @FXML
    private TableColumn<FileListXMLElement, String> clientFileNameColumn;
    @FXML
    private TableColumn<FileListXMLElement, Long> clientSizeColumn;
    @FXML
    private TableView<FileListXMLElement> tblServerContent;
    @FXML
    private TableColumn<FileListXMLElement, String> serverFileNameColumn;
    @FXML
    private TableColumn<FileListXMLElement, Long> severSizeColumn;

//    public void updTable() {
//        tblClientContent.setItems(mainApp.fileListDataProp);
//    }

    public void initTable() {
        tblClientContent.setEditable(false);
        clientFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        clientSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());
        tblClientContent.setItems(clientFileList);

        tblServerContent.setEditable(false);
        serverFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        severSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());
        tblServerContent.setItems(serverFileList);
    }

    @Override
    public void init() {
        serverFileList = mainApp.getFileListDataProp();
        clientFileList=mainApp.getClientFileList();
      //  clientFileList=
      //  ArrayList<FileListXMLElement> fileList = new ArrayList<>();
        updateClientFileList(mainApp.getConfig().getPath(), clientFileList);
//        clientFileList.addAll(updateClientFileList(mainApp.getConfig().getPath()));

        initTable();
    }

    private void updateClientFileList(String path, ObservableList fileList) {


        // создаем списаок
        File[] fList;
        File clientFolder = new File(path);
        //  String len;
        fList = clientFolder.listFiles();
        fileList.clear();
//        for (int i = 0; i < fList.size(); i++) {
//            fileList.add(new FileListXMLElement(fList.get(i).getFileName(), fList.get(i).getFileSize()));
//        }
        for (int i = 0; i < fList.length; i++) {
            //Нужны только папки в место isFile() пишим isDirectory()
            if (fList[i].isFile()) {
                String name =fList[i].getName();
                long  len =  fList[i].length();
                        fileList.add(new FileListXMLElement(fList[i].getName(), fList[i].length()));
            }
        }


    }

    public void uploadToServer() {
        FileListXMLElement fileListElement = tblClientContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();


            // открывем дилоговое окно
            if (currentFilename != null) {
                boolean answer = AlertWindow.dialogWindow("Upload file?", currentFilename);


                if (answer) {

                    // прикостылис сброр фалов в список. в таблице выбирается только 1 файл, будет несколько в лубом случае будет список,
                    // packContainerAndSendFile работет со списками
                    // TODO сделать возможность мультивыбора
                    ArrayList <File>files =  new ArrayList();
                    files.add(new File (mainApp.getConfig().getPath()+"\\"+currentFilename));

                    FileContainerSingle fcs = new FileContainerSingle();
                    clientController.packContainerAndSendFile(files, fcs);
//                    FileOperationPacket downLoad = new FileOperationPacket(PackageType.FILE_REQUEST, currentFilename);
//                    mainApp.socketThread.sendPacket(downLoad);

                    Logger.writeLog("download operation '" + currentFilename + "'complete");
                } else {

                    Logger.writeLog("download '" + currentFilename + "' operation canceled");
                }
            }
        }

        System.out.println("uploadToServer");
    }

    public void downloadFromServer() {

        FileListXMLElement fileListElement = tblServerContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();
            // открывем дилоговое окно
            if (currentFilename != null) {
                boolean answer = AlertWindow.dialogWindow("Download file?", currentFilename);

                if (answer) {
                    FileOperationPacket downLoad = new FileOperationPacket(PackageType.FILE_REQUEST, currentFilename);
                    mainApp.socketThread.sendPacket(downLoad);

                    Logger.writeLog("download operation '" + currentFilename + "'complete");
                } else {

                    Logger.writeLog("download '" + currentFilename + "' operation canceled");
                }
            }
        }

        System.out.println("download");

    }

    public void setClientManager(FileBoxClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
