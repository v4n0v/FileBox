package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.RegContainer;

public class AddUserPacket extends Packet{
    public AddUserPacket(String login, String mail, String pass) {
        super(PackageType.REGISTRATION, new RegContainer(login, mail, pass));
    }
}
