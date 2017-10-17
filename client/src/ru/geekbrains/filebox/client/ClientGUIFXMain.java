package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientGUIFXMain extends Application{

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/loggedClient.fxml"));
        primaryStage.setTitle("FileBox");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setMinHeight(630);
        primaryStage.setMinWidth(465);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
