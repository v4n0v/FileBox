package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ClientController implements SocketThreadListener, Thread.UncaughtExceptionHandler {
    enum State {CONNECTED, NOT_CONNECTED, ERROR, LOGIN, REGISTRATION, REGISTERED}

    public State state = State.NOT_CONNECTED;

    private final static String IP = "localhost";
    private boolean isRegistrated;
    private String errorMsg;
    private final static int MIN_PASS_LENGTH = 2;
    private final static int MAX_PASS_LENGTH = 32;
    private boolean isAuthorized;
    private final static int PORT = 8189;
    private FileWriter logFile;
    private PrintWriter log;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    public String login;
    public String password;

    private final static long MAX_FILE_SIZE = 5_242_880;

    public void setSocketThread(SocketThread socketThread) {
        this.socketThread = socketThread;
    }

    private SocketThread socketThread;
    private Socket socket;

    private FilePacket filePacket;
    private MessagePacket messagePacket;

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


    private FileBoxClientStart mainApp;

    public void setMainApp(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
        //     this.socketThread=mainApp.getSocketThread();
    }
    // общий метод создания инфоокна
    private void alertWindow(String title, String msg, Alert.AlertType type) {
        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    // инфоокно об ошибке
    private void errorMesage(String msg) {
        alertWindow("Error", msg, Alert.AlertType.ERROR);
    }
    // инфоокно с сообщение
    private void infoMesage(String msg) {
        alertWindow("Info", msg, Alert.AlertType.INFORMATION);
    }
    // добавляем пользователя
    public void addUser() {
        String pass2Reg;
        loginReg = loginRegField.getText();
        mailReg = mailRegField.getText();
        pass1Reg = pass1RegField.getText();
        pass2Reg = pass2RegField.getText();
        if (loginReg.isEmpty() && mailReg.isEmpty()
                && pass1Reg.isEmpty() && pass2Reg.isEmpty()) {
            errorMesage("Fill the all fields");
        } else if (pass1Reg.length() < MIN_PASS_LENGTH || pass1Reg.length() > MAX_PASS_LENGTH) {
            errorMesage("Password must be from" + MIN_PASS_LENGTH + " to " + MAX_PASS_LENGTH + " words.");
        } else {
            if (pass1Reg.equals(pass2Reg)) {
                state = State.REGISTRATION;
                connect();
            } else {
                errorMesage("Password fields are not equals");
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

//        stage.hide();
        stage.close();
    }

    public void loginShow() {
        initClientLoginLayout();
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
    // ссылка на элемент модального окна дляполучения ссылки на общий элемент класса mainApp
    private VBox loginRootElement;


    /// инициализация модального окна логина, в которое передается ссылка на mainApp
    public void initClientLoginLayout() {
        try {
            // новое окно логина
            Stage stage = new Stage();
//        Parent modal = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));

            FXMLLoader loaderLog = new FXMLLoader();
            loaderLog.setLocation(FileBoxClientStart.class.getResource("fxml/login_modal.fxml"));
            loginRootElement = (VBox) loaderLog.load();
            ClientController controller = loaderLog.getController();
            // получаем ссылку у контроллера окна
            controller.setMainApp(mainApp);
            stage.setTitle("Login");
            stage.setOnCloseRequest((event) -> event.consume());

            stage.setResizable(false);
            Scene sceneLog = new Scene(loginRootElement);
            stage.setScene(sceneLog);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnRename.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // установка соединения с сервером
    public void connect() {
        try {
            Socket socket = new Socket(IP, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
            // устанавливаем в mainApp ссылку на полученный поток сокета
            mainApp.setSocketThread(socketThread);
        } catch (IOException e) {
            e.printStackTrace();
            errorMesage(e.getMessage());
            writeLog("Exception: " + e.getMessage() + "\n");
        }

    }

    // закрытие соединения
    private void disconect() {

        mainApp.getSocketThread().close();
    }

    // меоды обработки нажатия на кнопку
    public void renameFile() {
        System.out.println("rename");
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
        disconect();
        writeLog("Client LogOut");

        loginShow();
    }


    // оправка логниа пароля для аутентификации
    public void loginToFileBox() {
        // если поля не пусты
        if (!fieldLogin.getText().isEmpty() || !fieldLogin.getText().isEmpty()) {
            login = fieldLogin.getText();
            password = fieldPass.getText();

            // меняем статус скиента и соединяемся
            state = State.LOGIN;
            connect();

        } else {

            alertWindow("Warning", "Fill mail and password fields", Alert.AlertType.WARNING);
        }

    }

    // отправка файла
    public void sendFile() {
        // получаем ссылку на сокет
        socketThread = mainApp.getSocketThread();
        // выбираем файл
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainer fileContainer = new FileContainer();

        // подсчитываем кол-во файлов и проверяем размер каждого файла
        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            if (file.length() > MAX_FILE_SIZE) {
                errorMesage("File size is more than 50MB");
                writeLog(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                continue;
            }
            // если файл подходящего размера, упаковываем в пакер
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName());
                filePacket = new FilePacket(fileContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeLog("Sending packet. Type: " + filePacket.getPacketType());
            // логируем  и отправляем
            socketThread.sendPacket(filePacket);
        }

    }

    // логирование событий в log файл
    private void writeLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) + msg;
        try {
            logFile = new FileWriter("client.log", true);
            log = new PrintWriter((java.io.Writer) logFile);
        } catch (IOException e) {
            log.printf(msg);
            e.printStackTrace();
            return;
        }
        try {
            throw new Exception();
        } catch (Exception ex) {
            log.printf(msg + "\n");
            log.flush();
        }

    }

    // оверайдим обработчик исключений
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String msg;
        if (stackTraceElements.length == 0) {
            msg = "Empty StackTrace";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        errorMesage(msg);

        writeLog("Exception: " + msg + "\n");
        System.exit(1);
    }

    // методы интерфейса SocketThread
    @Override
    // что просходит в клиенте, при соединениии
    // начали соединение
    public void onStartSocketThread(SocketThread socketThread) {
        writeLog(
                "Socket started");
    }

    // соединение закончено
    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        isAuthorized = false;
        writeLog("Socket closed. End of session");

    }

    // соединение установлено
    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        writeLog("Connection to server complete!");
        if (state == State.REGISTRATION) {
            AddUserPacket addUserPacket = new AddUserPacket(loginReg, mailReg, pass1Reg);
            socketThread.sendPacket(addUserPacket);
        } else {

            LoginPacket loginPacket = new LoginPacket(login, password);
            socketThread.sendPacket(loginPacket);
        }
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    // получили пакет c сервера
    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {
        handlePacket(packet);
        writeLog("Packet " + packet.getPacketType() + " was handled...");

    }

    // прилетело исключение
    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        Platform.runLater(() -> errorMesage(e.getMessage()));
        writeLog("Exception: " + e.getMessage());
    }

    // обрабатываем полученный пакет
    private void handlePacket(Packet packet) {
//        PackageType type = packet.getPacketType();
//        switch (type) {
//            case PackageType.LOGIN:
//                break;
//        }
        // если в полученном пакете файл
        if (packet.getPacketType() == PackageType.FILE) {
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            //получили список имен и файлов и записали их на диск
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            for (int i = 0; i < files.size(); i++) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(names.get(i)));
                    fos.write(files.get(i));
                    fos.close();
                    writeLog("File '" + names.get(i) + "' received. ");
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }
            // если получили сообщение, отрыли информационное окно
        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            String msg = (String) packet.getOutputPacket();
            Platform.runLater(() -> infoMesage(msg));
            writeLog("MESSAGE received");

            // получили список файлов в "облаке"
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            ArrayList<String> fileList = (ArrayList<String>) packet.getOutputPacket();
            //      Platform.runLater(() -> lastUpdate());
            writeLog("FILE_LIST received");
            // прилетела ошибка на сервере, открыли окно об ошибкке
        } else if (packet.getPacketType() == PackageType.ERROR) {
            errorMsg = (String) packet.getOutputPacket();
            state = State.ERROR;
            writeLog(errorMsg);
            Platform.runLater(() -> errorMesage(errorMsg));
        // такое сообщение не должно придти
        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();
         //сервер одбрил логин и парроль
        } else if (packet.getPacketType() == PackageType.AUTH_ACCEPT) {
            isAuthorized = (Boolean) packet.getOutputPacket();
            if (isAuthorized) {
                state = State.CONNECTED;

                Platform.runLater(() -> loginHide());
                FileListPacket fileListRequest = new FileListPacket(null);
                socketThread.sendPacket(fileListRequest);

            }
            // зарегистрировали нового пользователя
        } else if (packet.getPacketType() == PackageType.REG_ACCEPT) {
            isRegistrated = (Boolean) packet.getOutputPacket();
            Platform.runLater(() -> infoMesage("User " + loginReg + " successfully registered in FileBox"));
            Platform.runLater(() -> regExit());
            state = State.REGISTERED;
            disconect();
            state = State.NOT_CONNECTED;
//            Stage stage = (Stage) reg.getScene().getWindow();
//            stage.showAndWait();

        } else {
            writeLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }

    }

    // обновлени даты последнего обновления
    private synchronized void lastUpdate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM' at ' HH:mm:ss ");

        String upd = lbLastUpd.getText();
        upd += dateFormat.format(System.currentTimeMillis());
        lbLastUpd.setText(upd);
    }
}
