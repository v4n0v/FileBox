package ru.geekbrains.filebox.client.core;

import javafx.beans.property.*;

import java.util.Comparator;


public class FileListXMLElement  {

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


    public static Comparator<FileListXMLElement> FileNameComparator = new Comparator<FileListXMLElement>() {

        @Override
        public int compare(FileListXMLElement o1, FileListXMLElement o2) {
            String obj1 = o1.getFileName().getValue();
            String obj2 = o2.getFileName().getValue();
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            if (obj1.equals( obj2 )) {
                return 0;
            }
            return obj1.compareTo(obj2);
        }
    };




    public class ExampleComparator  implements Comparator<String> {
        public int compare(String obj1, String obj2) {
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            if (obj1.equals( obj2 )) {
                return 0;
            }
            return obj1.compareTo(obj2);
        }
    }
}
