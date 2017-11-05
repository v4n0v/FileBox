package ru.geekbrains.filebox.client.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "files")
public class ClientPreferencesWrapper {


    private List<ClientPreferencesList> options;

    @XmlElement(name = "file")
    public List<ClientPreferencesList> getOptions() {
        return options;
    }

    public void setOptions(List<ClientPreferencesList> options) {
        this.options = options;
    }

}


