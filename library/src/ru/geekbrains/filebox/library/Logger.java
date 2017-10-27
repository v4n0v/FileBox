package ru.geekbrains.filebox.library;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Logger {
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private static PrintWriter log;

    public static void writeLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) + msg;
        try {
            FileWriter logFile = new FileWriter("client.log", true);
            log = new PrintWriter((java.io.Writer) logFile);
        } catch (IOException e) {
            log.printf(msg);
            e.printStackTrace();
            return;
        }
        try {
            throw new Exception();
        } catch (Exception ex) {
            log.printf(msg + "\n");
            log.flush();
        }

    }
}
