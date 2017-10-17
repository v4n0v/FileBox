package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.ServerSocketThread;
import ru.geekbrains.filebox.network.ServerSocketThreadListener;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.AbstractPacket;
import ru.geekbrains.filebox.network.packet.FileContainer;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;

public class FileBoxServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;
    private ServerSocketThread serverSocketThread;
    private final int port = 8189;
    private final Vector<SocketThread> clients = new Vector<>();
    PrintWriter log;
    FileWriter logFile;
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
            log.printf(msg+"\n");
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
    public synchronized void onReceivePacket(SocketThread socketThread, Socket socket, AbstractPacket packet) {
        putLog("Packet "+packet.getPacketType());

        if (packet.getPacketType()== PackageType.FILE){
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            for (int i = 0; i < files.size(); i++) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(names.get(i)));
                    fos.write(files.get(i));
                    fos.close();
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }

//            List<File> filePack = (List<File>) packet.getOutputPacket();
//            File file;
//            FileWriter fileWriter;
//            PrintWriter printWriter;
//            String path="";
//            for (int i = 0; i < filePack.size(); i++) {
//                file=filePack.get(i);
//                try {
//                    fileWriter = new FileWriter( file.getName(), true);
//                    printWriter = new PrintWriter((java.io.Writer) fileWriter);
//                    putLog("File "+path+file.getName()+" was moved into "+path+" directory");
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//                    return;
//                }
//            }
//
//            System.out.println("Красавчик");
//          //  msg = dateFormat.format(System.currentTimeMillis()) + msg;
//
        }
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }

}
