package ru.geekbrains.filebox.client.core;

import ru.geekbrains.filebox.client.core.preferences.Style;
import ru.geekbrains.filebox.library.AlertWindow;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="config")
public class ClientPreferences {


    private List<Style> styleList;

    private String path;
    private String style;


//    public ClientPreferences getConfig() {
//        return config;
//    }

//    ClientPreferences config;
    @XmlElement(name="path")
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }


//    public List<Style> getStyleList() { return styleList; }
    @XmlElement(name="style_list")
    public void setStyleList(List<Style> styleList) {
        this.styleList = styleList;
    }
    public List<Style> getStyleList() {
        return styleList;
    }

    // текущий стиль, который система олжна выбрать для загрузки вначале
   @XmlElement(name="current_style")
    public String getCurrentStyle() {
        return style;
    }
    public void setCurrentStyle(String style) {
        this.style= style;
    }

    public void loadConfig() {
        System.out.println("loadConfig");

        File file = new File("config.xml");

        try {
            JAXBContext context = JAXBContext
                    .newInstance(ClientPreferences.class);
            Unmarshaller um = context.createUnmarshaller();

            System.out.println(file.getAbsolutePath());
            // Reading XML from the file and unmarshalling.
            ClientPreferences config = (ClientPreferences) um.unmarshal(file);
            this.styleList=config.getStyleList();
            this.path=config.getPath();
            this.style= config.getCurrentStyle();

        } catch (Exception e) { // catches ANY exception
            e.printStackTrace();
            AlertWindow.errorMesage(e.getMessage());
        }

    }
}
