package ru.geekbrains.filebox.client.core;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class FileListXMLElement {

    private StringProperty fileName;
    private LongProperty fileSize;

    public FileListXMLElement(String fileName, long fileSize) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileSize = new SimpleLongProperty(fileSize);
    }

    public StringProperty getFileName() {
        return fileName;
    }
    public void setFileName(String name) {
        this.fileName = new SimpleStringProperty(name);
    }

    public LongProperty getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = new SimpleLongProperty(fileSize);
    }

}
