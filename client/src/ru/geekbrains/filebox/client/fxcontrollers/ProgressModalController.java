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

    public void setProgressBar(ProgressBar progress) {
        this.progress = progress;
    }

    ProgressBar progress;

    //final ProgressIndicator progressIndicator = new ProgressIndicator(0);
    boolean isFinished;

    public void setModalController(ProgressModalController modalController) {
        this.modalController = modalController;
    }

    public ProgressModalController getModalController() {
        return modalController;
    }

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

    public Stage getStage(){
        return this.stage;
    }
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    //    public void bindToProgressBar(Object o){
//        progress.progressProperty().bind(o);
//    }



}