package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ru.geekbrains.filebox.client.fxcontrollers.ClientController;
import ru.geekbrains.filebox.network.SocketThread;

import java.io.File;
import java.io.IOException;

public class FileBoxClientStart extends Application {
    private Stage primaryStage;
    private VBox loggedRootElement;
    private VBox loginRootElement;
    private ClientController controller;

    public SocketThread getSocketThread() {
        return socketThread;
    }

    public void setSocketThread(SocketThread socketThread) {
        this.socketThread = socketThread;
    }

    public SocketThread socketThread;

    public void initClientLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/logged_client.fxml"));
            loggedRootElement = (VBox) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(loggedRootElement);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);
            controller.setSocketThread(socketThread);
            primaryStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("FileBoxClient");

        // Set the application icon.
//        this.primaryStage.getIcons().add(
//                new Image("file:resources/images/ico.png"));

        initClientLayout();
        controller.initClientLoginLayout();
    }
}
//    public void initClientLoginLayout() {
//        try {
//            // новое окно логина
//            Stage stage = new Stage();
////        Parent modal = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));
//
//            FXMLLoader loaderLog = new FXMLLoader();
//            loaderLog.setLocation(FileBoxClientStart.class.getResource("fxml/login_modal.fxml"));
//            loginRootElement = (VBox) loaderLog.load();
//            ClientController controller = loaderLog.getController();
//            controller.setMainApp(this);
//            controller.setSocketThread(socketThread);
//            stage.setTitle("Login");
//
//            stage.setResizable(false);
//            Scene sceneLog = new Scene(loginRootElement);
//            stage.setScene(sceneLog);
//
//            stage.setOnCloseRequest(event -> event.consume());
//            stage.initModality(Modality.WINDOW_MODAL);
//            stage.initOwner(primaryStage);
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        //  FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/logged_client.fxml"));
//        Parent root = FXMLLoader.load(getClass().getResource("fxml/logged_client.fxml"));
//        //Parent root = fxmlLoader.load();
//        primaryStage.setTitle("Login");
//        primaryStage.setScene(new Scene(root, 465, 630));
//        //primaryStage.setScene(new Scene(root, 465, 110));
//        primaryStage.setResizable(false);
//        primaryStage.show();
//
//        FXMLLoader loader1 = new FXMLLoader();
//        loader1.setLocation(FileBoxClientStart.class.getResource("fxml/logged_client.fxml"));
//        ClientController cm = (ClientController) loader1.getController();
//        cm.setMainApp(this);
//
//        Stage stage = new Stage();
//        Parent modal = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));
//
//        stage.setTitle("Login");
//        stage.setMinHeight(115);
//        stage.setMinWidth(460);
//        stage.setResizable(false);
//        stage.setScene(new Scene(modal));
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.initOwner(primaryStage);
//
//        stage.show();
//
//    }
//}