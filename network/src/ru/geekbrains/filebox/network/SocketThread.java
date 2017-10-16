package ru.geekbrains.filebox.network;

import ru.geekbrains.filebox.network.packet.AbstractPacket;
import ru.geekbrains.filebox.network.packet.FileBoxPacketManager;
import ru.geekbrains.filebox.network.packet.PackageType;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SocketThread extends Thread implements FileBoxPacketManager{

    private final SocketThreadListener eventListener;
    private final Socket socket;
    private DataOutputStream outD;
    private FileOutputStream outFile;
    private BufferedInputStream bis;

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
//            outD = new DataOutputStream(socket.getOutputStream());
//         //   out = new DataOutputStream(socket.getOutputStream());
//            System.out.println("Waiting for upload...");
            eventListener.onReadySocketThread(this, socket);
            while (!isInterrupted()) {
               // receivePacket();
//                InputStream in = socket.getInputStream();
//                DataInputStream din = new DataInputStream(in);
//                System.out.println(din.getClass().getCanonicalName());
//                int filesCount = din.readInt();//получаем количество файлов
//                System.out.println("Uploading " + filesCount + " files\n");
//                for (int i = 0; i < filesCount; i++) {
//                    System.out.println("Receiving N" + (i+1) + " file: \n");
//                    long fileSize = din.readLong(); // получаем размер файла
//                    String fileName = din.readUTF(); //прием имени файла
//                    System.out.println("File name: " + fileName+"\n");
//                    System.out.println("File size: " + fileSize + " byte\n");
//                    byte[] buffer = new byte[64 * 1024];
//                    FileOutputStream outFile = new FileOutputStream(fileName);
////                    FileOutputStream outF = new FileOutputStream(fileName);
//                    int count, total = 0;
//
//                    while ((count = din.read(buffer)) != -1) {
//                        total += count;
//                        outFile.write(buffer, 0, count);
//                        if (total == fileSize) {
//                            break;
//                        }
//                    }
//                    outFile.flush();
//                    outFile.close();
//                    System.out.println("Upload complete\n");
//                    // обрабатываем полученный файл
                InputStream in = socket.getInputStream();
                //  DataInputStream din = new DataInputStream(in);
                //   FileInputStream fis = new FileInputStream("temp.out");
                ObjectInputStream oin = new ObjectInputStream(in);
                //   if (oin.readObject() instanceof SerialTest) {
                packet = (AbstractPacket) oin.readObject();
                //System.out.println("version=" + ts.version);

                    eventListener.onReceivePacket(this, socket, packet);
              //  }
            }
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }catch (ClassNotFoundException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                eventListener.onExceptionSocketThread(this, socket, e);
            }
            eventListener.onStopSocketThread(this);
        }


    }

    public void sendFile(List<File> list) {
        int countFiles = list.size();

     //   DataOutputStream outD;
        try {
            outD = new DataOutputStream(socket.getOutputStream());

            outD.writeInt(countFiles);//отсылаем количество файлов

            for (int i = 0; i < countFiles; i++) {
                File f = list.get(i);

                outD.writeLong(f.length());//отсылаем размер файла
                outD.writeUTF(f.getName());//отсылаем имя файла

                FileInputStream in = new FileInputStream(f);
                byte[] buffer = new byte[64 * 1024];
                int count;

                while ((count = in.read(buffer)) != -1) {
                    outD.write(buffer, 0, count);
                }
                outD.flush();
                in.close();
            }
            //close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
            close();
        }
    }
//    public synchronized void sendMsg(String msg) {
//        try {
//            outMsg.writeUTF(msg);
//            outMsg.flush();
//        } catch (IOException e) {
//            eventListener.onExceptionSocketThread(this, socket, e);
//            close();
//        }
//    }
    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
    }

    @Override
    public void sendPacket(AbstractPacket outPacket) {

       try {
           outD = new DataOutputStream(socket.getOutputStream());
        //   FileOutputStream fos = new FileOutputStream("temp.out");
           ObjectOutputStream oos = new ObjectOutputStream(outD);
           oos.writeObject(outPacket);
           oos.flush();
           oos.close();
       } catch (IOException e){
           eventListener.onExceptionSocketThread(this, socket, e);
       }
    }

    @Override
    public void receivePacket( ) {
        try {
            InputStream in = socket.getInputStream();
          //  DataInputStream din = new DataInputStream(in);
         //   FileInputStream fis = new FileInputStream("temp.out");
            ObjectInputStream oin = new ObjectInputStream(in);
            //   if (oin.readObject() instanceof SerialTest) {
            packet = (AbstractPacket) oin.readObject();
            //System.out.println("version=" + ts.version);
        } catch (IOException oie ){
            eventListener.onExceptionSocketThread(this, socket, oie);
        } catch (ClassNotFoundException cl){
            eventListener.onExceptionSocketThread(this, socket, cl);
        }
    }
}
