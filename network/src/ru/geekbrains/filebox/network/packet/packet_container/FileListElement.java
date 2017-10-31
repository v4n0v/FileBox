package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class FileListElement implements Serializable{

    private String fileName;
//    private long fileSize;
    private String fileSize;

    public FileListElement(String fileName, String fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileSize() {
        return fileSize;
    }
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }


}
