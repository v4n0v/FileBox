package ru.geekbrains.filebox.client.fxcontrollers;



import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class RegistrationController {
    @FXML
    TextField loginField ;
    @FXML
    TextField mailField ;
    @FXML
    PasswordField pass1Field ;
    @FXML
    PasswordField pass2Field ;
    @FXML
    Button exit;
    @FXML
    Button addNew;
    Alert alert;

    public void addUser(){
        String  login = loginField.getText();
        String  mail = loginField.getText();
        String  pass1 = pass1Field.getText();
        String  pass2 = pass2Field.getText();
        if (login.isEmpty()&&mail.isEmpty()
                &&pass1.isEmpty()&&pass2.isEmpty()){
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Fill the all fields");
            alert.showAndWait();
        }else {

            if (pass1.equals(pass2)) {

            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Password fields are not equals");
                alert.showAndWait();
            }
        }
    }

    public void exit(){
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

}
