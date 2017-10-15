package ru.geekbrains.filebox.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.ClientGUIFX;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientGUIFXController implements SocketThreadListener{

    enum State {CONNECTED, NOT_CONNECTED};
    State state = State.NOT_CONNECTED;
    private SocketThread socketThread;
    private final String IP_ADRESS = "localhost";
    private final int PORT = 8189;
    Stage primaryStage;
    Stage stage;

    @FXML
    Button btnRegister;

    @FXML
    Button btnOptions;
    @FXML
    Button btnLogout;
    @FXML
    TextField fieldLogin;

    @FXML
    Button btnRename;
    @FXML
    Button btnDelete;

    @FXML
    Button btnDownload;
    @FXML
    Button btnUpload;
    @FXML
    TableView tblContent;

    public void loginAction(){
        if (state== State.NOT_CONNECTED) {
            connect();
            state = State.CONNECTED;
        }else {
            disconect();
            state= State.NOT_CONNECTED;
        }
    }
    public void sendFile(){
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);

        socketThread.sendFile(list);
    }
    private void connect() {
        try {
            Socket socket = new Socket(IP_ADRESS, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Server is not available :(", "Error: ", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void disconect() {
        socketThread.close();
        //   upperPanel.setVisible(true);
    }

    public void deleteFile() {
        System.out.println("file deleted");
    }
    public void renameFile() {
        System.out.println("file renamed");
    }
    public void downloadFile() {
        System.out.println("file downloaded");
    }
    public void openOptions() {
        System.out.println("options opened");
    }
    public void logOut() {
        System.out.println("user is logged out");
        if (state== State.NOT_CONNECTED) {
            //    disconect();
            try {
        //        showModalLoginWidow(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void onReceiveFile(SocketThread socketThread, Socket socket, String file) {

    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }
}
