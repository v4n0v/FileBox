package ru.geekbrains.filebox.network.packet.packet_container;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;

public class FileListContainer implements Serializable {
 //  private ObservableList<FileListElement> personData = FXCollections.observableArrayList();
    private ArrayList<FileListElement> fileListElements;


    private Integer usedSpace;
    public FileListContainer() {
        fileListElements=new ArrayList<>();
    }
    public void add(FileListElement element){
        this.fileListElements.add(element);
    }
    public void setUsedSpace(int space){
        this.usedSpace=space;
    }

    public int getUsedSpace() {
        return usedSpace;
    }

    public ArrayList getList(){
        return fileListElements;

    }
}
