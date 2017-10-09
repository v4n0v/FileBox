package ru.geekbrains.filebox.server.core.authorization;

import java.sql.SQLException;

public interface LoginManager {
    //boolean isExist (String login);
    void init();

    String getMail(String login) ;
    int getSpace(String login);

    void dispose();
}
