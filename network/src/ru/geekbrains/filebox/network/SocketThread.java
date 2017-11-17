package ru.geekbrains.filebox.network;

import com.sun.xml.internal.ws.api.policy.PolicyResolver;
import ru.geekbrains.filebox.network.packet.FilePacket;
import ru.geekbrains.filebox.network.packet.PackageType;
import ru.geekbrains.filebox.network.packet.Packet;
import ru.geekbrains.filebox.network.packet.packet_container.FileContainer;

import java.io.*;
import java.net.Socket;

public class SocketThread extends Thread {

    private final SocketThreadListener eventListener;

    public Socket getSocket() {
        return socket;
    }

    private final Socket socket;
    private DataOutputStream outD;
    private DataInputStream inD;
    private FileOutputStream outFile;
    private Packet packet;


    public SocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(name);
        this.eventListener = eventListener;
        this.socket = socket;
        start();
    }

    // цикл сокета, пока не разорвано соединение он повторяется, ожидая пакет
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
                packet = (Packet) oin.readObject();
                eventListener.onReceivePacket(this, socket, packet);

            }
        } catch (IOException | ClassNotFoundException e) {
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

    // закрываем соединение
    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
    }

    // отправка пакета
    public void sendPacket(Packet outPacket) {
        try {

             outD = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream oos = new ObjectOutputStream(outD);

            oos.writeObject(outPacket);
            outD.flush();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
            close();
        }
    }
}

