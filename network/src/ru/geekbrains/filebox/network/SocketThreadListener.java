package ru.geekbrains.filebox.network;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;

public interface SocketThreadListener {
    void onStartSocketThread(SocketThread socketThread);
    void onStopSocketThread(SocketThread socketThread);

    void onReadySocketThread(SocketThread socketThread, Socket socket);
    void onReceiveString(SocketThread socketThread, Socket socket, String msg);
    void onReceiveFile(SocketThread socketThread, Socket socket, String file);

    void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e);

   // void sendFiles(ArrayList<String> list, Socket socket);
}
