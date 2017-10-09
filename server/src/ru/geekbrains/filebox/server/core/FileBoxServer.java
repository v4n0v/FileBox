package ru.geekbrains.filebox.server.core;

import ru.geekbrains.filebox.server.core.authorization.SQLLoginManager;

public class FileBoxServer {
    private final FileBoxServerListener eventListener;
    private final SQLLoginManager loginManager;

    enum ServerState {WORKING, STOPPED}
    private ServerState state = ServerState.STOPPED;

    public FileBoxServer(FileBoxServerListener eventListener,SQLLoginManager loginManager) {
        this.eventListener = eventListener;
        this.loginManager=loginManager;
    }

    public void startListening(int port) {
        if (state != ServerState.WORKING) {
            loginManager.init();
            putLog("Server is working");
            putLog(loginManager.getMail("admin"));
            state = ServerState.WORKING;
        }
    }

    public void stopListening() {

        if (state != ServerState.STOPPED) {
            loginManager.dispose();
            putLog("Server stopped");
            state = ServerState.STOPPED;
        }
    }

    public void putLog(String msg) {

        eventListener.onFileBoxServerLog(this, msg);
    }
}
