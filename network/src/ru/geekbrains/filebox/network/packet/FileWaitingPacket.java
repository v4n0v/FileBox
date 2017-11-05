package ru.geekbrains.filebox.network.packet;

public class FileWaitingPacket extends Packet{
    public FileWaitingPacket(int count) {
        super(PackageType.FILE_WAITING, (Integer) count);
    }
}
