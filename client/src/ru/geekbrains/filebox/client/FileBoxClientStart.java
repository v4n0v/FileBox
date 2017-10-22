package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class FileBoxClientStart extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
       // Parent root = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("fxml/loggedClient.fxml"));
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 465, 630));
        //primaryStage.setScene(new Scene(root, 465, 110));
        primaryStage.setResizable(false);
        primaryStage.show();

        Stage stage = new Stage();
        Parent modal = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));
        stage.setTitle("Login");
        stage.setMinHeight(115);
        stage.setMinWidth(460);
        stage.setResizable(false);
        stage.setScene(new Scene(modal));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);



        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
