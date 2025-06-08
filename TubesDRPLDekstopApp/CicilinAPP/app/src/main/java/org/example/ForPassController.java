package org.example;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
            // kalo semua kosong
            // loginMessageLabel.setText("You Try To Login!");
            validateLogin();
        } else {
            //kalo salah satu kosong
            loginMessageLabel.setText("Please enter both email and password!");
        }
    }

    public void validateLogin() {
    DatabaseConnection connectNow = new DatabaseConnection();
    Connection connectDB = connectNow.getConnection();

    String verifyLogin = "SELECT * FROM \"User\" WHERE email = ? AND password = ?";

    try {
        // Pakai PreparedStatement supaya aman
        PreparedStatement preparedStatement = connectDB.prepareStatement(verifyLogin);
        preparedStatement.setString(1, emailTextField.getText().trim());
        preparedStatement.setString(2, passwordPasswordField.getText().trim());

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            // Artinya ada user dengan email & password yg cocok
            loginMessageLabel.setText("Login successful!");
            // TODO: redirect ke main scene
        } else {
            loginMessageLabel.setText("Invalid Login! Try Again...");
        }

    } catch (Exception e) {
        e.printStackTrace();
        loginMessageLabel.setText("Database error occurred.");
    }
}
}