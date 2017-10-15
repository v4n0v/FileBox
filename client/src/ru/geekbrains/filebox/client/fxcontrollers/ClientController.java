package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientController implements SocketThreadListener{
    private final String IP_ADRESS = "localhost";
    //  private final String IP_ADRESS = "127.0.0.1";
    private final int PORT = 8189;

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

    enum State {CONNECTED, NOT_CONNECTED};
    public State state = State.NOT_CONNECTED;
 //   public String str;
    private SocketThread socketThread;
    private Socket socket;
//
//    public ClientController(SocketThread socketThread, Socket socket) {
//        this.socketThread = socketThread;
//        this.socket = socket;
//    }
    public void connect() {
    try {
        Socket socket = new Socket(IP_ADRESS, PORT);
        socketThread = new SocketThread(this, "SocketThread", socket);
        //    state=State.CONNECTED;
    } catch (IOException e) {
        e.printStackTrace();
//            log.append("Exception: " + e.getMessage() + "\n");
//            log.setCaretPosition(log.getDocument().getLength());
    }
    //    upperPanel.setVisible(false);

}
    public void renameFile(){
        System.out.println("rename");
    }
    public void  deleteFile(){
        System.out.println("delete");
    }
    public void  downloadFile(){
        System.out.println("download");
    }
    public void  uploadFile(){
       sendFile();
        System.out.println("upload");
    }

    public void  openOptions(){
        System.out.println("options");
    }
    public void  logOut(){
        System.out.println("options");
    }


    public void sendFile(){
        FileChooser fileChooser = new FileChooser();
        List<File> list = fileChooser.showOpenMultipleDialog(null);

        socketThread.sendFile(list);
    }

}
