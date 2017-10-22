package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.AuthAcceptPacket;
import ru.geekbrains.filebox.network.packet.ErrorPacket;
import ru.geekbrains.filebox.network.packet.MessagePacket;

import java.net.Socket;

public class FileBoxSocketThread extends SocketThread {

    public String getLogin() {
        return login;
    }

    String login;
    private boolean isAuthorized;
    private boolean isReconnected;

    public FileBoxSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

    void sendError(String msg) {
        sendPacket(new ErrorPacket(msg));
        close();
    }
    void authorizeAccept() {
        this.isAuthorized = true;
        sendPacket(new AuthAcceptPacket(true));
    }

    void authorizeDecline(String login) {
        this.isAuthorized = false;
        sendPacket(new AuthAcceptPacket(true));
    }

    void reconnect(){
        isReconnected=true;
       // sendPacket(Messages.getReconnect());
        close();
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
}
