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
import ru.geekbrains.filebox.network.packet.packet_container.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileBoxClientManager implements SocketThreadListener, Thread.UncaughtExceptionHandler {

    public State state = State.NOT_CONNECTED;
    private final static String IP = "localhost";
    private String errorMsg;
    private boolean isAuthorized;
    private final static int PORT = 8189;
    FileBoxClientStart mainApp;
    //   private String CLIENT_INBOX_PATH;
    private SocketThread socketThread;

//    public void setListOutFiles(List<File> listOutFiles) {
//        this.listOutFiles = listOutFiles;
//    }

    private List<File> listOutFiles;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String login;
    private String password;

    private int FRASPACE_TOTAL = 10;
    private int usedSpace;



    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    private ClientController clientController;

//    public void setLoginReg(String loginReg) {
//        this.loginReg = loginReg;
//    }
//
//    public void setMailReg(String mailReg) {
//        this.mailReg = mailReg;
//    }
//
//    public void setPass1Reg(String pass1Reg) {
//        this.pass1Reg = pass1Reg;
//    }

    public void setRegistrationInfo(String loginReg, String mailReg, String pass1Reg) {
        this.pass1Reg = pass1Reg;
        this.mailReg = mailReg;
        this.loginReg = loginReg;
    }

    private String loginReg;
    private String mailReg;
    private String pass1Reg;
    private final static long MAX_FILE_SIZE = 5_242_880;


//    public void setMainApp(FileBoxClientStart mainApp) {
//        this.mainApp = mainApp;
//        //     this.socketThread=mainApp.getSocketThread();
//    }

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

    private void handlePacket(Packet packet) {

        // если в полученном пакете файл
        if (packet.getPacketType() == PackageType.FILE) {
            handleFilePacket(packet);
            //         }
            // если получили сообщение, отрыли информационное окно
        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            handleMessagePacket(packet);
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            handeFileListPacket(packet);
            // прилетела ошибка на сервере, открыли окно об ошибкке
        } else if (packet.getPacketType() == PackageType.ERROR) {
            handleErrorPacket(packet);
            // такое сообщение не должно придти
        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();
            //сервер одбрил логин и парроль
        } else if (packet.getPacketType() == PackageType.AUTH_ACCEPT) {
            handleAuthPacket(packet);
            // зарегистрировали нового пользователя
        } else if (packet.getPacketType() == PackageType.REG_ACCEPT) {
            handleRegAcceptPacket(packet);
        } else if (packet.getPacketType() == PackageType.FILE_WAITING) {
            sendFileBytes(listOutFiles);
        } else {
            Logger.writeLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }

    }


    public void updateList(ObservableList<FileListXMLElement> newFileList,
                           ObservableList<FileListXMLElement> currentFileList) {

        // обнуляем и заполняем список файлов
        currentFileList.clear();
        if (currentFileList.size() == 0)
            currentFileList.addAll(newFileList);

    }

    public synchronized void lastUpdate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM' at ' HH:mm:ss ");

        // upd = lbLastUpd.getText();
        String upd = dateFormat.format(System.currentTimeMillis());
        System.out.println(clientController.lbLastUpd);
        clientController.lbLastUpd.setText("Last upd " + upd);
    }

    //    public void handleSaveAs() {
//
//        File file = new File("fblist");
//
//        if (file != null) {
//            // Make sure it has the correct extension
//            if (!file.getPath().endsWith(".xml")) {
//                file = new File(file.getPath() + ".xml");
//            }
//            mainApp.saveFileListDataToFile(file);
//        }
//    }
    // TODO зафигачить побайтовую передачу
    public void sendFileBytes(List<File> list) {
        int countFiles = list.size();
        Socket socket = mainApp.getSocketThread().getSocket();

        DataOutputStream outD;
        try {
            outD = new DataOutputStream(socket.getOutputStream());

            // outD.writeInt(countFiles);//отсылаем количество файлов

            for (int i = 0; i < list.size(); i++) {
                File f = list.get(i);
                // сохраняю файл в массив байт
                byte[] bytes = Files.readAllBytes(Paths.get(f.getPath()));

                // создаю поток байт
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // пишу в него сохраненнный массив
                baos.write(bytes);

                outD.flush();
                baos.close();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleFilePacket(Packet packet) {
        File folder = new File(mainApp.getConfig().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }


        // получаем содержимое пакета и записываем в соответствующую логину папку
//            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
//            ArrayList<byte[]> files = filePackage.getFile();
//            ArrayList<String> names = filePackage.getName();
        Path path;

        FileContainerSingle fileContainer = (FileContainerSingle) packet.getOutputPacket();
        byte[] file = fileContainer.getFile();
        String name = fileContainer.getName();

        // проверяем есть ли файл на сервере
        // создаем список
        File[] fList;
        fList = folder.listFiles();

//            for (int i = 0; i < files.size(); i++) {
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
            path = Paths.get(folder.getPath() + "\\" + name);
            Files.write(path, file);
//                    FileOutputStream fos = new FileOutputStream(new File(folder.getPath() + "\\" + names.get(i)));
//                    fos.write(files.get(i));
//                    fos.close();
            Logger.writeLog("File '" + name + "' received. ");
        } catch (IOException e) {

            e.printStackTrace();
            return;
        }

    }

    private void handleMessagePacket(Packet packet) {
        String msg = (String) packet.getOutputPacket();
        Platform.runLater(() -> AlertWindow.infoMesage(msg));
        Logger.writeLog("MESSAGE received");

        // получили список файлов в "облаке"
    }

    private void handeFileListPacket(Packet packet) {
        //   ArrayList<FileListElement> fileList = (ArrayList<FileListElement>) packet.getOutputPacket();
        FileListContainer fc = (FileListContainer) packet.getOutputPacket();

        ArrayList<FileListElement> flist = (ArrayList<FileListElement>) fc.getList();
        ObservableList<FileListXMLElement> fXMLlist = FXCollections.observableArrayList();

        for (int i = 0; i < flist.size(); i++) {
            fXMLlist.add(new FileListXMLElement(flist.get(i).getFileName(), flist.get(i).getFileSize()));
        }
        //  mainApp.fillFileList(flist);
        updateList(fXMLlist, mainApp.getServerFileList());
        //    mainApp.setServerFileList(fXMLlist);
        //handleSaveAs();
        usedSpace =fc.getUsedSpace();
        updateClientFileList(mainApp.getConfig().getPath(), mainApp.getClientFileList());
        Platform.runLater(() -> {
            lastUpdate();
            clientController.setFreeSpaceLabel(FRASPACE_TOTAL*1024-usedSpace /1024, FRASPACE_TOTAL*1024);
        });

        Logger.writeLog("FILE_LIST received");
    }

    private void updateClientFileList(String path, ObservableList<FileListXMLElement> fileList) {


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
                String name = fList[i].getName();
                long len = fList[i].length();
                fileList.add(new FileListXMLElement(fList[i].getName(), fList[i].length()));
            }
        }


    }

    private void handleRegAcceptPacket(Packet packet) {
       boolean  isRegistrated = (Boolean) packet.getOutputPacket();
       if (isRegistrated) {
           Platform.runLater(() -> AlertWindow.infoMesage("User " + loginReg + " successfully registered in FileBox"));
           Platform.runLater(() -> regExit());
           state = State.REGISTERED;
           disconnect();
           state = State.NOT_CONNECTED;
           Platform.runLater(() ->  mainApp.regExit());
       } else {
           Platform.runLater(() -> AlertWindow.errorMesage("User " + loginReg + " registration error"));
       }
    }

    private void regExit() {
    }

    private void handleAuthPacket(Packet packet) {
        isAuthorized = (Boolean) packet.getOutputPacket();
        if (isAuthorized) {
            state = State.CONNECTED;
            //clientController.loginHide();
            Platform.runLater(() -> {clientController.loginHide();
                clientController.setLoggedInfoLabel(login);
            });

            FileListPacket fileListRequest = new FileListPacket(null);
            mainApp.socketThread.sendPacket(fileListRequest);

        }
    }

    private void handleErrorPacket(Packet packet) {
        errorMsg = (String) packet.getOutputPacket();
        state = State.ERROR;
        Logger.writeLog(errorMsg);
        Platform.runLater(() -> AlertWindow.errorMesage(errorMsg));
    }
}
