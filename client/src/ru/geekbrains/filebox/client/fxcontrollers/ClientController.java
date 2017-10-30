package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.client.core.FileBoxClientManager;
import ru.geekbrains.filebox.client.core.State;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Logger;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClientController {

    private final static int MIN_PASS_LENGTH = 2;
    private final static int MAX_PASS_LENGTH = 32;
    private final static long MAX_FILE_SIZE = 5_242_880;
    private FilePacket filePacket;
    private FileBoxClientStart mainApp;

    public void setMainApp(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
        //     this.socketThread=mainApp.getSocketThread();
    }

    private FileBoxClientManager clientManager;
    @FXML
    VBox loggedRootElement;
    @FXML
    GridPane upperPanelLogin;
    @FXML
    GridPane upperPanelLogged;
    @FXML
    TextField fieldLogin;
    @FXML
    PasswordField fieldPass;
    @FXML
    Button reg;
    @FXML
    VBox rootElement;
    @FXML
    Button btnRename;
    @FXML
    Label lbLastUpd;
    /// регистрацию сую сюда же
    @FXML
    TextField loginRegField;
    @FXML
    TextField mailRegField;
    @FXML
    PasswordField pass1RegField;
    @FXML
    PasswordField pass2RegField;
    @FXML
    Button exit;
    @FXML
    Button addNew;

    private Alert alert;
    private String loginReg;
    private String mailReg;
    private String pass1Reg;
    Stage loginStage;

    public void initClientLoginLayout() {
        try {
            // новое окно логина
            loginStage = new Stage();
            FXMLLoader loaderLog = new FXMLLoader();
            loaderLog.setLocation(FileBoxClientStart.class.getResource("fxml/login_modal.fxml"));
            loginRootElement = (VBox) loaderLog.load();
            ClientController controller = loaderLog.getController();
            // получаем ссылку у контроллера окна
            controller.setMainApp(mainApp);
            loginStage.setTitle("Login");
            loginStage.setOnCloseRequest((event) -> mainApp.getPrimaryStage().close());

            loginStage.setResizable(false);
            Scene sceneLog = new Scene(loginRootElement);
            loginStage.setScene(sceneLog);
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(btnRename.getScene().getWindow());
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // оправка логниа пароля для аутентификации
    public void loginToFileBox() {
        // предаем сылки на главный гласс и контроллер
        clientManager = new FileBoxClientManager(mainApp);
        clientManager.setClientController(this);
        // если поля не пусты
        if (!fieldLogin.getText().isEmpty() || !fieldLogin.getText().isEmpty()) {
            clientManager.setLogin(fieldLogin.getText());
            clientManager.setPassword(fieldPass.getText());

            // меняем статус скиента и соединяемся
            clientManager.state = State.LOGIN;
            clientManager.connect();

        } else {
            AlertWindow.warningMesage("Fill mail and password fields");
        }

    }

    public void registerNew() {
        Stage stage = (Stage) reg.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/registration.fxml"));
        Parent root1 = null;
        try {
            root1 = (Parent) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage registrStage = new Stage();
        registrStage.initModality(Modality.WINDOW_MODAL);
        registrStage.initOwner(stage);
        registrStage.setTitle("New user registration ");
        registrStage.setScene(new Scene(root1));

        registrStage.setResizable(false);
        registrStage.show();

    }

    // добавляем пользователя
    public void addUser() {
        String pass2Reg;
        loginReg = loginRegField.getText();
        mailReg = mailRegField.getText();
        mailReg = mailRegField.getText();
        pass1Reg = pass1RegField.getText();
        pass2Reg = pass2RegField.getText();
        if (loginReg.isEmpty() && mailReg.isEmpty()
                && pass1Reg.isEmpty() && pass2Reg.isEmpty()) {
            AlertWindow.errorMesage("Fill the all fields");
        } else if (pass1Reg.length() < MIN_PASS_LENGTH || pass1Reg.length() > MAX_PASS_LENGTH) {
            AlertWindow.errorMesage("Password must be from" + MIN_PASS_LENGTH + " to " + MAX_PASS_LENGTH + " words.");
        } else {
            if (pass1Reg.equals(pass2Reg)) {
                clientManager.setRegistritionInfo(loginReg, mailReg, pass1Reg);

                clientManager.state = State.REGISTRATION;
                clientManager.connect();
            } else {
                AlertWindow.errorMesage("Password fields are not equals");
            }
        }
    }

    // отработка нажатия на cancel
    public void regExit() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

    // прячем окно логина

    public void loginHide() {
        Stage stage = (Stage) rootElement.getScene().getWindow();
        stage.close();
    }

    public void loginShow() {

        initClientLoginLayout();

    }

    // ссылка на элемент модального окна дляполучения ссылки на общий элемент класса mainApp
    @FXML
    private VBox loginRootElement;
    @FXML
    private TableView<FileListElement> tblContent;
    @FXML
    private TableColumn<FileListElement, String> fileNameColumn;
    @FXML
    private TableColumn<FileListElement, Long> sizeColumn;


    public synchronized  void updateTable() {
        tblContent.setEditable(false);
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<FileListElement, String>("fileName"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<FileListElement, Long>("fileSize"));
        tblContent.setItems(mainApp.fileListData);

        System.out.println();
    }

    /// инициализация модального окна логина, в которое передается ссылка на mainApp


    // меоды обработки нажатия на кнопку
    public void renameFile() {

        System.out.println("rename");
        updateTable();
    }

    public void deleteFile() {
        System.out.println("delete");
    }

    public void downloadFile() {
        System.out.println("download");
    }

    //загрузка файла на сервер
    public void uploadFile() {
        sendFile();
        System.out.println("upload");
        lastUpdate();
    }

    public void openOptions() {
        System.out.println("options");
    }

    // отсоединение от сервера, открытие окна логина
    public void logOut() throws Exception {

        System.out.println("Client logOut");
//        clientManager.disconnect();
        mainApp.socketThread.close();
        Logger.writeLog("Client LogOut");

        loginShow();
    }


    // отправка файла
    public synchronized void sendFile() {
        // получаем ссылку на сокет
        //socketThread = mainApp.getSocketThread();
        // выбираем файл
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainer fileContainer = new FileContainer();

        // подсчитываем кол-во файлов и проверяем размер каждого файла
        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            if (file.length() > MAX_FILE_SIZE) {
                AlertWindow.errorMesage("File size is more than 50MB");
                Logger.writeLog(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                continue;
            }
            // если файл подходящего размера, упаковываем в пакет
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName());
                filePacket = new FilePacket(fileContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.writeLog("Sending packet. Type: " + filePacket.getPacketType());
            // логируем  и отправляем
            mainApp.socketThread.sendPacket(filePacket);
        }

    }

    private String upd;

    // обновлени даты последнего обновления
    public synchronized void lastUpdate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM' at ' HH:mm:ss ");

        upd = lbLastUpd.getText();
        upd += dateFormat.format(System.currentTimeMillis());
        lbLastUpd.setText(upd);
    }

    @FXML
    public void handleDragOver(DragEvent event){
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    public void handleDrop(DragEvent event){
        List <File> list = event.getDragboard().getFiles();
        FileContainer fileContainer = new FileContainer();
        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            if (file.length() > MAX_FILE_SIZE) {
                AlertWindow.errorMesage("File size is more than 50MB");
                Logger.writeLog(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                continue;
            }
            // если файл подходящего размера, упаковываем в пакет
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName());
                filePacket = new FilePacket(fileContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.writeLog("Sending packet. Type: " + filePacket.getPacketType());
            // логируем  и отправляем
            mainApp.socketThread.sendPacket(filePacket);
        }
    }
}
