package ru.geekbrains.filebox.network.packet;

public interface FileBoxPacketManager {
    void sendPacket(AbstractPacket outPacket);
    void receivePacket( );
}
