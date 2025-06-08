package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField fullnameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private Label registerMessageLabel;

    @FXML
    private void handleRegisterButtonAction() {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String insertUser = "INSERT INTO \"User\" (notelp, email, fullname, password) VALUES (?, ?, ?, ?)";

        try {
            long phoneNumber;
            try {
                phoneNumber = Long.parseLong(phoneNumberTextField.getText().trim());
            } catch (NumberFormatException nfe) {
                registerMessageLabel.setText("❌ Invalid phone number!");
                return;
            }

            PreparedStatement preparedStatement = connectDB.prepareStatement(insertUser);
            preparedStatement.setLong(1, phoneNumber);
            preparedStatement.setString(2, emailTextField.getText().trim());
            preparedStatement.setString(3, fullnameTextField.getText().trim());
            preparedStatement.setString(4, passwordPasswordField.getText().trim());

            preparedStatement.executeUpdate();
            registerMessageLabel.setText("✅ User registered successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            registerMessageLabel.setText("❌ Registration failed: " + e.getMessage());
        }
    }

}
