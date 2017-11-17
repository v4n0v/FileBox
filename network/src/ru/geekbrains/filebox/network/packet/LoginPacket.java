package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.LoginContainer;

public class LoginPacket extends Packet {
    public LoginPacket(String email, String password) {
        super(PackageType.LOGIN, new LoginContainer(email, password));
    }
}
