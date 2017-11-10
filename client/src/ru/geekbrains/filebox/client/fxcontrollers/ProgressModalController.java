package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.FileBoxClientStart;

import java.io.File;

public class ProgressModalController extends BaseController {

    @FXML
    Label lblFileName;
    ProgressBar progress;
    ProgressModalController modalController;


    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    public void setProgress(double val) {
        Platform.runLater(() -> {
            progress.setProgress(val);
        });
    }
    public void setProgressBar(ProgressBar progress) {
        this.progress = progress;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setModalController(ProgressModalController modalController) {
        this.modalController = modalController;
    }


}