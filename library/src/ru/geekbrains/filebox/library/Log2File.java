package ru.geekbrains.filebox.library;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log2File {
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private static PrintWriter log;
    private static final Logger loggerClient = Logger.getLogger("ClientLogger");
    private static final Logger loggerServer = Logger.getLogger("ServerLogger");
    private static final String CLIENT_LOG_FILE = "client.log";
    private static final String SERVER_LOG_FILE = "server.log";

    public static void writeLog(String msg) {
        toLog(msg, CLIENT_LOG_FILE, loggerClient, Level.INFO);
    }
    public static void writeLog(Level level, String msg) {
        toLog(msg, CLIENT_LOG_FILE, loggerClient, level);
    }


    private static void toLog(String msg, String fileName, Logger logger, Level level){
        msg = dateFormat.format(System.currentTimeMillis()) + msg;
        try {
            FileWriter logFile = new FileWriter(fileName, true);
            logger.log(level, msg);
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


    public static void writeServerLog(String msg) {
        toLog(msg, SERVER_LOG_FILE, loggerServer, Level.INFO);

    }
    public static void writeServerLog(Level level, String msg) {
        toLog(msg, SERVER_LOG_FILE, loggerServer, level);
    }

}
