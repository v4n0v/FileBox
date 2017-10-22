package ru.geekbrains.filebox.network;

import ru.geekbrains.filebox.network.packet.Packet;

import java.net.Socket;

public interface SocketThreadListener {
    void onStartSocketThread(SocketThread socketThread);
    void onStopSocketThread(SocketThread socketThread);

    void onReadySocketThread(SocketThread socketThread, Socket socket);
    void onReceiveString(SocketThread socketThread, Socket socket, String msg);

    void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet);

    void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e);

   // void sendFiles(ArrayList<String> list, Socket socket);
}
