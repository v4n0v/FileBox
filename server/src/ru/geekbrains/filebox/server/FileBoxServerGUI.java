package ru.geekbrains.filebox.server;

import ru.geekbrains.filebox.server.core.FileBoxServer;
import ru.geekbrains.filebox.server.core.FileBoxServerListener;
import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileBoxServerGUI extends JFrame implements ActionListener, FileBoxServerListener, Thread.UncaughtExceptionHandler{

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;

    private static final String TITLE = "FileBox Server";
    private static final String START_SERVER = "Start Server";
    private static final String STOP_SERVER = "Stop Server";


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileBoxServerGUI();
            }
        });
    }
    private final FileBoxServer fileBoxServer = new FileBoxServer(this, new SQLLoginManager());
    private final JButton btnStart = new JButton(START_SERVER);
    private final JButton btnStop = new JButton(STOP_SERVER);
    private final JTextArea log = new JTextArea();

    private FileBoxServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        setTitle(TITLE);

        btnStart.addActionListener(this);
        btnStop.addActionListener(this);

        JPanel upperPanel = new JPanel(new GridLayout(1, 2));
        upperPanel.add(btnStart);
        upperPanel.add(btnStop);
        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src==btnStart){
          fileBoxServer.startListening(8189);

        } else  if (src==btnStop){
          fileBoxServer.stopListening();
        } else {
            throw new RuntimeException("Unknown src="+src);
        }
    }

    @Override
    public void onFileBoxServerLog(FileBoxServer server, String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg+"\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      //  System.out.println("Cought exception"+e);
        e.printStackTrace();
        StackTraceElement[] stackTraceElements=e.getStackTrace();
        String msg;
        if (stackTraceElements.length==0){
            msg = "Empty StackTrace";
        }else {
            msg=e.getClass().getCanonicalName()+": "+e.getMessage()+"\n"+stackTraceElements[0];
        }
       JOptionPane.showMessageDialog(null, msg, "Exception: ", JOptionPane.ERROR_MESSAGE );
       System.exit(1);
    }
}
