package ru.geekbrains.filebox.server.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.*;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FileBoxServer implements ServerSocketThreadListener, SocketThreadListener {

    private final String SERVER_INBOX_PATH = "server/inbox/";
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;
    private ServerSocketThread serverSocketThread;
    private final int port = 8189;
    private final Vector<SocketThread> clients = new Vector<>();
    private PrintWriter log;
    private FileWriter logFile;
    private String clientPath = "";

    enum ServerState {WORKING, STOPPED}

    private ServerState state = ServerState.STOPPED;

    public FileBoxServer(FileBoxServerListener eventListener, SQLLoginManager loginManager) {
        this.eventListener = eventListener;
        this.loginManager = loginManager;
    }

    // начало работы сервера. слушаем порт
    public void startListening(int port) {

        if (state != ServerState.WORKING) {
            serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 1000);
            loginManager.init();
            putLog("Server is working");
            state = ServerState.WORKING;
        } else
            putLog("Server is already working.");
    }

    // выключили сервер
    public void stopListening() {

        if (state != ServerState.STOPPED) {
            serverSocketThread.interrupt();
            loginManager.dispose();
            putLog("Server stopped");
            state = ServerState.STOPPED;
        } else {
            putLog("Server is not started");
        }
    }

    // пишем лог в окно сервера и в файл
    public synchronized void putLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        eventListener.onFileBoxServerLog(this, msg);

        // логирую все, что происходит на сервере в файл
        try {
            logFile = new FileWriter("server.log", true);
            log = new PrintWriter((java.io.Writer) logFile);
        } catch (IOException ex) {
            log.printf(msg);
            ex.printStackTrace();
            return;
        }
        try {
            throw new Exception();
        } catch (Exception ex) {
            log.printf(msg + "\n");
            log.flush();
        }
    }

    // методы SSTListener'a
    // запустили сервер
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("SSocket started...");
    }

    // закрыли сервер
    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("SSocket stopped...");
    }

    // приняли входящее соединение
    @Override
    public void onServerSocketThreadReady(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("SSocket is ready...");
    }

    //
    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {

        //putLog("accept()timeout...");
    }
    // методы SocketThreadListener

    @Override
    // установили сединение, создали поток
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ": " + socket.getPort();
        //clients.add(new SocketThread(this, threadName, socket));
        //  new SocketThread(this, threadName, socket);
        new FileBoxSocketThread(this, threadName, socket);
        putLog("socket accepted...");
    }

    // если из потока прилетело исключение
    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception..." + e.getClass().getName());
    }

    // начало соединения логруем
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    //конец соединения. очищаем подлюченных клиентов, логируем
    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog("stopped.");
    }

    // соединение устоновлено, подключенный пользователь добавился в очередь
    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready");
        clients.add(socketThread);
    }

    // если прила строка, не стал удалять метод, тк может переделаю получение пакетов сообщений
    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String msg) {
        putLog("Send " + msg);
    }

    // обрабатываем полученные пакеты
    @Override
    public synchronized void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {
        putLog("Incoming packet type = " + packet.getPacketType());
        // создаем поток клиента, с информацией о нем
        FileBoxSocketThread client = (FileBoxSocketThread) socketThread;
        // если пакет содержит файлы
        if (packet.getPacketType() == PackageType.FILE) {
            // проверяем наличие папки пользователя
            File folder = new File(SERVER_INBOX_PATH + client.getLogin());
            if (!folder.exists()) {
                folder.mkdir();
            }


            // получаем содержимое пакета и записываем в соответствующую логину папку
//            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
//            ArrayList<byte[]> files = filePackage.getFiles();
//            ArrayList<String> names = filePackage.getNames();
            Path path;
            FileContainerSingle fileContainer = (FileContainerSingle) packet.getOutputPacket();
            byte[] file = fileContainer.getFile();
            String name = fileContainer.getName();
            // проверяем есть ли файл на сервере
            // создаем список
            File[] fList;
            fList = folder.listFiles();

            for (int i = 0; i < fList.length; i++) {

                if (fList[i].getName().equals(name)) {
                    MessagePacket msgPkt = new MessagePacket("File '" + name + "'was already uploaded. Delete it, or upload another file");
                    socketThread.sendPacket(msgPkt);
                    return;
                }
            }
            try {

                path = Paths.get(folder.getPath() + "\\" + name);
                Files.write(path, file);
//                    FileOutputStream fos = new FileOutputStream(new File(folder.getPath() + "\\" + names.get(i)));
//                    fos.write(files.get(i));
//                    fos.close();
                putLog(client.getLogin() + " " + "File '" + name + "' received. ");
            } catch (IOException e) {

                e.printStackTrace();
                return;
            }
//            }

            String filesStr = "";
//            for (String fileName : name) {
//                filesStr += "File: " + fileName + "\n";
//            }
            //отправляем пользователю сообщение об успешной загрузки
            filesStr += "upload complete";
          //  sendFileList(socketThread, client);
            MessagePacket msgPkt = new MessagePacket(filesStr);
            // сообщение от пользователя
        } else if (packet.getPacketType() == PackageType.FILE_REQUEST) {
            String fileRequest = (String) packet.getOutputPacket();
            packFiles(fileRequest, client);

        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            putLog(client.getLogin() + " MESSAGE received");

            // пользователь запросил список файлов
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
       //     sendFileList(socketThread, client);
            // сообщение об ощибке
        } else if (packet.getPacketType() == PackageType.ERROR) {
            putLog(client.getLogin() + " " + "ERROR received");
            // пришел пакет с логином и паролем
        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();
            // проверяем не залогинился ли уже пользователь
            if (client.isAuthorized()) {
                handleAuthorizedClient(client);

            } else {
                handleNonAuthorizedClient(client, lc);

            }
            // пакет запрос регистрацию новго пользователя
        } else if (packet.getPacketType() == PackageType.REGISTRATION) {
            RegContainer rc = (RegContainer) packet.getOutputPacket();
            // если логи не заня
            if (!loginManager.isLoginBusy(rc.getLogin())) {
                loginManager.addNewUser(rc.getLogin(), rc.getMail(), rc.getPassword());
                putLog("New user '" + rc.getLogin() + "' resistrated and added to database");
                RegAcceptPacket rap = new RegAcceptPacket(true);
                // если ок, отправляем пользователю пакет одбрящий аутентификацию
                socketThread.sendPacket(rap);
            } else {
                ((FileBoxSocketThread) socketThread).sendError("Login is busy");
            }
        } else if (packet.getPacketType() == PackageType.RENAME) {
            putLog("rename");
            String renameRequest = (String) packet.getOutputPacket();
            String[] rename = renameRequest.split("<>");
            File file = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + rename[0]);
            File newFile = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + rename[1]);
            if (file.renameTo(newFile)) {
                putLog(client.getLogin() + " " + "rename '" + rename[0] + "' to '" + rename[1] + "' complete");
            }
         //   sendFileList(socketThread, client);

            //TODO допилить прием файла
        } else if (packet.getPacketType() == PackageType.FILE_WAITING) {
            // узнаем сколько прилетит фалов
            int filesCount = (Integer) packet.getOutputPacket();
            socketThread.sendPacket(new FileWaitingPacket(1));
            Path path;
            File folder = new File(SERVER_INBOX_PATH + client.getLogin());
            if (!folder.exists()) {
                folder.mkdir();
            }
            for (int i = 0; i < filesCount ; i++) {
                try {
                    int x=0;
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    while ((dis.read())!=-1){
                        baos.write(x);
                    }
                    byte[] file=baos.toByteArray();
                    path = Paths.get(folder.getPath() + "\\" + "filename");
                    Files.write(path, file);
                    baos.close(); dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        } else if (packet.getPacketType() == PackageType.DELETE) {
            putLog("deleting file");
            String deleteRequest = (String) packet.getOutputPacket();
            File file = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + deleteRequest);
            if (file.delete()) {
                putLog(client.getLogin() + " " + "delete file '" + deleteRequest + "' complete");
            }
        //    sendFileList(socketThread, client);

        } else {
            putLog(client.getLogin() + " " + "Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }
        sendFileList(socketThread, client);
    }

    // если клиент уже автроризован
    private void handleAuthorizedClient(FileBoxSocketThread client) {


    }

    // если не авторизован
    private void handleNonAuthorizedClient(FileBoxSocketThread newClient, LoginContainer lc) {
        // проверяем логин и пароль из содержимого полученного пакета
        String login = lc.getLogin();
        boolean isAuth = loginManager.isLoginAndPassCorrect(lc.getLogin(), lc.getPassword());
        // если данные не верны, отправляем пакет ошибки, что логин\пароль не верны
        if (!isAuth) {
            newClient.sendError("Wrong email or password");
            putLog("Wrong mail\\pass '" + lc.getLogin() + "\\" + lc.getPassword() + "'");
            return;
        }
        // если все ок, создаем одноименный с пользователем поток сокета
        FileBoxSocketThread client = getClientByNick(login);
        // авторицзуем и отправляем пользователю сообщение об успешнй аутентификации
        newClient.authorizeAccept(login);
        MessagePacket msg = new MessagePacket("Login accepted");
        newClient.sendPacket(msg);
        if (client == null) {
            System.out.println("client connected");
            putLog("Client " + login + " connected");
        } else {
            putLog("Client " + login + " reconnected.");
            client.reconnect();

        }


    }

    private void sendFileList(SocketThread socketThread, FileBoxSocketThread client) {
        putLog("FILE_LIST request received");
        File clientFolder = new File(SERVER_INBOX_PATH + client.getLogin());
        // создаем списаок
        File[] fList;
        String name;
        //  String len;
        fList = clientFolder.listFiles();
        ///ArrayList<String> fileList = new ArrayList<>();
        FileListContainer fc = new FileListContainer();
        for (int i = 0; i < fList.length; i++) {
            //Нужны только папки в место isFile() пишим isDirectory()
            if (fList[i].isFile()) {
//                    info(String.valueOf(i) + " - " + fList[i].getName());
                //     fileList.add(fList[i].getName());
//                /name = fList[i].getName();
//                long len = fList[i].length()/1024;
                FileListElement element = new FileListElement(fList[i].getName(), fList[i].length() / 1024);
                fc.add(element);
            }
        }
        //отправляем список
        FileListPacket fileListRequest = new FileListPacket(fc);
        socketThread.sendPacket(fileListRequest);
    }

    // ищем поток клиента по нику
    public FileBoxSocketThread getClientByNick(String nickname) {
        final int cnt = clients.size();

        for (int i = 0; i < cnt; i++) {
            FileBoxSocketThread client = (FileBoxSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getLogin().equals(nickname)) return client;

        }
        return null;
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }

    public synchronized void sendFile() {

        // выбираем файл
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainer fileContainer = new FileContainer();

        // упаковываем в контейнер  и отправляем
        //   packFiles(list, fileContainer);

    }

    void packFiles(String fileName, FileBoxSocketThread client) {
        FilePacket filePacket = null;
        // FileContainer fileContainer = new FileContainer();
        FileContainerSingle fileContainer = new FileContainerSingle();
        File file = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + fileName);
        // ArrayList<File> list = new ArrayList<>();

        // подсчитываем кол-во файлов и проверяем размер каждого файла
//        if (list.size() > 0) {
//            for (int i = 0; i < list.size(); i++) {
//                File file = list.get(i);
//                if (file.length() > MAX_FILE_SIZE) {
//                    AlertWindow.errorMesage("File size is more than 50MB");
//                    Logger.writeLog(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
//                    continue;
//                }
        // если файл подходящего размера, упаковываем в пакет
        try {
            fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName(), file.length(), 1);
            filePacket = new FilePacket(fileContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        putLog("Sending packet. Type: " + filePacket.getPacketType());
        // логируем  и отправляем
        client.sendPacket(filePacket);
//            }
//        }
    }
}
