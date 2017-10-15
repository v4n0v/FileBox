package ru.geekbrains.filebox.client.fxcontrollers;

import javafx.event.ActionEvent;

public class ClientController {
    public String str;

    public void doSomething(ActionEvent actionEvent) {
        System.out.println(str);
    }


    public void renameFile(){
        System.out.println("rename");
    }
    public void  deleteFile(){
        System.out.println("delete");
    }
    public void  downloadFile(){
        System.out.println("download");
    }
    public void  uploadFile(){
        System.out.println("upload");
    }

    public void  openOptions(){
        System.out.println("options");
    }
    public void  logOut(){
        System.out.println("options");
    }
}
