package ru.geekbrains.filebox.network.packet;

public enum PackageType {
    FILE, FILE_LIST, MESSAGE, ERROR,
    LOGIN, AUTH_ACCEPT, REGISTRATION,
    REG_ACCEPT, RENAME, DELETE,
    FILE_REQUEST, ENTER_DIR, FILE_WAITING,
    NEW_FOLDER
}
