package ru.geekbrains.filebox.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;
    private final ServerSocketThreadListener eventListener;

    public ServerSocketThread(ServerSocketThreadListener eventListener, String name, int port, int timeout) {
        super(name);
        this.eventListener = eventListener;
        this.port = port;
        this.timeout = timeout;
        start();
    }
    // цекл сокета сервера, слушает порт, пока в него не постучит пользователь.
    @Override
    public void run() {
        System.out.println("SST start");
        eventListener.onStartServerSocketThread(this);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(timeout);
            eventListener.onServerSocketThreadReady(this, serverSocket);
            // постучали, создал новый поток сокета для дальнейшей обработки
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    eventListener.onTimeOutAccept(this, serverSocket);
                    continue;
                }
                eventListener.onAcceptedSocket(this, serverSocket, socket);
            }
        } catch (IOException e) {
            eventListener.onExceptionServerSocketThread(this, e);
        } finally {
            eventListener.onStopServerSocketThread(this);
        }


    }
}
