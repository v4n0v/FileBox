package ru.geekbrains.filebox.client.core;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.client.fxcontrollers.ClientController;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.FileType;
import ru.geekbrains.filebox.library.Log2File;
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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnectionManager implements SocketThreadListener, Thread.UncaughtExceptionHandler {

    private final Logger logger = Logger.getLogger("Filebox.ClientManager");

    public State state = State.NOT_CONNECTED;
    private final static String IP = "localhost";
    private String errorMsg;
    private boolean isAuthorized;
    private final static int PORT = 8189;
    FileBoxClientStart mainApp;
    //   private String CLIENT_INBOX_PATH;
    private SocketThread socketThread;


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

    public ClientConnectionManager(FileBoxClientStart mainApp) {
        this.mainApp = mainApp;
    }


    public void connect() {
        try {
            Socket socket = new Socket(IP, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
            // устанавливаем в mainApp ссылку на полученный поток сокета
            mainApp.setSocketThread(socketThread);
            //   logger.info("Client connected");
            Log2File.writeLog("Client connected");
        } catch (IOException e) {
            e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());
            //        logger.warning("Exception: " + e.getMessage());
            Log2File.writeLog(Level.WARNING, "Exception: " + e.getMessage());
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
        //  logger.warning("Exception: " + msg);
        Log2File.writeLog(Level.WARNING, "Exception: " + msg + "\n");
        System.exit(1);
    }
    // методы интерфейса SocketThread

    @Override
    // что просходит в клиенте, при соединениии
    // начали соединение
    public void onStartSocketThread(SocketThread socketThread) {
        Log2File.writeLog(
                "Socket started");
        //  logger.info("Socket started");
    }

    // соединение закончено
    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        isAuthorized = false;
        Log2File.writeLog("Socket closed. End of session");
        //  logger.info("Socket closed. End of session");
    }

    // соединение установлено
    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        Log2File.writeLog("Connection to server complete!");
        if (state == State.REGISTRATION) {
            AddUserPacket addUserPacket = new AddUserPacket(loginReg, mailReg, pass1Reg);
            mainApp.socketThread.sendPacket(addUserPacket);
        } else {

            LoginPacket loginPacket = new LoginPacket(login, password);
            mainApp.socketThread.sendPacket(loginPacket);
        }
        //   logger.info("Connection to server complete!");
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    // получили пакет c сервера
    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {
        handlePacket(packet);
        Log2File.writeLog("Packet " + packet.getPacketType() + " was handled...");

    }

    // прилетело исключение
    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        Platform.runLater(() -> AlertWindow.errorMesage(e.getMessage()));
        Log2File.writeLog(Level.WARNING, "Exception: " + e.getMessage());
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
            Log2File.writeLog(Level.WARNING, "Exception: Unknown package type :(");
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
        //  System.out.println(clientController.lbLastUpd);
        clientController.lbLastUpd.setText("Last upd " + upd);
    }


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
            Log2File.writeLog("File '" + name + "' received into " + mainApp.getConfig().getPath());
        } catch (IOException e) {

            e.printStackTrace();
            return;
        }

    }

    private void handleMessagePacket(Packet packet) {
        String msg = (String) packet.getOutputPacket();
        Platform.runLater(() -> AlertWindow.infoMesage(msg));
        Log2File.writeLog("MESSAGE received '" + msg + "'");

        // получили список файлов в "облаке"
    }

    private void handeFileListPacket(Packet packet) {
        //   ArrayList<FileListElement> fileList = (ArrayList<FileListElement>) packet.getOutputPacket();
        FileListContainer fc = (FileListContainer) packet.getOutputPacket();

        ArrayList<FileListElement> flist = (ArrayList<FileListElement>) fc.getList();
        ObservableList<FileListXMLElement> fXMLlist = FXCollections.observableArrayList();

        for (int i = 0; i < flist.size(); i++) {
            fXMLlist.add(new FileListXMLElement(flist.get(i).getFileName(),
                    flist.get(i).getFileSize(), flist.get(i).getType()));
        }
        //  mainApp.fillFileList(flist);
        //  fXMLlist.sorted();
//
//
//        System.out.println(mainApp.getConfig().getPath());
//        System.out.println(mainApp.getRootPath());
//        if (!mainApp.getConfig().getPath().equals(mainApp.getRootPath()))
//            fXMLlist.add(new FileListXMLElement("...", 0l, FileType.UP_DIR));


        Collections.sort(fXMLlist, FileListXMLElement.FileNameComparator);
        updateList(fXMLlist, mainApp.getServerFileList());
        //    mainApp.setServerFileList(fXMLlist);
        //handleSaveAs();
        usedSpace = fc.getUsedSpace();

        // таблица клиента будет обновляться только если мы находимся в дуфолтном каталоге.
        // в окне синхронизации свое обновление, чтоб не было конфликта обновления


        updateClientFileList(mainApp.getConfig().getPath(), mainApp.getClientFileList());
        Platform.runLater(() -> {
            lastUpdate();
            clientController.setFreeSpaceLabel(FRASPACE_TOTAL * 1024 - usedSpace / 1024, FRASPACE_TOTAL * 1024);
        });

        Log2File.writeLog("FILE_LIST received");
    }

    private void updateClientFileList(String path, ObservableList<FileListXMLElement> fileList) {
        // создаем список
        File clientFolder = new File(path);
        File[] fList = clientFolder.listFiles();
        fileList.clear();
//        for (int i = 0; i < fList.size(); i++) {
//            fileList.add(new FileListXMLElement(fList.get(i).getFileName(), fList.get(i).getFileSize()));
//        }
        // если клиент не в дефолтной своей папке, то добавляем флаг UP_DIR
        if (!mainApp.getConfig().getPath().equals(mainApp.getRootPath()))
            fileList.add(new FileListXMLElement("...", 0l, FileType.UP_DIR));


        for (int i = 0; i < fList.length; i++) {
            //Нужны только папки в место isFile() пишим isDirectory()
            if (fList[i].isFile()) {
                fileList.add(new FileListXMLElement(fList[i].getName(), fList[i].length(), FileType.FILE));
            }
            if (fList[i].isDirectory()) {
                fileList.add(new FileListXMLElement("[" + fList[i].getName() + "]", fList[i].length(), FileType.DIR));
            }
        }


    }

    private void handleRegAcceptPacket(Packet packet) {
        boolean isRegistrated = (Boolean) packet.getOutputPacket();
        if (isRegistrated) {
            Platform.runLater(() -> AlertWindow.infoMesage("User " + loginReg + " successfully registered in FileBox"));
            Platform.runLater(() -> regExit());
            state = State.REGISTERED;
            disconnect();
            state = State.NOT_CONNECTED;
            Platform.runLater(() -> mainApp.regExit());
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

            Platform.runLater(() -> {
                clientController.loginHide();
                clientController.setLoggedInfoLabel(login);
            });
            Log2File.writeLog("Authorization complete");
            //  FileListPacket fileListRequest = new FileListPacket(null);
            //   mainApp.socketThread.sendPacket(fileListRequest);

        } else {
            Log2File.writeLog(Level.WARNING, "Authorization error");
        }
    }

    private void handleErrorPacket(Packet packet) {
        errorMsg = (String) packet.getOutputPacket();
        state = State.ERROR;
        Log2File.writeLog(Level.WARNING, errorMsg);
        Platform.runLater(() -> AlertWindow.errorMesage(errorMsg));
    }
}
