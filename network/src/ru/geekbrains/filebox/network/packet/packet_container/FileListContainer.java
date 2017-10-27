package ru.geekbrains.filebox.network.packet.packet_container;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;

public class FileListContainer implements Serializable {
 //  private ObservableList<FileListElement> personData = FXCollections.observableArrayList();
    private ArrayList<FileListElement> fileListElements;

    public FileListContainer() {
        fileListElements=new ArrayList<>();
    }
    public void add(FileListElement element){
        this.fileListElements.add(element);
    }
    public ArrayList getList(){
        return fileListElements;

    }
}
