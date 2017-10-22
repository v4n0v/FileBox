package ru.geekbrains.filebox.network.packet;

import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;

public class FilePacket extends Packet {
 //   byte[] files= Files.readAllBytes(Paths.get(path));
 //ArrayList<String> fileNames;
//    public FilePacket(byte[] files) {
    public FilePacket(FileContainer files) {
        super(PackageType.FILE, files);
    //    this.fileNames=fileNames;
    }

}
