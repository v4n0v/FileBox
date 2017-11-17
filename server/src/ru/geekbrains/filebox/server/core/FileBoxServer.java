package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.library.FileType;
import ru.geekbrains.filebox.library.Log2File;
import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.*;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileBoxServer implements ServerSocketThreadListener, SocketThreadListener {

    private final String SERVER_INBOX_PATH = "server/inbox/";
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;
    private ServerSocketThread serverSocketThread;

    private final Vector<SocketThread> clients = new Vector<>();

    // состояние сервера
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
            putLog(Level.INFO,"Server is working");
            state = ServerState.WORKING;
        } else
            putLog( Level.WARNING, "Server is already working.");
    }

    // выключили сервер
    public void stopListening() {

        if (state != ServerState.STOPPED) {
            serverSocketThread.interrupt();
            loginManager.dispose();
            putLog(Level.INFO,"Server stopped");
            state = ServerState.STOPPED;
        } else {
            putLog( Level.WARNING, "Server is not started");
        }
    }


    // пишем лог в окно сервера и в файл
    private synchronized void putLog(Level level, String msg) {
        Log2File.writeServerLog(level, msg);
        msg = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        eventListener.onFileBoxServerLog(this, msg);
    }

    // методы SSTListener'a
    // запустили сервер
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {

        putLog(Level.INFO,"SSocket started...");

    }

    // закрыли сервер
    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
      putLog(Level.INFO,"SSocket stopped...");

    }

    // приняли входящее соединение
    @Override
    public void onServerSocketThreadReady(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog(Level.INFO,"SSocket is ready...");


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

        String threadName = "Socket thread: " + socket.getInetAddress() + ": " + socket.getPort();
        // поток для конкретного пользователя
        new FileBoxSocketThread(this, threadName, socket);
        putLog(Level.INFO,"Client connected: " + socket);
    }

    // если из потока прилетело исключение
    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog(Level.WARNING,"Exception..." + e.getClass().getName());
    }

    // начало соединения логруем
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog(Level.INFO,"started...");
    }

    //конец соединения. очищаем подлюченных клиентов, логируем
    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog(Level.INFO,"stopped.");
    }

    // соединение устоновлено, подключенный пользователь добавился в очередь
    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog(Level.INFO,"Socket is ready");
        clients.add(socketThread);
    }

    // если прила строка, не стал удалять метод, тк может переделаю получение пакетов сообщений
    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String msg) {
        putLog(Level.INFO,"Send " + msg);
    }

    // обрабатываем полученные пакеты
    @Override
    public synchronized void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {

        // создаем поток клиента, с информацией о нем
        FileBoxSocketThread client = (FileBoxSocketThread) socketThread;
        String clientName;
        if (client.getLogin()!=null) clientName = client.getLogin();
        else clientName = "Not authorized clent";
        putLog(Level.INFO,clientName + ": incoming packet type = " + packet.getPacketType());
        // если пакет содержит файлы
        if (packet.getPacketType() == PackageType.FILE) {
            // передаем пакет методу-обработчику
            handleFilePacket(packet, client, socketThread);
            // пользователь хочет скачать файл
        } else if (packet.getPacketType() == PackageType.FILE_REQUEST) {
            String fileRequest = (String) packet.getOutputPacket();
            packFiles(fileRequest, client);

        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            putLog(Level.INFO,client.getLogin() + " MESSAGE received");

            // пользователь запросил список файлов
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            System.out.println("File_List request");
            // пользователь входит в дирректорию
        } else if (packet.getPacketType() == PackageType.ENTER_DIR) {
            String dir = (String) packet.getOutputPacket();

            if (dir.equals("...")){
                client.setPreviousFolder();
            } else {
                client.setCurrentFolder(dir);
            }

        } else if (packet.getPacketType() == PackageType.NEW_FOLDER) {
            String folderName = (String) packet.getOutputPacket();
            File folder = new File(SERVER_INBOX_PATH + client.getLogin()+"/"+client.getCurrentFolder()+"/"+folderName);

            if (!folder.exists()) {
                folder.mkdir();
            }


        } else if (packet.getPacketType() == PackageType.ERROR) {
            putLog(Level.INFO,client.getLogin() + " " + "ERROR received");
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
            // если логи не занят, то пишем в базу нового пользователи
            if (!loginManager.isLoginBusy(rc.getLogin())) {

                loginManager.addNewUser(rc.getLogin(), rc.getMail(), rc.getPassword().hashCode());
                putLog(Level.INFO,"New user '" + rc.getLogin() + "' resistrated and added to database");
                File folder = new File(SERVER_INBOX_PATH + rc.getLogin());
                if (!folder.exists()) {
                    folder.mkdir();
                }
                RegAcceptPacket rap = new RegAcceptPacket(true);
                // если ок, отправляем пользователю пакет об обработанной регистрации
                socketThread.sendPacket(rap);
            } else {
                ((FileBoxSocketThread) socketThread).sendError("Login is busy");
            }
        } else if (packet.getPacketType() == PackageType.RENAME) {

            String renameRequest = (String) packet.getOutputPacket();
            String[] rename = renameRequest.split("<>");
            File file = new File(SERVER_INBOX_PATH + client.getLogin()+"/" + client.getCurrentFolder()+"/" + rename[0]);

            File newFile = new File(SERVER_INBOX_PATH + client.getLogin() +"/" + client.getCurrentFolder()+"/" + rename[1]);
            putLog(Level.INFO, "client.getLogin() rename '"+ rename[0]+"' -> '"+rename[1]+"'");
            if (file.renameTo(newFile)) {
                putLog(Level.INFO,client.getLogin()  + " rename '" + rename[0] + "' to '" + rename[1] + "' complete");
            }

//        } else if (packet.getPacketType() == PackageType.FILE_WAITING) {
//            // узнаем сколько прилетит фалов
//            int filesCount = (Integer) packet.getOutputPacket();
//            socketThread.sendPacket(new FileWaitingPacket(1));
//            Path path;
//            File folder = new File(SERVER_INBOX_PATH + client.getLogin());
//            if (!folder.exists()) {
//                folder.mkdir();
//            }
//            for (int i = 0; i < filesCount; i++) {
//                try {
//                    int x = 0;
//                    DataInputStream dis = new DataInputStream(socket.getInputStream());
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//                    while ((dis.read()) != -1) {
//                        baos.write(x);
//                    }
//                    byte[] file = baos.toByteArray();
//                    path = Paths.get(folder.getPath() + "\\" + "filename");
//                    Files.write(path, file);
//                    baos.close();
//                    dis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }


        } else if (packet.getPacketType() == PackageType.DELETE) {

            String deleteRequest = (String) packet.getOutputPacket();

            File file = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + deleteRequest);
            if (file.isDirectory()){
                deleteDirectory(file);
            } else{
                file.delete();
            }
            putLog(Level.INFO,"deleting file "+deleteRequest);


        } else {
            putLog(Level.WARNING,client.getLogin() + " " + "Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }
        if (client.isAuthorized()) sendFileList(socketThread, client);
    }

    private void handleFilePacket(Packet packet, FileBoxSocketThread client, SocketThread socketThread) {
        // проверяем наличие папки пользователя
        File folder = new File(SERVER_INBOX_PATH + client.getLogin()+client.getCurrentFolder());
        if (!folder.exists()) {
            folder.mkdir();
        }
        Path path;
        // получаем содержимое фалового контейнера внутри пакета
        FileContainerSingle fileContainer = (FileContainerSingle) packet.getOutputPacket();
        byte[] file = fileContainer.getFile();
        String name = fileContainer.getName();
        // проверяем есть ли файл на сервере
        // создаем список
        File[] fList;
        fList = folder.listFiles();

        // проеверем, не превысится ли объем хранилища и информирем об этом клиента
        // считаю все жестко в килобайтах
        if (file.length / 1024 + getUsedSpace(client) / 1024 > client.getTotalSpace()) {
            FileOperationPacket msgPkt = new FileOperationPacket(PackageType.MESSAGE,
                    "No free space in yot FileBox. Delete some and try again ;)");
            socketThread.sendPacket(msgPkt);
            return;
        }

        // если сервер содержит данных файл в конкретной папке, то информируем об этом клиента
        for (int i = 0; i < fList.length; i++) {
            if (fList[i].getName().equals(name)) {
                FileOperationPacket msgPkt = new FileOperationPacket(PackageType.MESSAGE,
                        "File '" + name + "'was already uploaded. Delete it, or upload another file");
                socketThread.sendPacket(msgPkt);
                return;
            }
        }
        // если все ок, то пишем файл на сервер
        try {
            path = Paths.get(folder.getPath() + "\\" + name);
            Files.write(path, file);
            putLog(Level.INFO,client.getLogin() + " " + "File '" + name + "' received. ");
        } catch (IOException e) {

            e.printStackTrace();
            putLog(Level.WARNING, "File saveing exception:"+e);
        }
    }

    // если клиент уже автроризован
    private void handleAuthorizedClient(FileBoxSocketThread client) {


    }
    // удаляем папку
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
    // если не авторизован
    private void handleNonAuthorizedClient(FileBoxSocketThread newClient, LoginContainer lc) {
        // проверяем логин и пароль из содержимого полученного пакета
        String login = lc.getLogin();
        // сравниваем логин и хеши пароля
        boolean isAuth = loginManager.isLoginAndPassCorrect(lc.getLogin(), lc.getPassword().hashCode());
        // если данные не верны, отправляем пакет ошибки, что логин\пароль не верны
        if (!isAuth) {
            newClient.sendError("Wrong email or password");
            putLog(Level.WARNING,"Wrong mail\\pass '" + lc.getLogin() + "\\" + lc.getPassword() + "'");
            return;
        }
        // если все ок, создаем одноименный с пользователем поток сокета
        FileBoxSocketThread client = getClientByNick(login);
        // авторицзуем и отправляем пользователю сообщение об успешнй аутентификации
        newClient.authorizeAccept(login);
        FileOperationPacket msg = new FileOperationPacket(PackageType.MESSAGE,"Login accepted");
        newClient.sendPacket(msg);
        if (client == null) {

            putLog(Level.INFO,"Client " + login + " connected");
        } else {
            putLog(Level.INFO,"Client " + login + " reconnected.");
            client.reconnect();

        }


    }
    // получаем иформацию о занятом на сервере месте
    private int getUsedSpace(FileBoxSocketThread client) {
        int usedSpace = 0;
        File clientFolder = new File(SERVER_INBOX_PATH + client.getLogin());

        try {
            List<File> myfiles = Files.walk(Paths.get(clientFolder.getPath()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (int i = 0; i < myfiles.size(); i++) {
                File file = myfiles.get(i);
                usedSpace += file.length();
            }
        } catch (IOException e) {
            putLog(Level.WARNING, "getUsedSpace exception: "+e);
            e.printStackTrace();
        }
        return usedSpace;
    }

    // отправлем список файловклиенту
    private void sendFileList(SocketThread socketThread, FileBoxSocketThread client) {
        putLog(Level.INFO,client.getLogin() + " FILE_LIST request received");
        // создаем контейнер
        FileListContainer fc = new FileListContainer();
        // устанавливаем путь к папке по имени клиента
        String path = SERVER_INBOX_PATH + client.getLogin();
        // если мыне корневом каталоге, то добавляем в список флаг-ссылку на предыдущую папку
        if (client.getCurrentFolder() != null && !client.getCurrentFolder().equals("")) {
            path += client.getCurrentFolder();
            fc.add(new FileListElement("...", 0, FileType.UP_DIR));
        }
        // определяем папку и ее содержимое
        File clientFolder = new File(path);
        File[] fList = clientFolder.listFiles();

        // собирает содержимое в конкретной папке в контейнер, сождавая FileListElement для каждой папки
        for (int i = 0; i < fList.length; i++) {
            // файлы
            if (fList[i].isFile()) {
                FileListElement element = new FileListElement(fList[i].getName(), fList[i].length() / 1024, FileType.FILE);
                fc.add(element);
            }
            //папка
            if (fList[i].isDirectory()) {
                FileListElement element = new FileListElement("[" + fList[i].getName() + "]", fList[i].length() / 1024, FileType.DIR);
                fc.add(element);
            }
        }
        // кладем в контейнер информацию о занятом месте
        fc.setUsedSpace(getUsedSpace(client));

        //отправляем список
        FileListPacket fileListServer = new FileListPacket(fc);
        socketThread.sendPacket(fileListServer);
    }

    // ищем поток подключенного клиента по нику среди остальных подключенных клинтов
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
        putLog(Level.WARNING, "Exception: "+e);
    }


    private void packFiles(String fileName, FileBoxSocketThread client) {
        FilePacket filePacket = null;
        FileContainerSingle fileContainer = new FileContainerSingle();
        File file = new File(SERVER_INBOX_PATH + client.getLogin() + "\\" + fileName);
        // если клиент пытается скачать файл, отдаем.
        if (file.isFile()) {
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName(), file.length(), 1);
                filePacket = new FilePacket(fileContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            putLog(Level.INFO, "Sending packet. Type: " + filePacket.getPacketType());
            // логируем  и отправляем
            client.sendPacket(filePacket);
        // если папку, предупреждаем об этом и просим выбрать файл
        } else {
            FileOperationPacket fop = new FileOperationPacket(PackageType.MESSAGE, "Its folder,select file please");
            client.sendPacket(fop);
        }

    }
}
