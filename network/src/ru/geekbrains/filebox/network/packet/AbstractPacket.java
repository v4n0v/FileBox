package ru.geekbrains.filebox.network.packet;

import java.io.*;
import java.util.ArrayList;

public abstract class AbstractPacket<T> implements Serializable {

    private PackageType packetType;
    private T outputPacket;

    AbstractPacket(PackageType packageType, T outputPacket){
        this.packetType =packageType;
        this.outputPacket=outputPacket;
    }

    public void setOutputPackage(T outPackage){
        this.outputPacket=outPackage;
    }
    public void setPacketType(PackageType packetType) {
        this.packetType = packetType;
    }

    public PackageType getPacketType() {
        return packetType;
    }

    public T getOutputPacket() {
        return outputPacket;
    }

}
