package ru.geekbrains.filebox.client;

import ru.geekbrains.filebox.client.core.FileBoxClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileBoxClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    //    private static final int POS_X= (screenSize.width - WIDTH) / 2;
//    private static final int POS_Y= (screenSize.height-HEIGHT)/2;
    private static final String TITLE = "FileBox Client";

    private static final String LOGIN = "Login";
    private static final String REGISTER = "Register";
    private final String IP_ADRESS = "localhost";
    private final String PORT = "8199";

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

    FileBoxClient client= new FileBoxClient();
    private final JPanel midPanel = new JPanel(new BorderLayout());
    private final JList<String> fileList = new JList<>();
    private final JPanel midButtonsPanel = new JPanel(new GridLayout(4, 1));
    private final JButton btnDownload = new JButton(DOWNLOAD);
    private final JButton btnUpload = new JButton(UPLOAD);
    private final JButton btnRename = new JButton(RENAME);
    private final JButton btnDelete = new JButton(DELETE);

    private FileBoxClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(TITLE);

        btnLogin.addActionListener(this);
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

        add(fileList, BorderLayout.CENTER);
        add(midButtonsPanel, BorderLayout.EAST);

        log.setEditable(false);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnLogin) {
            client.login();
        } else if (src == btnDownload) {
            client. downloadFile();
        } else if (src == btnUpload) {
            client.uploadFile();
        } else if (src == btnRename) {
            client.renameFile();
        } else if (src == btnDelete) {
            client.deleteFile();
        } else {
            throw new RuntimeException("Unknown src=" + src);
        }
//        switch (src) {
//            case  btnLogin:
//              System.out.println("");
//                break;
//            case btnDownload:
//        }
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //  System.out.println("Cought exception"+e);
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
}
