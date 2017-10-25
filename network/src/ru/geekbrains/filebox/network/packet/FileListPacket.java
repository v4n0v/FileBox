package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.FileListContainer;

import java.util.ArrayList;

public class FileListPacket extends Packet{
    public FileListPacket(ArrayList<String> fileList) {
        super(PackageType.FILE_LIST,  fileList);
    }
}
