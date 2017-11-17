package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class FileListElement implements Serializable{

    private String fileName;

    private Long fileSize;


    private String type;
    public FileListElement(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    public FileListElement(String fileName, long fileSize, String type) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.type=type;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
