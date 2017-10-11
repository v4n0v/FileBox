package ru.geekbrains.filebox.network;

import java.io.*;
import java.net.Socket;

public class SocketThread extends Thread {

    private final SocketThreadListener eventListener;
    private final Socket socket;
    private DataOutputStream out;
    private FileOutputStream outFile;


    public SocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(name);
        this.eventListener=eventListener;
        this.socket=socket;
        start();
    }


    @Override
    public void run() {
        eventListener.onStartSocketThread(this);
        try{
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

//            FileOutputStream out = new FileOutputStream("1.txt");
//            for (int i = 0; i < 5; i++) {
//                System.out.print(65+i+" ");
//                out.write(65+i);
//            }
//            out.close();
//            System.out.println( );
//            FileInputStream in = new FileInputStream("1.txt");
//            int x;
//            while ((x=in.read()) !=-1){
//                System.out.print((char) x);
//            }
//            in.close();


//            try(FileInputStream fin=new FileInputStream("C://SomeDir//note.txt"))
//            {
//                System.out.println("Размер файла: " + fin.available() + " байт(а)");
//
//                int i=-1;
//                while((i=fin.read())!=-1){
//
//                    System.out.print((char)i);
//                }
//            }  catch(IOException ex){

//            System.out.println(ex.getMessage());
//        }



            eventListener.onReadySocketThread(this, socket);
            while (!isInterrupted()){
                String msg=in.readUTF();
               // File file = new File("11.ss");
                eventListener.onReceiveString(this, socket, msg);
              //  eventListener.onReceiveFile(this, socket, file);
            }

        } catch (IOException e){
            eventListener.onExceptionSocketThread(this,socket, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();eventListener.onExceptionSocketThread(this, socket, e);
            }
            eventListener.onStopSocketThread(this);
        }
    }

    public void sendFile(File file){

//        try(FileInputStream fin=new FileInputStream("C://SomeDir//notes.txt");
//            FileOutputStream fos=new FileOutputStream("C://SomeDir//notes_new.txt"))
//        {
//            byte[] buffer = new byte[fin.available()];
//            // считываем буфер
//            fin.read(buffer, 0, buffer.length);
//            // записываем из буфера в файл
//            fos.write(buffer, 0, buffer.length);
//        }
//        catch(IOException ex){
//
//            System.out.println(ex.getMessage());
//        }
    }
    public synchronized void close(){
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this, socket, e);
        }
    }
}
