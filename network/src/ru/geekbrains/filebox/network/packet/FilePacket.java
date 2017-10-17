package ru.geekbrains.filebox.network.packet;

public class FilePacket extends AbstractPacket{
 //   byte[] files= Files.readAllBytes(Paths.get(path));
 //ArrayList<String> fileNames;
//    public FilePacket(byte[] files) {
    public FilePacket(FileContainer files) {
        super(PackageType.FILE, files);
    //    this.fileNames=fileNames;
    }

}
