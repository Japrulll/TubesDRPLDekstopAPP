package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ForPassController {

    @FXML
    private void handleForPass(ActionEvent event) throws Exception {
        Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/forgotPassword.fxml"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("CICILIN");
        stage.show();
    }
}
