package ru.geekbrains.filebox.client.core.preferences;

import javax.xml.bind.annotation.XmlElement;

public class Style {
    String style;
    Style(){}
    public Style(String style){
        this.style=style;
    }

    @XmlElement(name="style")
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style= style;
    }

    @Override
    public String toString() {
        return style;
    }
}
