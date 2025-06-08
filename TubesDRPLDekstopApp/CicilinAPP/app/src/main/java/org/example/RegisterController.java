package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField fullNameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private Label registerMessageLabel;

    @FXML
    private void handleLogin(ActionEvent e) throws Exception {
        Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/loginAja.fxml"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("CICILIN");
        stage.show();
    }

    @FXML
    private void registerButtonOnAction() {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();
        if (fullNameTextField.getText().isBlank()|| phoneNumberTextField.getText().isBlank() || emailTextField.getText().isBlank() || passwordPasswordField.getText().isBlank()) {
        registerMessageLabel.setText("All fields are required!");
        return;
    }

        String insertUser = "INSERT INTO \"User\" (notelp, email, fullname, password) VALUES (?, ?, ?, ?)";

        try {
            long phoneNumber;
            try {
                phoneNumber = Long.parseLong(phoneNumberTextField.getText().trim());
            } catch (NumberFormatException nfe) {
                registerMessageLabel.setText(" Invalid phone number!");
                return;
            }

            PreparedStatement preparedStatement = connectDB.prepareStatement(insertUser);
            preparedStatement.setLong(1, phoneNumber);
            preparedStatement.setString(2, emailTextField.getText().trim());
            preparedStatement.setString(3, fullNameTextField.getText().trim());
            preparedStatement.setString(4, passwordPasswordField.getText().trim());

            preparedStatement.executeUpdate();
            registerMessageLabel.setText(" User registered successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            registerMessageLabel.setText(" Registration failed: " + e.getMessage());
        }
    }

}
