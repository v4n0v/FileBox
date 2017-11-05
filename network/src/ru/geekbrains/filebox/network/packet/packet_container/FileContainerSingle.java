package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;
import java.util.ArrayList;

public class FileContainerSingle implements Serializable {
    private byte[] files;
    private String name;
    private long size;
    // количество файлов в пакете
    int count;

    public FileContainerSingle(){}
//    public FileContainerSingle(byte[] inFile, String name, long size) {
//        this.name = name;
//        this.files = inFile;
//        this.size = size;
//    }

    public void addFile(byte[] inFile, String name, long size, int count) {
        this.name = name;
        this.files = inFile;
        this.size = size;
        this.count = count;
    }

    public byte[] getFile() {
        return files;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}