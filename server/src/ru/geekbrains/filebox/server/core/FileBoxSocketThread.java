package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.AuthAcceptPacket;
import ru.geekbrains.filebox.network.packet.ErrorPacket;

import java.net.Socket;

public class FileBoxSocketThread extends SocketThread {

    public String getLogin() {
        return login;
    }

    String login;
    private boolean isAuthorized;
    private boolean isReconnected;

    public int getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(int totalSpace) {
        this.totalSpace = totalSpace;
    }

    private int totalSpace =10240;


    public FileBoxSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }
// отсылаем юзерыу сообщение об ошибке
    void sendError(String msg) {
        sendPacket(new ErrorPacket(msg));
        close();
    }
    // сообщение об успешной авторизации
    void authorizeAccept(String login) {
        this.isAuthorized = true;
        this.login=login;
        sendPacket(new AuthAcceptPacket(true));
    }
    // сообщение об не успешной авторизации
    void authorizeDecline(String login) {
        this.isAuthorized = false;
        sendPacket(new AuthAcceptPacket(false));
    }
    // переподсоединиться
    void reconnect(){
        isReconnected=true;
       // sendPacket(Messages.getReconnect());
        close();
    }
    // проверка, авторизовали пользователь
    public boolean isAuthorized() {
        return isAuthorized;
    }
}
