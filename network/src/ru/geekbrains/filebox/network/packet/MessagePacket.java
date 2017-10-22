package ru.geekbrains.filebox.network.packet;

public class MessagePacket extends Packet {
    public MessagePacket(String msg) {
        super(PackageType.MESSAGE, msg);
    }

}
