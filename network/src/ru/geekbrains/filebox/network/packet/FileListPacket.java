package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.FileListContainer;
import ru.geekbrains.filebox.network.packet.packet_container.FileListElement;

import java.util.ArrayList;

public class FileListPacket extends Packet{
//    public FileListPacket(ArrayList<FileListElement> fileList) {
    public FileListPacket(FileListContainer fileList) {
        super(PackageType.FILE_LIST,  fileList);
    }
}
