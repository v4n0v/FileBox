package ru.geekbrains.filebox.network;

import ru.geekbrains.filebox.network.packet.AbstractPacket;
import ru.geekbrains.filebox.network.packet.ErrorPacket;
import ru.geekbrains.filebox.network.packet.FileBoxPacketManager;
import ru.geekbrains.filebox.network.packet.PackageType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SocketThread extends Thread {

    private final SocketThreadListener eventListener;
    private final Socket socket;
    private DataOutputStream outD;
    private FileOutputStream outFile;


    private AbstractPacket packet;


    public SocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(name);
        this.eventListener = eventListener;
        this.socket = socket;
        start();
    }


    @Override
    public void run() {
        eventListener.onStartSocketThread(this);
        try {
            //
            outD = new DataOutputStream(socket.getOutputStream());
            eventListener.onReadySocketThread(this, socket);
            while (!isInterrupted()) {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                ObjectInputStream oin = new ObjectInputStream(dis);
                packet = (AbstractPacket) oin.readObject();
                eventListener.onReceivePacket(this, socket, packet);

            }
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        } catch (ClassNotFoundException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                eventListener.onExceptionSocketThread(this, socket, e);
            }
            eventListener.onStopSocketThread(this);
        }
    }


    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
    }



    public void sendPacket(AbstractPacket outPacket) {

        try {
            outD = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream oos = new ObjectOutputStream(outD);
            oos.writeObject(outPacket);
            outD.flush();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }


    }
}

