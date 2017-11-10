package ru.geekbrains.filebox.client.core;

import javafx.beans.property.*;


public class FileListXMLElement {

    private StringProperty fileName;
  //  private StringProperty fileSize;
    private SimpleObjectProperty<Long> fileSize;

    public String getType() {
        return type;
    }

    private String type;

    public FileListXMLElement() {
    this(null, null);
}
    public FileListXMLElement(String fileName, Long fileSize) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileSize = new SimpleObjectProperty<Long>(fileSize);
    }
    public FileListXMLElement(String fileName, Long fileSize, String type) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileSize = new SimpleObjectProperty<Long>(fileSize);
        this.type= type;
    }
    public StringProperty getFileName() {
        return fileName;
    }
    public void setFileName(String name) {
        this.fileName = new SimpleStringProperty(name);
    }

    public SimpleObjectProperty<Long> getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = new SimpleObjectProperty<Long>(fileSize);
    }

}
