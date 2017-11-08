package ru.geekbrains.filebox.library;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertWindow {
    private static Alert alert;
    private static void alertWindow(String title, String msg, Alert.AlertType type){
        Platform.runLater(() -> {
            alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
    public static void errorMesage(String msg) {

        alertWindow("Error", msg, Alert.AlertType.ERROR );
    }

    public static void infoMesage(String msg) {
        alertWindow("Info", msg, Alert.AlertType.INFORMATION );
    }
    public static void warningMesage(String msg) {
        alertWindow("Warning", msg, Alert.AlertType.WARNING );
    }

    public static boolean dialogWindow(String title, String file){
        boolean answer=false;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Are you sure?");
        alert.setContentText(file);


        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            answer=true;
        }
        return answer;
    }
}
