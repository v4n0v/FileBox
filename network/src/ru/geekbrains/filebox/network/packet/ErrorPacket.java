package ru.geekbrains.filebox.network.packet;

public class ErrorPacket extends AbstractPacket {
    ErrorPacket(String error) {
        super(PackageType.ERROR, error);
    }
}
