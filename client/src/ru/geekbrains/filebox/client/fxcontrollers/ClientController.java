package ru.geekbrains.filebox.client.fxcontrollers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.*;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ClientController implements SocketThreadListener, Thread.UncaughtExceptionHandler {
    private final String IP_ADRESS = "localhost";

    String errorMsg;

    public boolean isAuthorized() {
        return isAuthorized;
    }
    AbstractPacket incomingPacket;
    private boolean isAuthorized;

    private final int PORT = 8189;
    private FileWriter logFile;
    private PrintWriter log;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    public String login;
    public String password;
    private String username;
    private final long MAX_FILE_SIZE = 5_242_880;

    enum State {CONNECTED, NOT_CONNECTED, ERROR}

    public State state = State.NOT_CONNECTED;

    private SocketThread socketThread;
    private Socket socket;

    private FilePacket filePacket;
    private MessagePacket messagePacket;

    @FXML
    VBox loggedRootElement;
    @FXML
    GridPane upperPanelLogin;
    @FXML
    GridPane upperPanelLogged;
    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    private void handlePacket(AbstractPacket packet){




    }


    public void connect() {

        System.out.println("");
        try {
            Socket socket = new Socket(IP_ADRESS, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);


        } catch (IOException e) {
            e.printStackTrace();

            errorMesage(e.getMessage());
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

    public void logOut() throws Exception {

        System.out.println("Client logOut");
        disconect();
        writeLog("Client LogOut");
        Stage mystage = (Stage) loggedRootElement.getScene().getWindow();
        mystage.close();
    }

    public void loginToFileBox() {
        if (!fieldLogin.getText().isEmpty() || !fieldLogin.getText().isEmpty()) {
            login = fieldLogin.getText();
            password = fieldPass.getText();


            connect();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Fill mail and password fields");
            alert.showAndWait();
        }
    }

    public void sendFile() {
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);
        FileContainer fileContainer = new FileContainer();

        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            if (file.length() > MAX_FILE_SIZE) {
                errorMesage("File size is more than 50MB");
                writeLog(file.getName() + " is too big for transmission (>" + MAX_FILE_SIZE + "bytes)");
                continue;
            }
            try {
                fileContainer.addFile(Files.readAllBytes(Paths.get(file.getPath())), file.getName());
                filePacket = new FilePacket(fileContainer);

                writeLog("Sending packet. Type: " + filePacket.getPacketType());
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
        errorMesage(msg);

        writeLog("Exception: " + msg + "\n");
        System.exit(1);
    }

    private void errorMesage(String msg) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(msg);
        errorAlert.showAndWait();
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        writeLog(
                "Socket started");
    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        isAuthorized = false;

        writeLog("Socket closed. End of session");
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        writeLog("Connection to server done!");
        LoginPacket loginPacket = new LoginPacket(login, password);
        socketThread.sendPacket(loginPacket);
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {

    }

    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, AbstractPacket packet) {





        if (packet.getPacketType() == PackageType.FILE) {
            FileContainer filePackage = (FileContainer) packet.getOutputPacket();
            ArrayList<byte[]> files = filePackage.getFiles();
            ArrayList<String> names = filePackage.getNames();
            for (int i = 0; i < files.size(); i++) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(names.get(i)));
                    fos.write(files.get(i));
                    fos.close();
                    writeLog("File '" + names.get(i) + "' received. ");
                } catch (IOException e) {

                    e.printStackTrace();
                    return;
                }
            }

        } else if (packet.getPacketType() == PackageType.MESSAGE) {
            writeLog("MESSAGE received");
        } else if (packet.getPacketType() == PackageType.FILE_LIST) {
            writeLog("FILE_LIST received");


        } else if (packet.getPacketType() == PackageType.ERROR) {
            errorMsg = "ERROR received";
            state = State.ERROR;
            writeLog(errorMsg);
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    errorMesage(errorMsg);
                }
            });
            //

        } else if (packet.getPacketType() == PackageType.LOGIN) {
            LoginContainer lc = (LoginContainer) packet.getOutputPacket();
        } else if (packet.getPacketType() == PackageType.AUTH_ACCEPT) {
            isAuthorized = (Boolean) packet.getOutputPacket();

            if (!isAuthorized){
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        errorMesage("wrong login\\pass");
                    }
                });
            }
            state = State.CONNECTED;

            upperPanelLogged.setVisible(true);
            //   upperPanelLogged.setVisible(true);
            upperPanelLogin.setVisible(false);
        } else {
            writeLog("Exception: Unknown package type :(");
            throw new RuntimeException("Unknown package type");

        }
        writeLog("Upload complete...");

    }


    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        errorMesage(e.getMessage());
        writeLog("Exception: " + e.getMessage());
    }

}
