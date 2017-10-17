package ru.geekbrains.filebox.network.packet;

public class MessagePacket extends AbstractPacket{
    MessagePacket(String msg) {
        super(PackageType.MESSAGE, msg);
    }

}
