package ru.geekbrains.filebox.client.core;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientPreferencesList {

    String cfgPath;
    ArrayList<String> styleSpinner;

    public String getPath() {
        return cfgPath;
    }

    public void setCfgPath(String cfgPath) {
        this.cfgPath = cfgPath;
    }

    public ArrayList<String> getStyleSpinner() {
        return styleSpinner;
    }

    public void setStyleSpinner(ArrayList<String> styleSpinner) {
        this.styleSpinner = styleSpinner;
    }

    public void setStyleSpinner(String style) {
        styleSpinner.add(style);
    }

    public void removeSpinner(String style) {
        styleSpinner.remove(style);
    }

    public void removeSpinner(int key) {
        styleSpinner.remove(key);
    }
}
