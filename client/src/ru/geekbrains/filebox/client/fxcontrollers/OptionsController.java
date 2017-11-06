package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import ru.geekbrains.filebox.client.core.ClientPreferences;
import ru.geekbrains.filebox.client.core.FileListXMLWrapper;
import ru.geekbrains.filebox.client.core.preferences.Style;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Logger;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class OptionsController extends BaseController{
    public void setPrefereces(ClientPreferences prefereces) {
        this.prefereces = prefereces;
    }

    ClientPreferences prefereces;
    ObservableList<Style> styles =  FXCollections.observableArrayList();;
    @FXML
    TextField inboxPath;
    @FXML
    Button folder;
    @FXML
    Button okBtn;
    @FXML
    Button cancelBtn;
    @FXML
    ChoiceBox styleChoice;

    String path;

    @Override
    public void init() {

        inboxPath.setText( mainApp.getConfig().getPath());
        File folder = new File("client\\src\\ru\\geekbrains\\filebox\\client\\css");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File[] fList;
        fList = folder.listFiles();
        for (int i = 0; i < fList.length; i++) {
            String fileName = fList[i].getName();
            int dot = fileName.lastIndexOf('.');
            styles.add(new Style(fileName.substring(0, dot)));
        }

        styleChoice.setItems(FXCollections.observableArrayList(styles));


    }
    public void selectDir(){
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(null);
        if (dir != null) {
           path=dir.getAbsolutePath();
            inboxPath.setText(path);
        } else {
            AlertWindow.warningMesage("Directory not chosen");
        }
    }

    public void loginConfig() {
        System.out.println("loadConfig");

        File file = new File("config.xml");

        try {
            JAXBContext context = JAXBContext
                    .newInstance(FileListXMLWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            System.out.println(file.getAbsolutePath());
            // Reading XML from the file and unmarshalling.
            ClientPreferences wrapper = (ClientPreferences) um.unmarshal(file);


        } catch (Exception e) { // catches ANY exception
            e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());
        }

    }

    @FXML
    private void okAndSave(){
        ClientPreferences config = new ClientPreferences();
        if (inboxPath!=null) {
            try {
            JAXBContext context = JAXBContext.newInstance(ClientPreferences.class);

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            System.out.println("Saving config");
            config.setPath(inboxPath.getText());
            File cfg = new File("config.xml");
            List<Style> l = new ArrayList<>();
//            Style styleGray = new Style("Gray");
            l.addAll(styles);
            config.setStyleList(l);

            m.marshal(config, cfg);
            } catch (Exception e) { // catches ANY exception
                e.printStackTrace();
                AlertWindow.errorMesage(e.getMessage());
            }
            stage.close();
            Logger.writeLog("Config saved");
        } else {
            AlertWindow.warningMesage("Select folder");
        }
        System.out.println("saveConfig");

    }


}
