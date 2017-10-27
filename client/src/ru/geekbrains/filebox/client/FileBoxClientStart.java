package ru.geekbrains.filebox.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.core.FileListWrapper;
import ru.geekbrains.filebox.client.core.FileListXMLElement;
import ru.geekbrains.filebox.client.fxcontrollers.ClientController;
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
            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);
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

        // Set the application icon.
//        this.primaryStage.getIcons().add(
//                new Image("file:resources/images/ico.png"));
        initClientLayout();
    //    loadFileListDataFromFile();
        controller.initClientLoginLayout();
        controller.updateTable();
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
     *
     * @param file - файл или null, чтобы удалить путь
     */
    public void setFileListFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(FileBoxClientStart.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Обновление заглавия сцены.
            primaryStage.setTitle("FileBox - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Обновление заглавия сцены.
            primaryStage.setTitle("FileBox");
        }
    }


//    public void loadFileListDataFromFile(File file) {
    public void loadFileListDataFromFile( ) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(FileListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();
            File file = new File("fblist.xml");
            System.out.println(file.getAbsolutePath());
            // Reading XML from the file and unmarshalling.
            FileListWrapper wrapper = (FileListWrapper) um.unmarshal(file);

            fileListData.clear();
            fileListData.addAll(wrapper.getFiles());

            // Save the file path to the registry.
            setFileListFilePath(file);

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

            // Сохраняем путь к файлу в реестре.
       //     setFileListFilePath(file);
        } catch (Exception e) { // catches ANY exception
            AlertWindow.errorMesage("Could not save data to file:\n" + file.getPath()+"\n"+e.getMessage());
        }
    }
}
