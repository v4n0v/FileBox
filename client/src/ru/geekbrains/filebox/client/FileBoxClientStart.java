package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.geekbrains.filebox.client.core.ClientPreferences;
import ru.geekbrains.filebox.client.core.FileListWrapper;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.client.core.FileListXMLWrapper;
import ru.geekbrains.filebox.client.fxcontrollers.*;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class FileBoxClientStart extends Application {
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public FileBoxClientStart getMainApp() {
        return this;
    }

    public void setFileListData(ObservableList<FileListElement> fileListData) {
        this.fileListData = fileListData;
    }

    public ObservableList<FileListElement> fileListData = FXCollections.observableArrayList();

    public ObservableList<FileListXMLElement> getFileListDataProp() {
        return fileListDataProp;
    }

    public ObservableList<String> rowFiles;
    public ObservableList<Long> rowSize;

    public void setFileListDataProp(ObservableList<FileListXMLElement> fileListDataProp) {
        this.fileListDataProp = fileListDataProp;
    }

    public ObservableList<FileListXMLElement> fileListDataProp = FXCollections.observableArrayList();

    public void fillFileList(ArrayList<FileListElement> list) {
        fileListData.clear();
        for (int i = 0; i < list.size(); i++) {
            fileListData.add(list.get(i));
        }
    }

    private Stage primaryStage;
    private VBox loggedRootElement;

    private ClientController clientController;

    public SocketThread getSocketThread() {
        return socketThread;
    }

    public void setSocketThread(SocketThread socketThread) {
        this.socketThread = socketThread;
    }

    public SocketThread socketThread;

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
            // Give the clientController access to the main app.
            clientController = loader.getController();
            clientController.setMainApp(this);

            //   clientController.updateTable();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        // подгружаем последний список файлов из XML


    }

    public static void main(String[] args) {
        launch(args);
    }

    public ClientPreferences getConfig() {
        return config;
    }

    private ClientPreferences config;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("FileBoxClientManager");
        //инициализируем окно клиента
        showClientLayout();
        //инициализируем модальное окно логина
        showClientLoginLayout();
      //  clientController.initClientLoginLayout();
        File cfgFile = new File("config.xml");
        if (cfgFile.exists()) {
            config = new ClientPreferences();
            config.loadConfig();
        }
        // связываем таблицу и ObservableList  данными
        clientController.initTable();

//        // получаем последний сохранненный  список файлов
//        File file = getFileListFilePath();
//        if (file != null) {
//            loadFileListDataFromFile(file);
//        }
    //    System.out.println(clientController.lbLastUpd);
        // обновляем время последней синхронизации
       // clientController.lastUpdate();
    }

    public File getFileListFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(FileBoxClientStart.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Задаёт путь текущему загруженному файлу. Этот путь сохраняется
     * в реестре, специфичном для конкретной операционной системы.
     */
    public void setFileListFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(FileBoxClientStart.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Обновление заглавия сцены.
//            primaryStage.setTitle("FileBox - " + file.getName());
            primaryStage.setTitle("FileBox - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Обновление заглавия сцены.
            primaryStage.setTitle("FileBox");
        }
    }


    //    public void loadFileListDataFromFile(File file) {
    public void loadFileListDataFromFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(FileListXMLWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            System.out.println(file.getAbsolutePath());
            // Reading XML from the file and unmarshalling.
            FileListXMLWrapper wrapper = (FileListXMLWrapper) um.unmarshal(file);

            fileListDataProp.clear();
            fileListDataProp.addAll(wrapper.getFiles());

            // Save the file path to the registry.
            setFileListFilePath(file);

        } catch (Exception e) { // catches ANY exception
            e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());
        }
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

            loginStage.setTitle("Login");
            loginStage.setOnCloseRequest((event) -> primaryStage.close());

            loginStage.setResizable(false);
            loginStage.setScene(sceneLog);
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(primaryStage);
            LoginController controller = loaderLog.getController();
            controller.setClientController(clientController);
            controller.setMainApp(this);
            clientController.setLoginStage(loginStage);
            controller.setStage(loginStage);


//            Stage renameStage = new Stage();
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(FileBoxClientStart.class.getResource("fxml/rename_modal.fxml"));
//
//            // создаем сцену, задаем параметры
//            HBox page = (HBox) loader.load();
//            Scene scene = new Scene(page);
//            renameStage.setScene(scene);
//            renameStage.setTitle("Rename " + fileName);
//            renameStage.initModality(Modality.WINDOW_MODAL);
//            renameStage.initOwner(primaryStage);
//
//            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
//            RenameController renameController = loader.getController();
//            renameController.setMainApp(this);
//            renameController.setCurrentName(fileName);
//            renameController.setDialogStage(renameStage);
//            renameController.setClientController(clientController);
//            renameStage.show();

            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void saveFileListDataToFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(FileListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные об адресатах.
            FileListWrapper wrapper = new FileListWrapper();
            wrapper.setFiles(fileListData);

            // Маршаллируем и сохраняем XML в файл.
            m.marshal(wrapper, file);
            System.out.println("file '" + file + "' marshalised");
            // Сохраняем путь к файлу в реестре.
            //     setFileListFilePath(file);
        } catch (Exception e) { // catches ANY exception
            AlertWindow.errorMesage("Could not save data to file:\n" + file.getPath() + "\n" + e.getMessage());
        }
    }

    public void removeFromTable(String name) {
        FileListXMLElement element;
        for (int i = 0; i < fileListDataProp.size(); i++) {
            element = fileListDataProp.get(i);
            if (name.equals(element.getFileName().getValue())) {
                fileListDataProp.remove(i);
                return;
            }
        }


    }
    private void initController(BaseController controller,  Stage stage){
        // controller = loader.getController();
        controller.setMainApp(this);
        controller.setStage(stage);
        controller.setClientController(clientController);
    }
    // инициализация дилогового окна переименования файла
    public void showRenameLayout(String fileName) {

        try {
            // новое окно переименования
            Stage renameStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/rename_modal.fxml"));

            // создаем сцену, задаем параметры
            HBox page = (HBox) loader.load();
            Scene scene = new Scene(page);
            renameStage.setScene(scene);
            renameStage.setTitle("Rename " + fileName);
            renameStage.initModality(Modality.WINDOW_MODAL);
            renameStage.initOwner(primaryStage);

            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            RenameController renameController = loader.getController();
            renameController.setMainApp(this);
            renameController.setCurrentName(fileName);
            renameController.setDialogStage(renameStage);
            renameController.setClientController(clientController);
            renameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterNewLayout() {
        try {
            Stage registerStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/registration_modal.fxml"));

            VBox canvas = (VBox) loader.load();
            Scene scene = new Scene(canvas);
            registerStage.setScene(scene);
            registerStage.setTitle("Register new user");
            registerStage.initModality(Modality.WINDOW_MODAL);
            registerStage.initOwner(primaryStage);

            RegistrationController regController = loader.getController();
//            regController.setMainApp(this);
//            regController.setStage(registrStage);
//            regController.setClientController(clientController);
            initController(regController, registerStage);
            //   regController.setClientController(clientController);

            registerStage.setResizable(false);
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showOptionsLayout() {

        try {
            // новое окно переименования
            Stage optionsStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/options_modal.fxml"));

            // создаем сцену, задаем параметры
            VBox page = (VBox) loader.load();
            Scene scene = new Scene(page);
            optionsStage.setScene(scene);
            optionsStage.setTitle("Options");
            optionsStage.initModality(Modality.WINDOW_MODAL);
            optionsStage.initOwner(primaryStage);

            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            OptionsController optionsController = loader.getController();
            optionsController.setMainApp(this);
            optionsController.init();
            optionsController.setStage(optionsStage);
            optionsController.setClientController(clientController);
            optionsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProgressModalController getProgressController() {
        return progressController;
    }

    private ProgressModalController progressController;
//    public void showProgressLayout(String title, String fileName, File file) {
    public void showProgressLayout(String title) {

        try {
            // новое окно переименования
            Stage progressStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/progress_modal.fxml"));

            // создаем сцену, задаем параметры
            VBox page = (VBox) loader.load();
            Scene scene = new Scene(page);
            progressStage.setScene(scene);
            progressStage.setTitle(title);
            progressStage.initModality(Modality.WINDOW_MODAL);
            progressStage.initOwner(primaryStage);

            ProgressBar pb = (ProgressBar)  page.getChildren().get(1);
            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
//            ProgressModalController progressController = loader.getController();
            progressController = new ProgressModalController();
            progressController.setMainApp(this);
            progressController.setProgressBar(pb);
            // progressController.setCurrentName(fileName);
            progressController.setStage(progressStage);
            progressController.setModalController(progressController);
         //   progressController.setFile(file);
            progressController.setClientController(clientController);
            progressStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showSyncLayout( ) {

        try {
            // новое окно переименования
            Stage syncStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FileBoxClientStart.class.getResource("fxml/sync_layout.fxml"));

            // создаем сцену, задаем параметры
           HBox page = (HBox) loader.load();
            Scene scene = new Scene(page);
            syncStage.setScene(scene);
            syncStage.setTitle("Synchronize your FileBox");
            syncStage.initModality(Modality.WINDOW_MODAL);
            syncStage.initOwner(primaryStage);


            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            SyncController syncController = loader.getController();
            syncController.setMainApp(this);
            // progressController.setCurrentName(fileName);
            syncController.setStage(syncStage);
            //   progressController.setFile(file);
            syncController.setClientController(clientController);
            syncStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showDioalogLayout(String title, String message) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        dialog.setTitle(title);
        buttons.getChildren().addAll(new Button("Ok"), new Button("Cancel"));
        box.getChildren().addAll(new Label(message), new TextField(), buttons);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.show();
    }
}
