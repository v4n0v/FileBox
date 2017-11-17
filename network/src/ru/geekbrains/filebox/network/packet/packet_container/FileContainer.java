package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;
import java.util.ArrayList;

public class FileContainer implements Serializable {
    private ArrayList<byte[]> files = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

    public FileContainer() {}

    public void addFile(byte[] inFile, String name) {
        names.add(name);
        files.add(inFile);
    }

    public ArrayList<byte[]> getFiles() {
        return files;
    }
    public ArrayList<String> getNames() {
        return names;
    }
}
