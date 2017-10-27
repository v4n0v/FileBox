package ru.geekbrains.filebox.library;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter {


    @Override
    public Object unmarshal(Object v) throws Exception {
        return LocalDate.parse((CharSequence) v);
    }

    @Override
    public Object marshal(Object v) throws Exception {
        return v.toString();
    }
}
