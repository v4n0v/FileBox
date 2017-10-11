package ru.geekbrains.filebox.client.core;

import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import java.io.File;
import java.net.Socket;

public class FileBoxClient implements SocketThreadListener{


    public void login() {
        System.out.println("Login");
    }

    public void downloadFile() {
        System.out.println("Download");
    }

    public void uploadFile() {
        System.out.println("Upload");
    }

    public void renameFile() {
        System.out.println("Rename");
    }

    public void deleteFile() {
        System.out.println("Delete");
    }

    public void connect(String IP, int port){

    }
    public void disconnect(){

    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {

    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {

    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {

    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    @Override
    public void onReceiveFile(SocketThread socketThread, Socket socket, File file) {

    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }
}
