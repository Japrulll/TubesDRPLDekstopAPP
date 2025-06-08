package org.example;

// import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private Button backLoginButton;

    @FXML
    private void handleRegister(ActionEvent e) throws Exception {
        Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/loginFRPL.fxml"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("CICILIN");
        stage.show();
    }

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
                // redirect ke main scene
            } else {
                loginMessageLabel.setText("Invalid Login! Try Again...");
            }

        } catch (Exception e) {
            e.printStackTrace();
            loginMessageLabel.setText("Database error occurred.");
        }
    }
    public void registerUser(String notelp, String email, String fullname, String password) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String insertUser = "INSERT INTO \"User\" (notelp, email, fullname, password) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(insertUser);
            preparedStatement.setLong(1, Long.parseLong(notelp.trim())); // notelp kamu INT8 → pakai Long
            preparedStatement.setString(2, email.trim());
            preparedStatement.setString(3, fullname.trim());
            preparedStatement.setString(4, password.trim());

            preparedStatement.executeUpdate();
            System.out.println("✅ User registered successfully!");

        } catch (Exception e) {
            if (e.getMessage().contains("duplicate key value")) {
                System.err.println("❌ User already exists (duplicate key)!");
            } else {
                e.printStackTrace();
            }
        }
    }
}