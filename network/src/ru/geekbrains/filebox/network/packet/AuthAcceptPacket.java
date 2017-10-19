package ru.geekbrains.filebox.network.packet;

public class AuthAcceptPacket extends MessagePacket {
    public AuthAcceptPacket(String login) {
        super(login);
    }
}
