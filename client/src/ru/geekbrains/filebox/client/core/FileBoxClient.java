package ru.geekbrains.filebox.client.core;

import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class FileBoxClient {


    public void login() {
        System.out.println("Login");
    }

    public void downloadFile() {
        System.out.println("Download");
    }

    public void uploadFile(File file, Socket socket) {

        System.out.println("Uploading file: "+file);
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

}
