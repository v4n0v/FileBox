package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.geekbrains.filebox.client.core.FileListWrapper;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.client.core.FileListXMLWrapper;
import ru.geekbrains.filebox.client.fxcontrollers.ClientController;
import ru.geekbrains.filebox.client.fxcontrollers.RenameController;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.prefs.Preferences;

public class FileBoxClientStart extends Application {
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public FileBoxClientStart getMainApp(){
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

    public void fillFileList(ArrayList<FileListElement> list){
        fileListData.clear();
        for (int i = 0; i < list.size(); i++) {
            fileListData.add(list.get(i));
        }
    }

    private Stage primaryStage;
    private VBox loggedRootElement;

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
            primaryStage.setResizable(false);
            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);
         //   controller.updateTable();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        // подгружаем последний список файлов из XML


    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("FileBoxClientManager");
        //инициализируем окно клиента
        initClientLayout();
        //инициализируем модальное окно логина
        controller.initClientLoginLayout();

        // связываем таблицу и ObservableList  данными
        controller.initTable();

//        // получаем последний сохранненный  список файлов
//        File file = getFileListFilePath();
//        if (file != null) {
//            loadFileListDataFromFile(file);
//        }
        // обновляем время последней синхронизации
        controller.lastUpdate();
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
    public void setFileListFilePath(File file ) {
       // File file = new File("fblist.xml");
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
    public void loadFileListDataFromFile(File file ) {
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
            setFileListFilePath(file );

        } catch (Exception e) { // catches ANY exception
           e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());
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
            System.out.println( "file '"+file+"' marshalised");
            // Сохраняем путь к файлу в реестре.
       //     setFileListFilePath(file);
        } catch (Exception e) { // catches ANY exception
            AlertWindow.errorMesage("Could not save data to file:\n" + file.getPath()+"\n"+e.getMessage());
        }
    }
    public void removeFromTable(String name){
        FileListXMLElement element;
        for (int i = 0; i < fileListDataProp.size(); i++) {
            element = fileListDataProp.get(i);
            if (name.equals(element.getFileName().getValue())){
                fileListDataProp.remove(i);
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

            // создаем сцену, задаем параметры
            HBox page = (HBox) loader.load();
            Scene scene = new Scene(page);
            renameStage.setScene(scene);
            renameStage.setTitle("Rename "+fileName);
            renameStage.initModality(Modality.WINDOW_MODAL);
            renameStage.initOwner(primaryStage);

            // получаем контроллер текущей сцены и передаем в него ссылку на текущий класс
            RenameController renameController = loader.getController();
            renameController.setMainApp(this);
            renameController.setCurrentName(fileName);
            renameController.setDialogStage(renameStage);
            renameController.setClientController(controller);
            renameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDioalogLayout(String title, String message ){
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
