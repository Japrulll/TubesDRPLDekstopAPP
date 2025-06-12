package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button backLoginButton;

    @FXML
    private void handleConfirm() {
        String email = emailField.getText();

        if (email != null && email.contains("@")) {
            int atIndex = email.indexOf("@");
            String masked = email.charAt(0) + "****" + email.substring(atIndex);
            messageLabel.setText("We've sent email to " + masked);
        } else {
            messageLabel.setText("Please enter a valid email.");
        }
    }
    
    @FXML
    private void handleRegister(ActionEvent e) throws Exception {
        Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/loginAja.fxml"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("CICILIN");
        stage.show();
    }
}