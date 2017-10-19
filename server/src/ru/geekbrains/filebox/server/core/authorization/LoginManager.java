package ru.geekbrains.filebox.server.core.authorization;

import java.sql.SQLException;

public interface LoginManager {

    void init();

    String getMail(String mail) ;
  //  String getMail(String login, String pass) ;
    int getSpace();
    String getLogin(String login, String pass) ;

    void dispose();
}
