package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;
import ru.geekbrains.filebox.network.packet.packet_container.RegContainer;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            for (int i = 0; i < files.size(); i++) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(folder.getPath() + "\\" + names.get(i)));
                    fos.write(files.get(i));
                    fos.close();
                    putLog("File '" + names.get(i) + "' received. ");
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }

            String filesStr = "";
            for (String fileName : names) {
                filesStr += "File: " + fileName + "\n";
            }
            //отправляем пользователю сообщение об успешной загрузки
            filesStr += "upload complete";
            MessagePacket msgPkt = new MessagePacket(filesStr);
            // сообщение от пользователя
        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            putLog("MESSAGE received");

            // пользователь запросил список файлов
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            putLog("FILE_LIST request received");
            File clientFolder = new File(SERVER_INBOX_PATH + client.getLogin());
            // создаем списаок
            File[] fList;
            fList = clientFolder.listFiles();
            ArrayList<String> fileList = new ArrayList<>();
            for (int i = 0; i < fList.length; i++) {
                //Нужны только папки в место isFile() пишим isDirectory()
                if (fList[i].isFile())
//                    info(String.valueOf(i) + " - " + fList[i].getName());
                    fileList.add(fList[i].getName());
            }
            //отправляем список
            FileListPacket fileListRequest = new FileListPacket(fileList);
            socketThread.sendPacket(fileListRequest);

            // сообщение об ощибке
        } else if (packet.getPacketType() == PackageType.ERROR) {
            putLog("ERROR received");
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
        } else {
            putLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }
    }
    // если клиент уже автроризован
    private void handleAuthorizedClient(FileBoxSocketThread client) {


    }
    // если не авторизован
    private void handleNonAuthorizedClient(FileBoxSocketThread newClient, LoginContainer lc) {
        // проверяем логин и пароль
        String login = lc.getLogin();
        boolean isAuth = loginManager.checkLogin(lc.getLogin(), lc.getPassword());
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

}
