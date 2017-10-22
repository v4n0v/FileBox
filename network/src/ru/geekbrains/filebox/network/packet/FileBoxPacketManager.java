package ru.geekbrains.filebox.network.packet;

public interface FileBoxPacketManager {
    void sendPacket(Packet outPacket);
    void receivePacket( );
}
