package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.core.ClientPreferences;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.client.fxcontrollers.*;
import ru.geekbrains.filebox.network.SocketThread;

import java.io.File;
import java.io.IOException;

public class FileBoxClientStart extends Application {

    // перменные
    private ProgressModalController progressController;
    private ObservableList<FileListXMLElement> clientFileList = FXCollections.observableArrayList();
    private ObservableList<FileListXMLElement> serverFileList = FXCollections.observableArrayList();
    private Stage primaryStage;
    private VBox loggedRootElement;
    private String logoPath;
    private ClientController clientController;
    public SocketThread socketThread;
    private ClientPreferences config;
    private String currentStyleCSS;

    // геттеры и сеттеры
    public ObservableList<FileListXMLElement> getServerFileList() {
        return serverFileList;
    }
    public ObservableList<FileListXMLElement> getClientFileList() {
        return clientFileList;
    }
    public void setServerFileList(ObservableList<FileListXMLElement> serverFileList) {
        this.serverFileList = serverFileList;
    }
    public ClientPreferences getConfig() {
        return config;
    }
    public ProgressModalController getProgressController() {
        return progressController;
    }
    public SocketThread getSocketThread() {
        return socketThread;
    }
    public void setSocketThread(SocketThread socketThread) {
        this.socketThread = socketThread;
    }
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public FileBoxClientStart getMainApp() {
        return this;
    }
    public String getCurrentStyleCSS() {
        return currentStyleCSS;
    }
    public void setCurrentStyleCSS(String currentStyleCSS) {
        this.currentStyleCSS = currentStyleCSS;
    }

    // инициализация всех окон приложения

    public void showClientLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/logged_client.fxml"));
            loggedRootElement = (VBox) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(loggedRootElement);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.getIcons().add(new Image(logoPath));
            setStyleToStage(currentStyleCSS, scene);
            // Give the clientController access to the main app.
            clientController = loader.getController();
            clientController.setMainApp(this);

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
        this.primaryStage.setTitle("FileBoxClientManager");
        File logo = new File("logo.png");
        logoPath = "file:///" + logo.getAbsolutePath().replace("\\", "/");
        //инициализируем окно клиента
        File cfgFile = new File("config.xml");
        if (cfgFile.exists()) {
            config = new ClientPreferences();
            config.loadConfig();
        }
        currentStyleCSS = config.getCurrentStyle();

        showClientLayout();
        //инициализируем модальное окно логина
        showClientLoginLayout();

        // связываем таблицу и ObservableList  данными
        clientController.initTable();

    }


    private void initController(BaseController controller, Stage stage) {
        // controller = loader.getController();
        controller.setMainApp(this);
        controller.setStage(stage);
        controller.setClientController(clientController);
    }

    public void showClientLoginLayout() {
        try {
            // новое окно логина
            Stage loginStage = new Stage();
            FXMLLoader loaderLog = new FXMLLoader();
            loaderLog.setLocation(FileBoxClientStart.class.getResource("fxml/login_modal.fxml"));
            VBox loginRootElement = (VBox) loaderLog.load();
            Scene sceneLog = new Scene(loginRootElement);

            // получаем ссылку у контроллера окна
            // controller.

            loginStage.getIcons().add(new Image(logoPath));
            loginStage.setTitle("Login");
            loginStage.setOnCloseRequest((event) -> primaryStage.close());
            setStyleToStage(currentStyleCSS, sceneLog);
            loginStage.setResizable(false);
            loginStage.setScene(sceneLog);
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(primaryStage);
            LoginController controller = loaderLog.getController();
            controller.setClientController(clientController);
            controller.setMainApp(this);
            clientController.setLoginStage(loginStage);
            controller.setStage(loginStage);

            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void removeFromTable(String name) {
        FileListXMLElement element;
        for (int i = 0; i < serverFileList.size(); i++) {
            element = serverFileList.get(i);
            if (name.equals(element.getFileName().getValue())) {
                serverFileList.remove(i);
                return;
            }
        }
    }


    // инициализация дилогового окна переименования файла
    public void showRenameLayout(String fileName) {

        try {
            // новое окно переименования
            Stage renameStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/rename_modal.fxml"));
            renameStage.getIcons().add(new Image(logoPath));
            // создаем сцену, задаем параметры
            HBox page = (HBox) loader.load();
            Scene scene = new Scene(page);
            renameStage.setScene(scene);
            renameStage.setTitle("Rename " + fileName);
            renameStage.initModality(Modality.WINDOW_MODAL);
            renameStage.initOwner(primaryStage);
            setStyleToStage(currentStyleCSS, scene);
            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            RenameController renameController = loader.getController();
            renameController.setMainApp(this);
            renameController.setCurrentName(fileName);
            renameController.setStage(renameStage);
            renameController.init();
            renameController.setClientController(clientController);
            renameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Stage registerStage;
    public void showRegisterNewLayout() {
        try {
           registerStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/registration_modal.fxml"));
            registerStage.getIcons().add(new Image(logoPath));
            VBox canvas = (VBox) loader.load();
            Scene scene = new Scene(canvas);
            registerStage.setScene(scene);
            registerStage.setTitle("Register new user");
            registerStage.initModality(Modality.WINDOW_MODAL);
            registerStage.initOwner(primaryStage);

            setStyleToStage(currentStyleCSS, scene);
            RegistrationController regController = loader.getController();
            initController(regController, registerStage);
            registerStage.setResizable(false);
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void regExit() {
        registerStage.close();
    }
    public void showOptionsLayout() {

        try {
            // новое окно переименования
            Stage optionsStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/options_modal.fxml"));
            optionsStage.getIcons().add(new Image(logoPath));
            // создаем сцену, задаем параметры
            VBox page = (VBox) loader.load();
            Scene scene = new Scene(page);
            optionsStage.setScene(scene);
            optionsStage.setTitle("Options");
            optionsStage.initModality(Modality.WINDOW_MODAL);
            optionsStage.initOwner(primaryStage);
            setStyleToStage(currentStyleCSS, scene);
            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            OptionsController optionsController = loader.getController();
            optionsController.setMainApp(this);
            optionsController.init();
            optionsController.setPrimaryStage(primaryStage);
            optionsController.setStage(optionsStage);
            optionsController.setClientController(clientController);
            optionsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //    public void showProgressLayout(String title, String fileName, File file) {
    public void showProgressLayout(String title) {

        try {
            // новое окно переименования
            Stage progressStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/progress_modal.fxml"));
            progressStage.getIcons().add(new Image(logoPath));
            // создаем сцену, задаем параметры
            VBox page = (VBox) loader.load();
            Scene scene = new Scene(page);
            progressStage.setScene(scene);
            progressStage.setTitle(title);
            progressStage.initModality(Modality.WINDOW_MODAL);
            progressStage.initOwner(primaryStage);
            setStyleToStage(currentStyleCSS, scene);
            ProgressBar pb = (ProgressBar) page.getChildren().get(1);
            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс

            progressController = new ProgressModalController();
            progressController.setMainApp(this);
            progressController.setProgressBar(pb);
            progressController.setStage(progressStage);
            progressController.setModalController(progressController);
            progressController.setClientController(clientController);
            progressStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showSyncLayout() {

        try {
            // новое окно переименования
            Stage syncStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/sync_layout.fxml"));
            syncStage.getIcons().add(new Image(logoPath));
            // создаем сцену, задаем параметры
            HBox page = (HBox) loader.load();
            Scene scene = new Scene(page);
            syncStage.setScene(scene);
            syncStage.setTitle("Synchronize your FileBox");
            syncStage.initModality(Modality.WINDOW_MODAL);
            syncStage.initOwner(primaryStage);
            setStyleToStage(currentStyleCSS, scene);

            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            SyncController syncController = loader.getController();
            syncController.setMainApp(this);
            syncController.init();
            syncController.setStage(syncStage);
            syncController.setClientController(clientController);
            syncStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setStyleToStage(String style, Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(style);

    }


}
