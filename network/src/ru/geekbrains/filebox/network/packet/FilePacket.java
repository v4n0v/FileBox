package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;

public class FilePacket extends Packet {

    public FilePacket(FileContainerSingle files) {
//    public FilePacket(FileContainer files) {
        super(PackageType.FILE, files);

    }

}
