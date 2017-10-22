package ru.geekbrains.filebox.network.packet;

public class ErrorPacket extends Packet {
    public ErrorPacket(String error) {
        super(PackageType.ERROR, error);
    }
}
