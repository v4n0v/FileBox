package ru.geekbrains.filebox.client;

import ru.geekbrains.filebox.client.core.FileBoxClient;
import ru.geekbrains.filebox.network.SocketThread;
import ru.geekbrains.filebox.network.SocketThreadListener;
import ru.geekbrains.filebox.network.packet.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class FileBoxClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    enum State {CONNECTED, NOT_CONNECTED};
    State state = State.NOT_CONNECTED;


    ArrayList<String> selectFiles;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    //    private static final int POS_X= (screenSize.width - WIDTH) / 2;
//    private static final int POS_Y= (screenSize.height-HEIGHT)/2;
    private static final String TITLE = "FileBox Client";

    private static final String LOGIN = "Login";
    private static final String LOGOUT= "LogOut";
    private static final String REGISTER = "Register";
    private final String IP_ADRESS = "localhost";
  //  private final String IP_ADRESS = "127.0.0.1";
    private final int PORT = 8189;

    private static final String DOWNLOAD = "Download";
    private static final String UPLOAD = "Upload";
    private static final String RENAME = "Rename";
    private static final String DELETE = "DELETE";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileBoxClientGUI();
            }
        });
    }

    private final JPanel upperPanel = new JPanel(new GridLayout(1, 3));

    private final JTextArea log = new JTextArea();
    private final JTextField fieldLogin = new JTextField("root");
    private final JPasswordField fieldPassword = new JPasswordField("1234567");
    private final JButton btnLogin = new JButton(LOGIN);
    private final JButton btnLogout = new JButton(LOGOUT);
    FileBoxClient client = new FileBoxClient();
  //  private final JPanel midPanel = new JPanel(new BorderLayout());
    private final JList<String> fileList = new JList<>();
    private final JPanel midButtonsPanel = new JPanel(new GridLayout(5, 1));
    private final JButton btnDownload = new JButton(DOWNLOAD);
    private final JButton btnUpload = new JButton(UPLOAD);
    private final JButton btnRename = new JButton(RENAME);
    private final JButton btnDelete = new JButton(DELETE);
    private final JFileChooser fileChooser = new JFileChooser();

    private SocketThread socketThread;

    private FileBoxClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(TITLE);

        btnLogin.addActionListener(this);
        btnLogout.addActionListener(this);
        btnDownload.addActionListener(this);
        btnUpload.addActionListener(this);
        btnRename.addActionListener(this);
        btnDelete.addActionListener(this);


        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPassword);
        upperPanel.add(btnLogin);

        System.out.println(upperPanel.getHeight());

        add(upperPanel, BorderLayout.NORTH);

        midButtonsPanel.add(btnDownload);
        midButtonsPanel.add(btnUpload);
        midButtonsPanel.add(btnRename);
        midButtonsPanel.add(btnDelete);
        midButtonsPanel.add(btnLogout);
        add(fileList, BorderLayout.CENTER);
        add(midButtonsPanel, BorderLayout.EAST);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);


        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnLogin) {
            if (state==State.NOT_CONNECTED)
           connect();
            else
                disconect();
        } else if (src == btnDownload) {
             client.downloadFile();

        } else if (src == btnUpload) {
         //   fileChooser.showOpenDialog(this);
            fileChooser.setMultiSelectionEnabled(true);
            selectFiles = new ArrayList<String>();
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                log.append("Files selected to upload:\n" );
                File[] file = fileChooser.getSelectedFiles();
                for (File d : file){
                    selectFiles.add(d+"");
                    log.append(d+"\n");
                }

            }
            if(selectFiles.size()==0) return;
         //   socketThread.sendFile(selectFiles);
            log.append("Upload complete");

        } else if (src == btnRename) {
            client.renameFile();
        } else if (src == btnDelete) {
            client.deleteFile();
        } else if (src == btnLogout) {
            disconect();
        } else {
            throw new RuntimeException("Unknown src=" + src);
        }

    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {

        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String msg;
        if (stackTraceElements.length == 0) {
            msg = "Empty StackTrace";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception: ", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }



    private void connect() {
        //    upperPanel.setVisible(false);
        try {
            Socket socket = new Socket(IP_ADRESS, PORT);
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception: " + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
        upperPanel.setVisible(false);

    }

    private void disconect() {
      socketThread.close();
      upperPanel.setVisible(true);

    }


    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("SocketThread started. Connection\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("SocketThread stopped. Connection lost\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("SocketThread is ready. Connected\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onReceivePacket(SocketThread socketThread, Socket socket, Packet packet) {

    }



    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
                log.append("Exception: "+e.getMessage()+"\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }


}
