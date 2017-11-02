package ru.geekbrains.filebox.client.core;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.client.fxcontrollers.ClientController;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Logger;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileListContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;
import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileBoxClientManager implements SocketThreadListener, Thread.UncaughtExceptionHandler {

    public State state = State.NOT_CONNECTED;
    private final static String IP = "localhost";
    private String errorMsg;
    private boolean isAuthorized;
    private final static int PORT = 8189;

    SocketThread socketThread;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String login;
    private String password;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    private ClientController clientController;

    public void setLoginReg(String loginReg) {
        this.loginReg = loginReg;
    }

    public void setMailReg(String mailReg) {
        this.mailReg = mailReg;
    }

    public void setPass1Reg(String pass1Reg) {
        this.pass1Reg = pass1Reg;
    }

    public void setRegistrationInfo(String loginReg, String mailReg, String pass1Reg) {
        this.pass1Reg = pass1Reg;
        this.mailReg = mailReg;
        this.loginReg = loginReg;
    }

    private String loginReg;
    private String mailReg;
    private String pass1Reg;
    private final static long MAX_FILE_SIZE = 5_242_880;

    FileBoxClientStart mainApp;

    public void setMainApp(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
        //     this.socketThread=mainApp.getSocketThread();
    }

    public FileBoxClientManager(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
    }


    public void connect() {
        try {
            Socket socket = new Socket(IP, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
            // устанавливаем в mainApp ссылку на полученный поток сокета
            mainApp.setSocketThread(socketThread);
        } catch (IOException e) {
            e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());

            Logger.writeLog("Exception: " + e.getMessage() + "\n");
        }

    }

    public void disconnect() {
        mainApp.socketThread.close();
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
        // AlertWindow.errorMesage(msg);

        Logger.writeLog("Exception: " + msg + "\n");
        System.exit(1);
    }
    // методы интерфейса SocketThread

    @Override
    // что просходит в клиенте, при соединениии
    // начали соединение
    public void onStartSocketThread(SocketThread socketThread) {
        Logger.writeLog(
                "Socket started");
    }

    // соединение закончено
    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        isAuthorized = false;
        Logger.writeLog("Socket closed. End of session");
    }

    // соединение установлено
    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        Logger.writeLog("Connection to server complete!");
        if (state == State.REGISTRATION) {
            AddUserPacket addUserPacket = new AddUserPacket(loginReg, mailReg, pass1Reg);
            mainApp.socketThread.sendPacket(addUserPacket);
        } else {

            LoginPacket loginPacket = new LoginPacket(login, password);
            mainApp.socketThread.sendPacket(loginPacket);
        }
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    // получили пакет c сервера
    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {
        handlePacket(packet);
        Logger.writeLog("Packet " + packet.getPacketType() + " was handled...");
    }

    // прилетело исключение
    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        Platform.runLater(() -> AlertWindow.errorMesage(e.getMessage()));
        Logger.writeLog("Exception: " + e.getMessage());
    }

    // обрабатываем полученный пакет

    private String CLIENT_INBOX_PATH = "C:/FileBoxFolder/";
    private void handlePacket(Packet packet) {

        // если в полученном пакете файл
        if (packet.getPacketType() == PackageType.FILE) {
            File folder = new File(CLIENT_INBOX_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }


            // получаем содержимое пакета и записываем в соответствующую логину папку
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            Path path;

            // проверяем есть ли файл на сервере
            // создаем список
            File[] fList;
            fList = folder.listFiles();

            for (int i = 0; i < files.size(); i++) {
//                for (int j = 0; j < fList.length; j++) {
//                    String s= fList[j].getName();
//                    String s1 = names.get(i);
//                    if (fList[j].getName().equals(names.get(i))){
//                        MessagePacket msgPkt = new MessagePacket("File '"+names.get(i)+"'was already uploaded. Delete it, or upload another file");
//                        socketThread.sendPacket(msgPkt);
//                        return;
//                    }
//                }
                try {
                    path = Paths.get(folder.getPath() + "\\" + names.get(i));
                    Files.write(path, files.get(i));
//                    FileOutputStream fos = new FileOutputStream(new File(folder.getPath() + "\\" + names.get(i)));
//                    fos.write(files.get(i));
//                    fos.close();
                    Logger.writeLog("File '" + names.get(i) + "' received. ");
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }
            // если получили сообщение, отрыли информационное окно
        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            String msg = (String) packet.getOutputPacket();
            Platform.runLater(() -> AlertWindow.infoMesage(msg));
            Logger.writeLog("MESSAGE received");

            // получили список файлов в "облаке"
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            //   ArrayList<FileListElement> fileList = (ArrayList<FileListElement>) packet.getOutputPacket();
            FileListContainer fc = (FileListContainer) packet.getOutputPacket();

            ArrayList<FileListElement> flist = (ArrayList<FileListElement>) fc.getList();
            ObservableList<FileListXMLElement> fXMLlist = FXCollections.observableArrayList();

            for (int i = 0; i < flist.size(); i++) {
                fXMLlist.add(new FileListXMLElement(flist.get(i).getFileName(), flist.get(i).getFileSize()));
            }
            mainApp.fillFileList(flist);
            updateList(fXMLlist, mainApp.fileListDataProp);
            //    mainApp.setFileListDataProp(fXMLlist);
            handleSaveAs();

//            Platform.runLater(()->{
//////////                clientController.lastUpdate();
//              clientController.initTable();
//            });

            Logger.writeLog("FILE_LIST received");
            // прилетела ошибка на сервере, открыли окно об ошибкке
        } else if (packet.getPacketType() == PackageType.ERROR) {
            errorMsg = (String) packet.getOutputPacket();
            state = State.ERROR;
            Logger.writeLog(errorMsg);
            Platform.runLater(() -> AlertWindow.errorMesage(errorMsg));
            // такое сообщение не должно придти
        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();
            //сервер одбрил логин и парроль
        } else if (packet.getPacketType() == PackageType.AUTH_ACCEPT) {
            isAuthorized = (Boolean) packet.getOutputPacket();
            if (isAuthorized) {
                state = State.CONNECTED;
                //clientController.loginHide();
                Platform.runLater(() -> clientController.loginHide());
                FileListPacket fileListRequest = new FileListPacket(null);
                mainApp.socketThread.sendPacket(fileListRequest);

            }
            // зарегистрировали нового пользователя
        } else if (packet.getPacketType() == PackageType.REG_ACCEPT) {
            // isRegistrated = (Boolean) packet.getOutputPacket();
//            Platform.runLater(() -> infoMesage("User " + loginReg + " successfully registered in FileBox"));
//            Platform.runLater(() -> regExit());
            state = State.REGISTERED;
            disconnect();
            state = State.NOT_CONNECTED;
//            Stage stage = (Stage) reg.getScene().getWindow();
//            stage.showAndWait();

        } else {
            Logger.writeLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }

    }

    public void updateList(ObservableList<FileListXMLElement> newFileList,
                           ObservableList<FileListXMLElement> currentFileList) {


        // обнуляем и заполняем список файлов
        currentFileList.clear();
        if (currentFileList.size()==0)
            currentFileList.addAll(newFileList);

//        FileListXMLElement elementNew;
//        FileListXMLElement elementCurrent;
        // устанавливаем счетчик в нулевое значение
//        int count = 0;
//        // если текущий список пуст, добавляем все элементы
//        if (currentFileList.size() == 0) {
////            for (int i = 0; i < newFileList.size(); i++) {
////                elementNew = newFileList.get(i);
////                currentFileList.add(elementNew);
////            }
//            currentFileList.addAll(newFileList);
//        } else {
//            // проверяем наличие новых файлов, добавляем если таковые имеются
//            // берем элемент из новго листа и сравниваем со старыми
//            for (int i = 0; i < newFileList.size(); i++) {
//                // берем жлемент новго списка
//                elementNew = newFileList.get(i);
//                // сранвиваем поочередно с элементами старого
//                for (int j = 0; j < currentFileList.size(); j++) {
//                    elementCurrent = currentFileList.get(j);
//                    // если элементы не равно инкемент счетчика
//                    if (!elementNew.getFileName().getValue().equals(elementCurrent.getFileName().getValue())) {
//                        count++;
//                    } else {
//                        break;
//                    }
//                    // если счетчик равен кол-ву элементов, это значит, что такого элемента нет, добавляем его в список
//                    if (count == currentFileList.size()) {
//                        currentFileList.add(elementNew);
//                        // обнуляем список
//                        count = 0;
//                    }
//                }
//            }
//
//            // проверяем, наличие удаленных на сервере файлов
//            // берем элемент из текущего листа и сравниваем с новым листом
//            count = 0;
//            for (int i = 0; i < currentFileList.size(); i++) {
//                elementCurrent = currentFileList.get(i);
//                for (int j = 0; j < newFileList.size(); j++) {
//                    elementNew = newFileList.get(j);
//
//                    // плюсуем счетчик, если такого элемента нет
//                    if (!elementNew.getFileName().getValue().equals(elementCurrent.getFileName().getValue())) {
//                        count++;
//                        // если такой файл есть, проверяем следующий
//                    } else {
//                        break;
//                    }
//                    if (count == newFileList.size()) {
//                        currentFileList.remove(i);
//                        count=0;
//                    }
//                }
//
//            }
//        }
    }

    public void handleSaveAs() {

        File file = new File("fblist");

        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveFileListDataToFile(file);
        }
    }

}
