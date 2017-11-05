package ru.geekbrains.filebox.client.fxcontrollers;



import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import ru.geekbrains.filebox.client.core.FileBoxClientManager;
import ru.geekbrains.filebox.client.core.State;
import ru.geekbrains.filebox.library.AlertWindow;


public class RegistrationController extends BaseController{
    @FXML
    TextField loginRegField ;
    @FXML
    TextField mailRegField ;
    @FXML
    PasswordField pass1RegField ;
    @FXML
    PasswordField pass2RegField ;
    @FXML
    Button exit;
    @FXML
    Button addNew;

    private final static int MIN_PASS_LENGTH = 2;
    private final static int MAX_PASS_LENGTH = 32;

    public void addUser() {
        FileBoxClientManager clientManager = clientController.getClientManager();

        String loginReg = loginRegField.getText();
        String mailReg = mailRegField.getText();
  //      String  mailReg = mailRegField.getText();
        String pass1Reg = pass1RegField.getText();
        String pass2Reg = pass2RegField.getText();
        if (loginReg.isEmpty() && mailReg.isEmpty()
                && pass1Reg.isEmpty() && pass2Reg.isEmpty()) {
            AlertWindow.errorMesage("Fill the all fields");
        } else if (pass1Reg.length() < MIN_PASS_LENGTH || pass1Reg.length() > MAX_PASS_LENGTH) {
            AlertWindow.errorMesage("Password must be from" + MIN_PASS_LENGTH + " to " + MAX_PASS_LENGTH + " words.");
        } else {
            if (pass1Reg.equals(pass2Reg)) {
                clientManager.setRegistrationInfo(loginReg, mailReg, pass1Reg);
                clientManager.state = State.REGISTRATION;
                clientManager.connect();
            } else {
                AlertWindow.errorMesage("Password fields are not equals");
            }
        }


    }
//    public void setRegistrationInfo(String loginReg, String mailReg, String pass1Reg) {
//        this.pass1Reg = pass1Reg;
//        this.mailReg = mailReg;
//        this.loginReg = loginReg;
//    }

//    public void exit(){
//        Stage stage = (Stage) exit.getScene().getWindow();
//        stage.close();
//    }

}
