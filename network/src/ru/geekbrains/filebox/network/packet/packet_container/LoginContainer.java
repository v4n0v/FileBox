package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class LoginContainer implements Serializable{

    private String login;
    private String password;
    public LoginContainer(String login, String password) {

        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }


}
