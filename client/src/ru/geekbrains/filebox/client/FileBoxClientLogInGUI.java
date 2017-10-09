package ru.geekbrains.filebox.client;

import javax.swing.*;
import java.awt.*;

public class FileBoxClientLogInGUI extends JFrame {
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int WIDTH=400;
    private static final int HEIGHT=115;
    private static final int POS_X= (screenSize.width - WIDTH) / 2;
    private static final int POS_Y= (screenSize.height-HEIGHT)/2;
    private static final String TITLE = "FileBox LogIn";
    private static final String USERNAME = "username";
    private static final String PASS = "password";

    private static final String LOGIN = "Login";
    private static final String REGISTER = "Register";

    private final JPanel mainPanel = new JPanel(new GridLayout(2,3));
    private final JLabel usernameLabel = new JLabel(USERNAME);
    private final JLabel passLabel = new JLabel(PASS);
    private final JTextField fieldLogin = new JTextField("root");
    private final JPasswordField fieldPassword = new JPasswordField("1234567");
    private final JButton btnLogin= new JButton(LOGIN);
    private final JButton btnRegister= new JButton(REGISTER);

    private final JPanel btnPanel = new JPanel(new BorderLayout());
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileBoxClientLogInGUI();
            }
        });
    }
    private FileBoxClientLogInGUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setSize( WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(TITLE);



        mainPanel.add(usernameLabel);
        mainPanel.add(fieldLogin);
        mainPanel.add(btnLogin);
        mainPanel.add(passLabel);
        mainPanel.add(fieldPassword);
        mainPanel.add(btnRegister);

//        btnPanel.add(btnRegister);
//          btnPanel.add(btnLogin);

        add(mainPanel, BorderLayout.NORTH);
//
//        add(btnRegister, BorderLayout.WEST);
//        btnRegister.setPreferredSize(new Dimension(50, 20));
//        add(btnLogin, BorderLayout.EAST);
        setVisible(true);
    }
}
