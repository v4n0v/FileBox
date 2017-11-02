package ru.geekbrains.filebox.network.packet;

import java.util.HashMap;

public class FileOperationPacket extends Packet{

   public FileOperationPacket(PackageType type, String key) {
        super(type, key);

    }
}
