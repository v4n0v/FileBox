package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class FileBoxServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;
    private ServerSocketThread serverSocketThread;
    private final int port = 8189;
    private final Vector<SocketThread> clients = new Vector<>();

    enum ServerState {WORKING, STOPPED}

    private ServerState state = ServerState.STOPPED;

    public FileBoxServer(FileBoxServerListener eventListener, SQLLoginManager loginManager) {
        this.eventListener = eventListener;
        this.loginManager = loginManager;
    }

    public void startListening(int port) {
//        if(serverSocketThread != null && serverSocketThread.isAlive()) {
//            putLog("Сервер уже запущен.");
//            return;
//        }
         if (state != ServerState.WORKING) {
            serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 1000);
            loginManager.init();
            putLog("Server is working");
//            putLog(loginManager.getMail("admin"));
            state = ServerState.WORKING;
         } else
             putLog("Server is working.");
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
        new  SocketThread(this, threadName, socket);
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
        putLog("Send "+msg);
    }

    @Override
    public synchronized void onReceiveFile(SocketThread socketThread, Socket socket, String file) {

        putLog("Moved file '"+file+"' to "+" directory");
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }

}
