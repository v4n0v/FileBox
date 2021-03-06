package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.AuthAcceptPacket;
import ru.geekbrains.filebox.network.packet.FileOperationPacket;
import ru.geekbrains.filebox.network.packet.PackageType;

import java.net.Socket;

public class FileBoxSocketThread extends SocketThread {

    String getLogin() {
        return login;
    }

    String login;
    private boolean isAuthorized;
    private boolean isReconnected;

    String getCurrentFolder() {
        return currentFolder;
    }

    void setCurrentFolder(String currentFolder) {
        this.currentFolder +="/"+ currentFolder;
    }
    void setPreviousFolder(){
        String[] folderPath = currentFolder.split("/");
//        String[] prevFolderPath = new String[folderPath.length-1];
//        System.arraycopy(folderPath, 0, prevFolderPath, 0, prevFolderPath.length);
//        this.currentFolder
        currentFolder="";
        if (folderPath.length!=2) {
            for (int i = 0; i < folderPath.length - 1; i++) {
                currentFolder += folderPath[i] + "/";
            }
        }
    }
    private String currentFolder;

    int getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(int totalSpace) {
        this.totalSpace = totalSpace;
    }

    private int totalSpace =10240;


    FileBoxSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
        currentFolder="";
    }
// отсылаем юзерыу сообщение об ошибке
    void sendError(String msg) {
        sendPacket(new FileOperationPacket(PackageType.ERROR, msg));
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
    boolean isAuthorized() {
        return isAuthorized;
    }
}
