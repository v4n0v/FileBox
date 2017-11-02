package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class FileListElement implements Serializable{

    private String fileName;
//    private long fileSize;
    private Long fileSize;

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
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }


}
