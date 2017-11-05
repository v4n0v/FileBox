package ru.geekbrains.filebox.client.core;

import ru.geekbrains.filebox.library.AlertWindow;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClientPreferences {


    private String clientFolder;
    private ArrayList<String> skinList;
    private String login;
    private File prefFile = new File("client.cfg");
    private String config;
    private final String SEPARATOR = "<==>";
    private final String MULTIPLE_SEPARATOR = "<!!!>";
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public ClientPreferences( ) {

    }

    public String getClientFolder() {
        return clientFolder;
    }

    public void setClientFolder(String clientFolder) {
        this.clientFolder = clientFolder;
    }

    public String getSkin(int i) {
        return skinList.get(i);
    }
    public ArrayList<String> getSkinList() {
        return skinList;
    }
    public void addSkin(String skin) {
        this.skinList.add(skin);
    }
    public void loadConfigFromFile(File file ) {
        String cfg;


        try {
            cfg= new String(Files.readAllBytes(Paths.get(prefFile.getName())));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void saveConfigToFile( ) {
        config="";
        // сохраняем папку
        config+="client_path="+SEPARATOR+clientFolder+"\n";
        // сохраняем скины
        config+="client_skins="+SEPARATOR;
        for (int i = 0; i < skinList.size(); i++) {
            config+=skinList.get(i)+MULTIPLE_SEPARATOR;
        }

        try(FileWriter writer = new FileWriter(prefFile, false))
        {
            // запись всей строки
            writer.write(config);
            // запись по символам
//            writer.append('\n');
//            writer.append('E');
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }



}
