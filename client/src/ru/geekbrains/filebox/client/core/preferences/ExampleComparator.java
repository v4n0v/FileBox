package ru.geekbrains.filebox.client.core.preferences;

import ru.geekbrains.filebox.client.core.FileListXMLElement;

import java.util.Comparator;

public class ExampleComparator  implements Comparator<FileListXMLElement> {

    @Override
    public int compare(FileListXMLElement o1, FileListXMLElement o2) {
        String obj1= o1.getFileName().toString();
        String obj2= o2.getFileName().toString();
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