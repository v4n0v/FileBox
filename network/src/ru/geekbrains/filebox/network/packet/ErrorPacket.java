package ru.geekbrains.filebox.network.packet;

public class ErrorPacket extends AbstractPacket {
    public ErrorPacket(String error) {
        super(PackageType.ERROR, error);
    }
}
