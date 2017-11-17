package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.FileContainerSingle;

public class FilePacket extends Packet {

    public FilePacket(FileContainerSingle files) {

        super(PackageType.FILE, files);

    }

}
