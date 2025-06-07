package org.example;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import org.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ForPassController {
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private void handleForPass(ActionEvent event) throws Exception {
        Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/forgotPassword.fxml"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("CICILIN");
        stage.show();
    }

    @FXML
    private Label loginMessageLabel;

    public void loginButtonOnAction(ActionEvent e) {
        if (emailTextField.getText().isBlank() == false && passwordPasswordField.getText().isBlank() == false) {
            //kalo semua kosong
            // loginMessageLabel.setText("You Try To Login!");
            validateLogin();
        } else {
            //kalo salah satu kosong
            loginMessageLabel.setText("Please enter both email and password!");
        }
    }

    public void validateLogin(){
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String verifyLogin = "SELECT count(1) FROM UserAcc WHERE Email = '" + emailTextField.getText() + "' AND password = '" + passwordPasswordField.getText() + "'";

        try{
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    loginMessageLabel.setText("Login successful!");
                    // Redirect to the next scene or perform further actions
                } else {
                    loginMessageLabel.setText("Invalid Login! Try Again...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
