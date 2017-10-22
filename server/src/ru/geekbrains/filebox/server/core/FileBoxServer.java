package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.Packet;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;
import ru.geekbrains.filebox.network.packet.RegAcceptPacket;
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

    private final String SERVER_INBOX_PATH =    "server/inbox/";
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;
    private ServerSocketThread serverSocketThread;
    private final int port = 8189;
    private final Vector<SocketThread> clients = new Vector<>();
    private PrintWriter log;
    private FileWriter logFile;
    private String clientPath="";
    enum ServerState {WORKING, STOPPED}

    private ServerState state = ServerState.STOPPED;

    public FileBoxServer(FileBoxServerListener eventListener, SQLLoginManager loginManager) {
        this.eventListener = eventListener;
        this.loginManager = loginManager;
    }

    public void startListening(int port) {

        if (state != ServerState.WORKING) {
            serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 1000);
            loginManager.init();
            putLog("Server is working");
            state = ServerState.WORKING;
        } else
            putLog("Server is already working.");
    }

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
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("SSocket started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("SSocket stopped...");
    }

    @Override
    public void onServerSocketThreadReady(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("SSocket is ready...");
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {

        //putLog("accept()timeout...");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ": " + socket.getPort();
        //  clients.add(new SocketThread(this, threadName, socket));
//        new SocketThread(this, threadName, socket);
        new FileBoxSocketThread (this, threadName, socket);
        putLog("socket accepted...");
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception..." + e.getClass().getName());
    }


    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog("stopped.");
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready");
        clients.add(socketThread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String msg) {
        putLog("Send " + msg);
    }


    @Override
    public synchronized void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {
        putLog("Incoming packet type = " + packet.getPacketType());
        FileBoxSocketThread client = (FileBoxSocketThread) socketThread;
        //client.

        if (packet.getPacketType() == PackageType.FILE) {
//            File folder = new File( "server/inbox/client");
//            if (!folder.exists()) {
//                folder.mkdir();
//            }
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            for (int i = 0; i < files.size(); i++) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(SERVER_INBOX_PATH + clientPath + names.get(i)));
                    fos.write(files.get(i));
                    fos.close();
                    putLog("File '" + names.get(i) + "' received. ");
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }

        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            putLog("MESSAGE received");
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            putLog("FILE_LIST received");
        } else if (packet.getPacketType() == PackageType.ERROR) {
            putLog("ERROR received");

        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();

            if (client.isAuthorized()){
                handleAuthorizedClient(client);
            } else {
                handleNonAuthorizedClient(client, lc);
            }
        }else if (packet.getPacketType() == PackageType.REGISTRATION) {
            RegContainer rc = (RegContainer) packet.getOutputPacket();
            if (!loginManager.isLoginBusy(rc.getLogin())){
                String l = rc.getLogin();
                String m = rc.getMail();
                String p = rc.getPassword();
                loginManager.addNewUser(rc.getLogin(), rc.getMail(), rc.getPassword());
                putLog("New user '"+rc.getLogin()+"' resistrated and added to database" );
                RegAcceptPacket rap = new RegAcceptPacket(true);
                socketThread.sendPacket(rap);
            } else {
                ((FileBoxSocketThread) socketThread).sendError("Login is busy");
            }
        }else {
            putLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }
    }

    private void handleAuthorizedClient(FileBoxSocketThread client){

    }
    private void handleNonAuthorizedClient(FileBoxSocketThread newClient, LoginContainer lc){
       // String login = loginManager.getLogin(lc.getLogin(), lc.getPassword());
        String login= lc.getLogin();
        boolean isAuth = loginManager.checkLogin(lc.getLogin(), lc.getPassword());
    //    String mail = loginManager.getLogin(lc.getLogin(), lc.getPassword());
//        if (login==null){
        if (!isAuth){
            newClient.sendError("Wrong email or password");
            putLog("Wrong mail\\pass '"+lc.getLogin()+"\\"+ lc.getPassword()+"'");
            return;
        }
//        if (mail==null){
//            newClient.sendError("Wrong email or password");
//            return;
//        }
        FileBoxSocketThread client = getClientByNick(lc.getLogin());
        newClient.authorizeAccept();

        if (client == null) {
            System.out.println("client connected");
            putLog("Client "+login+" connected" );
        } else {
            putLog("Client "+login + " reconnected.");
             client.reconnect();

        }
       // newClient.authorizeAccept();

    }

    public FileBoxSocketThread getClientByNick(String nickname){
        final int cnt =clients.size();

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
