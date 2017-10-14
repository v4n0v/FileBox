package ru.geekbrains.filebox.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class LoginModalFXController {

    public void showLoginDialog(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("fxml/login_modal.fxml"));

            stage.setTitle("Login");
            stage.setMinHeight(630);
            stage.setMinWidth(465);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());
            stage.setResizable(false);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
