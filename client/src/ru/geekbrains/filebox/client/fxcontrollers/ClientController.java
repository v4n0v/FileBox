package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.client.core.ClientConnectionManager;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.FileType;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.FilePacket;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class ClientController {

    private final static long MAX_FILE_SIZE = 5_242_880;
    private FilePacket filePacket;
    private FileBoxClientStart mainApp;

    public void setMainApp(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    private ClientController clientController;


    @FXML
    GridPane upperPanelLogged;

    @FXML
    Button btnRename;
    @FXML
    public Label lbLastUpd;
    /// регистрацию сую сюда же

    @FXML
    Label lblLogedInfo;
    @FXML
    Label lblFreeSpaceInfo;
    private Alert alert;
    private String loginReg;
    private String mailReg;
    private String pass1Reg;
    private Stage loginStage;

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }


    public void setLoggedInfoLabel(String name) {
        lblLogedInfo.setText("Logged in as " + name);
    }

    public void setFreeSpaceLabel(int space, int totalClientSpace) {
        lblFreeSpaceInfo.setText("Free space " + space + "kb" + " from " + totalClientSpace + "kb");
    }

    // прячем окно логина
    public void loginHide() {
        loginStage.close();
    }

    private void loginShow() {
        mainApp.showClientLoginLayout();
        //initClientLoginLayout();
        Log2File.writeLog("Login window opened");
        //  logger.info("Login window opened");
    }

    // ссылка на элемент модального окна дляполучения ссылки на общий элемент класса mainApp

    @FXML
    private TableView<FileListXMLElement> tblContent;
    @FXML
    private TableColumn<FileListXMLElement, String> fileNameColumn;
    @FXML
    private TableColumn<FileListXMLElement, Long> sizeColumn;

    private void updTable() {
        tblContent.setItems(mainApp.getServerFileList());
    }

    public void initTable() {
        tblContent.setEditable(false);
        fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());
        tblContent.setItems(mainApp.getServerFileList());
        System.out.println();

        tblContent.setRowFactory(tv -> {
            TableRow<FileListXMLElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileListXMLElement rowData = row.getItem();
                    switch (rowData.getType()) {
                        case FileType.FILE:
                            getFile(rowData.getFileName().getValue());
                            break;
                        case FileType.DIR:
                            String dirName = getFolderName(rowData.getFileName().getValue());
                            System.out.println(dirName);
                            enterDirectory(dirName);
                            break;
                        case FileType.UP_DIR:
                            enterDirectory("...");
                            break;
                        default:
                            System.out.println("wrong type of element");
                            //    logger.warning("wrong type of ");
                            break;
                    }
                    //Делайте, что требуется с элементом.
                }
            });
            return row;
        });
        Log2File.writeLog("Table initialized");
        //   logger.info("Table initialized");
    }

    public String getFolderName(String name) {
        String dirName = name;
        dirName = dirName.replace("[", "");
        dirName = dirName.replace("]", "");
        return dirName;
    }

    public void enterDirectory(String dir) {


        FileOperationPacket dirPacket = new FileOperationPacket(PackageType.ENTER_DIR, dir);
        mainApp.socketThread.sendPacket(dirPacket);
        Log2File.writeLog("Entering '" + dir + "' directory");
        //   logger.info("Entering '"+dir+"' directory");

    }

    public void createFolder() {
        mainApp.showNewFolderLayout();
        Log2File.writeLog("create new folder window opened");
    }

    // методы обработки нажатия на кнопку
    // переименование файла
    public void renameFile() {
        updTable();
        // получаем список выбранного элеменота таблицы
        FileListXMLElement fileListElement = tblContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();

            // открывем дилоговое окно
            if (currentFilename != null)
                mainApp.showRenameLayout(currentFilename);
        }
    }

    public void syncFileBox() {
        mainApp.showSyncLayout();
    }

    public void deleteFile() {

        System.out.println("delete");
        FileListXMLElement fileListElement = tblContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();

            // открывем дилоговое окно
            if (currentFilename != null) {
                boolean answer;
                if (fileListElement.getType().equals(FileType.DIR)) {
                    currentFilename = getFolderName(currentFilename);
                    answer = AlertWindow.dialogWindow("Delete folder and everything in it?", currentFilename);
                } else
                    answer = AlertWindow.dialogWindow("Delete file?", currentFilename);

                if (answer) {
                    FileOperationPacket deletePacket = new FileOperationPacket(PackageType.DELETE, currentFilename);
                    mainApp.socketThread.sendPacket(deletePacket);
                    mainApp.removeFromTable(currentFilename);
                    Log2File.writeLog(currentFilename + " deleted");
                    System.out.println("OK");

                } else {

                    Log2File.writeLog("deleting operation canceled");
                }
            }

        }

    }

    public void downloadFile() {

        System.out.println("download");
        FileListXMLElement fileListElement = tblContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();
            // открывем дилоговое окно
            getFile(currentFilename);
        }
    }

    public void getFile(String filename) {
        if (filename != null) {
            boolean answer = AlertWindow.dialogWindow("Download file?", filename);

            if (answer) {
                FileOperationPacket downLoad = new FileOperationPacket(PackageType.FILE_REQUEST, filename);
                mainApp.socketThread.sendPacket(downLoad);

                Log2File.writeLog("download operation '" + filename + "'complete");
            } else {

                Log2File.writeLog("download '" + filename + "' operation canceled");
            }
        }

    }

    //загрузка файла на сервер
    public void uploadFile() {
        sendFile();

        //  lastUpdate();
    }

    public void openOptions() {

        mainApp.showOptionsLayout();
    }

    // отсоединение от сервера, открытие окна логина
    public void logOut() throws Exception {

        System.out.println("Client logOut");
        mainApp.socketThread.close();
        Log2File.writeLog("Client LogOut");
        loginShow();
    }



    // отправка файла
    private synchronized void sendFile() {

        // выбираем файл
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainerSingle fileContainer = new FileContainerSingle();

        // упаковываем в контейнер  и отправляем
        packContainerAndSendFile(list, fileContainer);
        Log2File.writeLog("all files uploaded");
    }


    // обработка Drag'N'Drop
    // увидели, что перетягивается файл
    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    // обработати перетянутый на таблицу файл
    @FXML
    public void handleDrop(DragEvent event) {
        List<File> list = event.getDragboard().getFiles();
        FileContainerSingle fcs = new FileContainerSingle();
        packContainerAndSendFile(list, fcs);
        Log2File.writeLog("drog'n'drop");
    }

    public void packContainerAndSendFile(List<File> list, FileContainerSingle fileContainer) {
        mainApp.showProgressLayout("Files upload");
        ProgressModalController progressModalController = mainApp.getProgressController();

        double cntStep = 1d / list.size();

        double prgress = 0;
        // подсчитываем кол-во файлов и проверяем размер каждого файла
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                File file = list.get(i);
                if (file.length() > MAX_FILE_SIZE) {
                    AlertWindow.errorMesage("File size is more than 50MB");
                    Log2File.writeLog(Level.WARNING, file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                    //   logger.warning(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                    return;
                }
                // если файл подходящего размера, упаковываем в пакет
                try {
                    fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName(), file.length(), list.size());
                    filePacket = new FilePacket(fileContainer);
                    // logger.info("file '"+fileContainer.getName()+"' uploaded");
                    Log2File.writeLog("file '" + fileContainer.getName() + "' uploaded");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // logger.info("Sending packet. Type: " + filePacket.getPacketType());
                Log2File.writeLog(Level.WARNING, "Sending packet. Type: " + filePacket.getPacketType());
                // логируем  и отправляем
                mainApp.socketThread.sendPacket(filePacket);
                prgress += cntStep;
                System.out.println("ProgressBar: " + prgress);
                progressModalController.setProgress(prgress);

            }
        }
        //   progressModalController.getStage().close();
    }

    public boolean showConfirmation(String file) {
        boolean answer = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete File");
        alert.setHeaderText("Are you sure?");
        alert.setContentText(file);

        // option != null.
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            answer = true;
        }
        return answer;
    }


}
