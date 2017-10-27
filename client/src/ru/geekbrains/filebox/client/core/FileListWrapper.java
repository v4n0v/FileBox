package ru.geekbrains.filebox.client.core;

import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name = "files")
//@XmlType(propOrder = {"name", "size"})
public class FileListWrapper {
 //  private String name;

    private List<FileListElement> files;

    @XmlElement(name = "file")
    public List<FileListElement> getFiles() {
        return files;
    }

    public void setFiles(List<FileListElement> files) {
        this.files = files;
    }

}
