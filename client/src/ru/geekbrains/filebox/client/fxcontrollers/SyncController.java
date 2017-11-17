package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ru.geekbrains.filebox.client.core.ClientConnectionManager;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.FileType;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class SyncController extends BaseController implements InitLayout {
    private ObservableList<FileListXMLElement> clientFileList = FXCollections.observableArrayList();
    private ObservableList<FileListXMLElement> serverFileList = FXCollections.observableArrayList();
    private ClientConnectionManager clientManager;

    public ObservableList<FileListXMLElement> getClientFileList() {
        return clientFileList;
    }

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


    private String TEMP_PATH;
    private String currentAbsPath;

// инициализация таблицы
    private  void initTable() {
        tblClientContent.setEditable(false);
        clientFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        clientSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());
        tblClientContent.setItems(clientFileList);

        tblServerContent.setEditable(false);
        serverFileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        severSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());
        tblServerContent.setItems(serverFileList);

// обработака двойного клика по таблице сервера
        tblServerContent.setRowFactory(tv -> {
            TableRow<FileListXMLElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileListXMLElement rowData = row.getItem();
                    // если это файл, качаем файл
                    if (rowData.getType().equals(FileType.FILE)) {
                        clientController.getFile(rowData.getFileName().getValue());

                    /// если папка, вхходим в папку
                    } else if (rowData.getType().equals(FileType.DIR)) {

                        String dirName = clientController.getFolderName(rowData.getFileName().getValue());
                        System.out.println(dirName);
                        clientController.enterDirectory(dirName);

                        // если "..." то идем вверх
                    } else if (rowData.getType().equals(FileType.UP_DIR)) {
                        clientController.enterDirectory("...");
                    } else {
                        System.out.println("Sync server.  wrong type of element");
                        Log2File.writeLog(Level.WARNING, "Sync. Wrong type of table element");

                    }
                    //Делайте, что требуется с элементом.
                }
            });
            return row;
        });
// обработака двойного клика по таблице клиента
        tblClientContent.setRowFactory(tv -> {
            TableRow<FileListXMLElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileListXMLElement rowData = row.getItem();

                    // если файл ничего не делаем
                    if (rowData.getType().equals(FileType.FILE)) {
                        System.out.println("double click " + rowData.getFileName().getValue());

                    // если папка
                    } else if (rowData.getType().equals(FileType.DIR)) {
                        // получаем путь
                        String dirName = clientController.getFolderName(rowData.getFileName().getValue());

                        // сохраняем путь
                        clientPath += dirName + "\\";
                        currentAbsPath = TEMP_PATH + clientPath;
                        // передаем ткущий путь в mainApp, тк путь этот путь нужен для указания директории куда качать
                        mainApp.getConfig().setPath(currentAbsPath);
                        // обновляем файл лист
                        updateClientFileList(mainApp.getConfig().getPath(), clientFileList);
//
                        Log2File.writeLog("Sync client. Enter dir:" + mainApp.getConfig().getPath());

                    } else if (rowData.getType().equals(FileType.UP_DIR)) {
                        // clientController.enterDirectory("...");
                        System.out.println("...");

                        // костылю обратный слеш, тк не получается сделать сплит по "\\"
                        clientPath = clientPath.replace("\\", "/");
                        currentAbsPath = TEMP_PATH + clientPath;
                        // получаю длину текущей папки
                        String[] folderPath = clientPath.split("/");

                        // получаю длину папки по умолчанию
                        clientPath = TEMP_PATH.replace("\\", "/");
                        String[] defaultFolderPath = clientPath.split("/");
                        // обнуляю путь
                        clientPath = "";
                        // сравниваю длины путей папок, если не равны, значит мы не корневом каталоге
                        if (folderPath.length + defaultFolderPath.length > defaultFolderPath.length) {
                            for (int i = 0; i < folderPath.length - 1; i++) {
                                clientPath += folderPath[i] + "\\";
                            }
                        }
                        // если равны, то тущая папка станет равна корневой
                        currentAbsPath = TEMP_PATH + clientPath;
                        mainApp.getConfig().setPath(currentAbsPath);
                        // обновляем файл лист
                        updateClientFileList(mainApp.getConfig().getPath(), clientFileList);
                        Log2File.writeLog("Sync client. Enter dir:" + mainApp.getConfig().getPath());

                    } else {
                        System.out.println("wrong type of element");
                        Log2File.writeLog("Sync client. Wrong type of table element");
                    }

                }
            });
            return row;
        });
    }

    private String clientPath = "";
    // bybwbfkbpfwbz лейаута
    @Override
    public void init() {
        serverFileList = mainApp.getServerFileList();
        clientFileList = mainApp.getClientFileList();

        updateClientFileList(mainApp.getConfig().getPath() + clientPath, clientFileList);
        // сохраняем путь корнвого каталога клиента
        TEMP_PATH = mainApp.getConfig().getPath() + "\\";
        initTable();
    }

    // обнвление текущей папки клиента
    public void updateClientFileList(String path, ObservableList fileList) {
        fileList.clear();
        // если каталог не корневой, добавляем элемент, указывающий на выход в прерыд каталог
        if (clientPath != null && !clientPath.equals("")) {

            fileList.add(new FileListXMLElement("...", 0l, FileType.UP_DIR));
        }
        // создаем список
        File[] fList;
        File clientFolder = new File(path);
        fList = clientFolder.listFiles();

        if (fList.length != 0 || fileList.size() != 0) {

            for (int i = 0; i < fList.length; i++) {
                //Нужны только папки в место isFile() пишим isDirectory()
                if (fList[i].isFile()) {
                    fileList.add(new FileListXMLElement(fList[i].getName(), fList[i].length(), FileType.FILE));
                } else if (fList[i].isDirectory()) {
                    fileList.add(new FileListXMLElement("[" + fList[i].getName() + "]", fList[i].length() / 1024, FileType.DIR));

                }

            }
        }

        // сортировка, но она не работает
        Collections.sort(fileList, FileListXMLElement.FileNameComparator);
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
                    // случае, будет список

                    ArrayList<File> files = new ArrayList();
                    files.add(new File(mainApp.getConfig().getPath() + clientPath + "\\" + currentFilename));

                    FileContainerSingle fcs = new FileContainerSingle();
                    clientController.packContainerAndSendFile(files, fcs);

                    Log2File.writeLog("download operation '" + currentFilename + "'complete");
                } else {

                    Log2File.writeLog("download '" + currentFilename + "' operation canceled");
                }
            }
        }

        System.out.println("uploadToServer");
    }
    // скачиваем с сервера в текущую папку клиента
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

    public void setClientManager(ClientConnectionManager clientManager) {
        this.clientManager = clientManager;
    }

    // закрываем окно, обнуляем текуйщий путь к папкие клиента на дефолтный
    @Override
    public void close() {
        super.close();
        mainApp.getConfig().setPath(TEMP_PATH);
        System.out.println(mainApp.getConfig().getPath());
        Log2File.writeLog("Sync closed, client directory: " + mainApp.getConfig().getPath());
    }

    public void addFolder(){
        mainApp.showNewFolderLayout(NewFolderController.Destination.CLIENT);
        updateClientFileList(mainApp.getConfig().getPath(), clientFileList);
    }
    public void addServerFolder(){
        mainApp.showNewFolderLayout(NewFolderController.Destination.SERVER);

    }

    public void delClientFolder(){

        FileListXMLElement fileListElement = tblClientContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();
            currentFilename = currentFilename.replace("[", "");
            currentFilename = currentFilename.replace("]", "");
            // открывем дилоговое окно
            if (currentFilename != null) {
                boolean answer;
                if (fileListElement.getType().equals(FileType.DIR)) {

                    answer = AlertWindow.dialogWindow("Delete folder and everything in it?", currentFilename);
                } else
                    answer = AlertWindow.dialogWindow("Delete file?", currentFilename);

                if (answer) {
                    File file = new File(mainApp.getConfig().getPath() + "\\" + currentFilename);
                    System.out.println(file.getAbsolutePath());
                    if (file.isDirectory()) {

                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
                System.out.println("delete " + currentFilename);
                updateClientFileList(mainApp.getConfig().getPath(), clientFileList);
            }
        }

    }
    public void  delServerFolder(){
        System.out.println("delete");
        FileListXMLElement fileListElement = tblServerContent.getSelectionModel().getSelectedItem();
        if (fileListElement != null) {
            String currentFilename = fileListElement.getFileName().getValue();
            currentFilename = currentFilename.replace("[", "");
            currentFilename = currentFilename.replace("]", "");
            // открывем дилоговое окно
            if (currentFilename != null) {
                boolean answer;
                if (fileListElement.getType().equals(FileType.DIR)) {

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

    private static void deleteDirectory(File dir) {
        // проверяем файл это или папка
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // если папка не пуста, то удаляем все фалы внтри нее, а затем ее саму
            for (int i=0; i<children.length; i++) {
                File f = new File(dir, children[i]);
                deleteDirectory(f);
            }
            dir.delete();
        } else dir.delete();
    }
}
