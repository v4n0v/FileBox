package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class FileListElement implements Serializable{

    private String fileName;
    private long fileSize;

    public FileListElement(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


}
