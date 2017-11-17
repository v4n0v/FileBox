package ru.geekbrains.filebox.network.packet;

public class RegAcceptPacket extends Packet {
    public RegAcceptPacket(boolean isRegged) {
        super(PackageType.REG_ACCEPT, (Boolean) isRegged);
    }
}
