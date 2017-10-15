package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;

import java.io.IOException;
import java.net.Socket;

public class LoginController  {


    @FXML
    TextField fieldLogin;

    @FXML
    PasswordField fieldPass;

    @FXML
    VBox rootElement;

    public void tryToLogin() throws Exception {

        //if (state==State.CONNECTED) {

            Stage mystage = (Stage)rootElement.getScene().getWindow();
            //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/loggedClient.fxml"));
            Parent root = fxmlLoader.load();
            mystage.setTitle("Main");
            mystage.setScene(new Scene(root, 465, 630));
            mystage.setResizable(false);
            ClientController cm = (ClientController)fxmlLoader.getController();
            cm.connect();
            mystage.show();
    //    }
    }
    //private SocketThread socketThread;
//    private void connect() {
//        try {
//            Socket socket = new Socket(IP_ADRESS, PORT);
//            socketThread = new SocketThread(this, "SocketThread", socket);
//        //    state=State.CONNECTED;
//        } catch (IOException e) {
//            e.printStackTrace();
////            log.append("Exception: " + e.getMessage() + "\n");
////            log.setCaretPosition(log.getDocument().getLength());
//        }
//    //    upperPanel.setVisible(false);
//
//    }

}
