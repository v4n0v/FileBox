package ru.geekbrains.filebox.network;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class SocketThread extends Thread {

    private final SocketThreadListener eventListener;
    private final Socket socket;
    private DataOutputStream outD;
    private FileOutputStream outFile;
    private BufferedInputStream bis;

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
            outD = new DataOutputStream(socket.getOutputStream());
            System.out.println("Wait connect...");
            eventListener.onReadySocketThread(this, socket);
            while (!isInterrupted()) {
        //    while (true) {

                InputStream in = socket.getInputStream();
                DataInputStream din = new DataInputStream(in);

                int filesCount = din.readInt();//получаем количество файлов
                System.out.println("Uploading " + filesCount + " files\n");
                for (int i = 0; i < filesCount; i++) {
                    System.out.println("Receiving N" + (i+1) + " file: \n");
                    long fileSize = din.readLong(); // получаем размер файла
                    String fileName = din.readUTF(); //прием имени файла
                    System.out.println("File name: " + fileName+"\n");
                    System.out.println("File size: " + fileSize + " byte\n");
                    byte[] buffer = new byte[64 * 1024];
                    FileOutputStream outFile = new FileOutputStream(fileName);
//                    FileOutputStream outF = new FileOutputStream(fileName);
                    int count, total = 0;

                    while ((count = din.read(buffer)) != -1) {
                        total += count;
                        outFile.write(buffer, 0, count);

                        if (total == fileSize) {
                            break;
                        }
                    }
                    outFile.flush();
                    outFile.close();
                    System.out.println("Upload complete\n");
                    // обрабатываем полученный файл
                    //eventListener.onReceiveFile(this, socket, fileName);
                }

            }

        } catch (IOException e) {
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

    public void sendFile(ArrayList<String> list) {
        int countFiles = list.size();

     //   DataOutputStream outD;
        try {
            outD = new DataOutputStream(socket.getOutputStream());

            outD.writeInt(countFiles);//отсылаем количество файлов

            for (int i = 0; i < countFiles; i++) {
                File f = new File(list.get(i));

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

    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
    }
}
