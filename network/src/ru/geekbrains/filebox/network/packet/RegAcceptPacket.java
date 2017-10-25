package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.AuthAcceptPacket;

public class RegAcceptPacket extends Packet{

    public RegAcceptPacket(boolean isRegged) {
        super(PackageType.REG_ACCEPT, (Boolean) isRegged);
    }
}
