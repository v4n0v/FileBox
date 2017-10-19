package ru.geekbrains.filebox.network.packet.packet_container;

import java.io.Serializable;

public class LoginContainer implements Serializable{

    private String email;
    private String password;
    public LoginContainer(String email, String password) {

        this.email = email;
        this.password = password;
    }

    public String getMail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


}
