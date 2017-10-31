package ru.geekbrains.filebox.library;

import javafx.application.Platform;
import javafx.scene.control.Alert;

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
}
