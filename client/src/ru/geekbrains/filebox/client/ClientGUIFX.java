package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientGUIFX extends Application implements SocketThreadListener{
    enum State {CONNECTED, NOT_CONNECTED};
    State state = State.NOT_CONNECTED;
    private SocketThread socketThread;
    private final String IP_ADRESS = "localhost";
    private final int PORT = 8189;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("fxml/2.fxml"));
        primaryStage.setTitle("FileBox");
        primaryStage.setMinHeight(630);
        primaryStage.setMinWidth(465);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        LoginModalFXController login = new LoginModalFXController();

        primaryStage.show();
    }
    @FXML
    Button btnLogin;
    @FXML
    Button btnRegister;
    @FXML
    TextField fieldLogin;



    public void loginAction(){

        if (state== State.NOT_CONNECTED)
            connect();
        else
            disconect();
        fieldLogin.appendText("login");



    }
    public void sendFile(){
                FileChooser fileChooser = new FileChooser();

        List<File> list = fileChooser.showOpenMultipleDialog(null);

        socketThread.sendFile(list);
    }
    private void connect() {
        //    upperPanel.setVisible(false);
        try {
            Socket socket = new Socket(IP_ADRESS, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
//            log.append("Exception: " + e.getMessage() + "\n");
//            log.setCaretPosition(log.getDocument().getLength());
        }
//        upperPanel.setVisible(false);

    }

    private void disconect() {
        socketThread.close();
     //   upperPanel.setVisible(true);

    }
    public static void main(String[] args) {
        launch(args);
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
