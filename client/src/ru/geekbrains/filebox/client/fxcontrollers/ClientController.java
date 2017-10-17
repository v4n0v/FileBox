package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.AbstractPacket;
import ru.geekbrains.filebox.network.packet.FileContainer;
import ru.geekbrains.filebox.network.packet.FilePacket;
import ru.geekbrains.filebox.network.packet.MessagePacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClientController implements SocketThreadListener, Thread.UncaughtExceptionHandler {
    private final String IP_ADRESS = "localhost";
    //  private final String IP_ADRESS = "127.0.0.1";
    private final int PORT = 8189;
    private FileWriter logFile;
    private PrintWriter log;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    public String login;
    public String password;

    enum State {CONNECTED, NOT_CONNECTED}

    public State state = State.NOT_CONNECTED;

    private SocketThread socketThread;
    private Socket socket;

    private FilePacket filePacket;
    private MessagePacket messagePacket;

    @FXML
    VBox loggedRootElement;

    public void connect() {
        try {
            Socket socket = new Socket(IP_ADRESS, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
            state = State.CONNECTED;
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection error");
            alert.setHeaderText("No connection!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
//            log.append("Exception: " + e.getMessage() + "\n");
            writeLog("Exception: " + e.getMessage() + "\n");
        }
    }

    private void disconect() {
        socketThread.close();
    }

    public void renameFile() {
        System.out.println("rename");
    }

    public void deleteFile() {
        System.out.println("delete");
    }

    public void downloadFile() {
        System.out.println("download");
    }

    public void uploadFile() {
        sendFile();
        System.out.println("upload");
    }

    public void openOptions() {
        System.out.println("options");
    }

    public void logOut()throws Exception {

        System.out.println("Client logOut");
        disconect();
        writeLog("Client LogOut");
        Stage mystage = (Stage) loggedRootElement.getScene().getWindow();
        mystage.close();
    }


    public void sendFile() {
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainer fileContainer = new FileContainer();

        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName());
                filePacket = new FilePacket(fileContainer);
                socketThread.sendPacket(filePacket);
                //
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) + msg;
        try {
            logFile = new FileWriter("client.log", true);
            log = new PrintWriter((java.io.Writer) logFile);
        } catch (IOException e) {
            log.printf(msg);
            e.printStackTrace();
            return;
        }
        try {
            throw new Exception();
        } catch (Exception ex) {
            log.printf(msg + "\n");
            log.flush();
        }

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String msg;
        if (stackTraceElements.length == 0) {
            msg = "Empty StackTrace";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(e.getClass().getCanonicalName());
        alert.setContentText(msg);
        alert.showAndWait();

        writeLog("Exception: " + msg + "\n");
        System.exit(1);
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {

    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {

    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        writeLog("Connection to server done!");

    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, AbstractPacket packet) {

    }


    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {

    }

}
