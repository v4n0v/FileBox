package ru.geekbrains.filebox.client.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "files")
public class FileListXMLWrapper {
    //  private String name;

    private List<FileListXMLElement> files;

    @XmlElement(name = "file")
    public List<FileListXMLElement> getFiles() {
        return files;
    }

    public void setFiles(List<FileListXMLElement> files) {
        this.files = files;
    }

}
