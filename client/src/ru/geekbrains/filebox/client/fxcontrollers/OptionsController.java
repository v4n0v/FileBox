package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.geekbrains.filebox.client.core.ClientPreferences;
import ru.geekbrains.filebox.client.core.preferences.Style;
import ru.geekbrains.filebox.library.AlertWindow;
import ru.geekbrains.filebox.library.Log2File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class OptionsController extends BaseController implements InitLayout {
    public void setPrefereces(ClientPreferences prefereces) {
        this.prefereces = prefereces;
    }

    private String STYLES_PATH = "client\\src\\ru\\geekbrains\\filebox\\client\\css\\styles\\";
    private String CURRENT_STYLE_PATH = "client\\src\\ru\\geekbrains\\filebox\\client\\css\\";

    private ClientPreferences prefereces;
    private ObservableList<Style>styles =FXCollections.observableArrayList();
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

    private String path;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private Stage primaryStage;

    @Override
    public void init() {

        inboxPath.setText(mainApp.getConfig().getPath());
        File folder = new File(STYLES_PATH);
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

        Log2File.writeLog("Options opened");
    }

    public void selectDir() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(null);
        if (dir != null) {
            path = dir.getAbsolutePath();
            mainApp.getConfig().setPath(path);
            inboxPath.setText(path);
        } else {
            AlertWindow.warningMesage("Directory not chosen");
            Log2File.writeLog(Level.WARNING, "Directory not chosen");
        }
    }


    @FXML
    private void okAndSave() {
        ClientPreferences config = new ClientPreferences();
        Style selectedStyle = (Style) styleChoice.getSelectionModel().getSelectedItem();
        System.out.println(selectedStyle.getStyle());


        String absPath = mainApp.getCurrentStyleCSS();
        // если выбран новый стиль, то заменяем им файл текущего стиля
        if (selectedStyle != null) {
            File newStyleFile = new File(STYLES_PATH + selectedStyle.getStyle() + ".css");
            absPath = "file:///" + newStyleFile.getAbsolutePath().replace("\\", "/");
            mainApp.setCurrentStyleCSS(absPath);
//            Platform.runLater(() -> {
            primaryStage.getScene().getStylesheets().clear();
            primaryStage.getScene().getStylesheets().add(absPath);
//            });

        }

        // если
        if (inboxPath != null) {
            try {
                JAXBContext context = JAXBContext.newInstance(ClientPreferences.class);

                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                System.out.println("Saving config");
                config.setPath(inboxPath.getText());
                File cfg = new File("config.xml");
                List<Style> l = new ArrayList<>();

                l.addAll(styles);
                config.setStyleList(l);
                config.setCurrentStyle(absPath);
                m.marshal(config, cfg);
            } catch (Exception e) { // catches ANY exception
                e.printStackTrace();
                AlertWindow.errorMesage(e.getMessage());
            }
            stage.close();
            Log2File.writeLog("Config saved");
        } else {
            AlertWindow.warningMesage("Select folder");

        }
        System.out.println("saveConfig");

    }


}
