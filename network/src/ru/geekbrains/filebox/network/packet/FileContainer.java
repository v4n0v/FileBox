package ru.geekbrains.filebox.network.packet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileContainer implements Serializable{
    private ArrayList<byte[]> files = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

//    public FileContainer(ArrayList<byte[]> file, ArrayList<String> name) {
//        this.files = file;
//        this.names = name;
//    }
    public FileContainer(){

    }
    public void addFile(byte[] inFile, String name){
            names.add(name);
        //byte[] files= Files.readAllBytes(Paths.get(inFile.getPath()));
            files.add(inFile);

    }

    public ArrayList<byte[]> getFiles() {
        return files;
    }

    public ArrayList<String> getNames() {
        return names;
    }
}
