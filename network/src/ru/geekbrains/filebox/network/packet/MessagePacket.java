package ru.geekbrains.filebox.network.packet;

public class MessagePacket extends AbstractPacket{
    public MessagePacket(String msg) {
        super(PackageType.MESSAGE, msg);
    }

}
