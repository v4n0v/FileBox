package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ru.geekbrains.filebox.client.core.FileBoxClientManager;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.FileType;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;


import java.io.File;
import java.util.ArrayList;

public class SyncController extends BaseController implements InitLayout {
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
//        tblClientContent.setItems(mainApp.serverFileList);
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


        tblServerContent.setRowFactory(tv -> {
            TableRow<FileListXMLElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileListXMLElement rowData = row.getItem();
                    if (rowData.getType().equals(FileType.FILE)) {
                        clientController.getFile(rowData.getFileName().getValue());
                    } else if (rowData.getType().equals(FileType.DIR)) {
                        String dirName = clientController.getFolderName(rowData.getFileName().getValue());
                        System.out.println(dirName);
                        clientController.enterDirectory(dirName);
                    } else if (rowData.getType().equals(FileType.UP_DIR)) {
                        clientController.enterDirectory("...");
                    } else {
                        System.out.println("wrong type of element");
                        //    logger.warning("wrong type of ");
                    }
                    //Делайте, что требуется с элементом.
                }
            });
            return row;
        });

        tblClientContent.setRowFactory(tv -> {
            TableRow<FileListXMLElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileListXMLElement rowData = row.getItem();
                    if (rowData.getType().equals(FileType.FILE)) {
                        System.out.println("double click " + rowData.getFileName().getValue());
                        //
                    } else if (rowData.getType().equals(FileType.DIR)) {
                        String dirName = clientController.getFolderName(rowData.getFileName().getValue());
                        clientPath+="\\"+dirName;
                        updateClientFileList(mainApp.getConfig().getPath( ) + clientPath, clientFileList);
//                        System.out.println(dirName);
//                        clientController.enterDirectory(dirName);
                    } else if (rowData.getType().equals(FileType.UP_DIR)) {
                       // clientController.enterDirectory("...");
                        System.out.println("...");
                        clientPath=clientPath.replace("\\", "/");
                        String[] folderPath = clientPath.split("/");
                        clientPath=clientPath.replace("/", "\\");
//        String[] prevFolderPath = new String[folderPath.length-1];
//        System.arraycopy(folderPath, 0, prevFolderPath, 0, prevFolderPath.length);
//        this.currentFolder
                        clientPath="";
                        if (folderPath.length!=2) {
                            for (int i = 0; i < folderPath.length - 1; i++) {
                                clientPath += folderPath[i] + "\\";
                            }
                        }
                        updateClientFileList(mainApp.getConfig().getPath( ) + clientPath, clientFileList);
                    } else {
                        System.out.println("wrong type of element");
                        //    logger.warning("wrong type of ");
                    }
                    //Делайте, что требуется с элементом.
                }
            });
            return row;
        });
    }
private String clientPath="";
    @Override
    public void init() {
        serverFileList = mainApp.getServerFileList();
        clientFileList = mainApp.getClientFileList();
        //  clientFileList=
        //  ArrayList<FileListXMLElement> fileList = new ArrayList<>();
        updateClientFileList(mainApp.getConfig().getPath()+clientPath, clientFileList);
//        clientFileList.addAll(updateClientFileList(mainApp.getConfig().getPath()));

        initTable();
    }

    private void updateClientFileList(String path, ObservableList fileList) {
        fileList.clear();
        if (clientPath != null && !clientPath.equals("")) {

            fileList.add(new FileListXMLElement("...", 0l, FileType.UP_DIR));
        }
        // создаем список
        File[] fList;
        File clientFolder = new File(path);
        //  String len;
        fList = clientFolder.listFiles();

//        for (int i = 0; i < fList.size(); i++) {
//            fileList.add(new FileListXMLElement(fList.get(i).getFileName(), fList.get(i).getFileSize()));
//        }
        if (fList.length!=0) {

            for (int i = 0; i < fList.length; i++) {
                //Нужны только папки в место isFile() пишим isDirectory()
                if (fList[i].isFile()) {
                    String name = fList[i].getName();
                    long len = fList[i].length();
                    fileList.add(new FileListXMLElement(fList[i].getName(), fList[i].length(), FileType.FILE));

                } else if (fList[i].isDirectory()) {
                    fileList.add(new FileListXMLElement("[" + fList[i].getName() + "]", fList[i].length() / 1024, FileType.DIR));
                    // fc.add(element);
                }

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

                    // прикостылил сброр фалов в список. в таблице выбирается только 1 файл, будет несколько, в любом
                    // случае, будет список,
                    // packContainerAndSendFile работет со списками

                    ArrayList<File> files = new ArrayList();
                    files.add(new File(mainApp.getConfig().getPath()+clientPath + "\\" + currentFilename));

                    FileContainerSingle fcs = new FileContainerSingle();
                    clientController.packContainerAndSendFile(files, fcs);
//                    FileOperationPacket downLoad = new FileOperationPacket(PackageType.FILE_REQUEST, currentFilename);
//                    mainApp.socketThread.sendPacket(downLoad);

                    Log2File.writeLog("download operation '" + currentFilename + "'complete");
                } else {

                    Log2File.writeLog("download '" + currentFilename + "' operation canceled");
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

                    Log2File.writeLog("download operation '" + currentFilename + "'complete");
                } else {

                    Log2File.writeLog("download '" + currentFilename + "' operation canceled");
                }
            }
        }
      //  updateClientFileList(mainApp.getConfig().getPath()+clientPath, clientFileList);
        System.out.println("download");

    }

    public void setClientManager(FileBoxClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
