package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

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
}