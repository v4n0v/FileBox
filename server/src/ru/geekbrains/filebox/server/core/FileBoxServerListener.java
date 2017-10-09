package ru.geekbrains.filebox.server.core;

public interface FileBoxServerListener {
    void onFileBoxServerLog(FileBoxServer server, String msg);
}
