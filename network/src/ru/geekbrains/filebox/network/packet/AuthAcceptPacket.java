package ru.geekbrains.filebox.network.packet;

public class AuthAcceptPacket extends Packet {

    public AuthAcceptPacket(boolean isAuth) {
        super(PackageType.AUTH_ACCEPT, (Boolean) isAuth);
    }
}
