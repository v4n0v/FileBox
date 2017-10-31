package ru.geekbrains.filebox.client.core;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class FileListXMLElement {

    private StringProperty fileName;
    private StringProperty fileSize;
//    private LongProperty fileSize;
    public FileListXMLElement() {
    this(null, null);
}
    public FileListXMLElement(String fileName, String fileSize) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileSize = new SimpleStringProperty(fileSize);
    }

    public StringProperty getFileName() {
        return fileName;
    }
    public void setFileName(String name) {
        this.fileName = new SimpleStringProperty(name);
    }

    public StringProperty getFileSize() {
        return fileSize;
    }
    public void setFileSize(String fileSize) {
        this.fileSize = new SimpleStringProperty(fileSize);
    }

}
