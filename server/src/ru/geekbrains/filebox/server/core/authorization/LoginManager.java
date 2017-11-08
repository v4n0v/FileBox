package ru.geekbrains.filebox.server.core.authorization;

import java.sql.SQLException;

public interface LoginManager {

    void init();

    String getMail(String mail) ;
  //  String getLogin(String login, String pass) ;
    int getSpace();

    boolean isLoginAndPassCorrect(String login, int pass) ;
//    boolean isLoginAndPassCorrect(String login, String pass) ;
    void addNewUser(String login, String mail, int passHash);
//    void addNewUser(String login, String mail, String pass);
    void dispose();
}
