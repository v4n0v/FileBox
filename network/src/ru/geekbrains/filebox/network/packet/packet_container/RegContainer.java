package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class RegContainer implements Serializable {


    private String mail;
    private String login;
    private String password;
    public RegContainer(String login, String mail, String password) {
        this.mail=mail;
        this.login=login;
        this.password=password;
    }
    public String getMail() {
        return mail;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

}
