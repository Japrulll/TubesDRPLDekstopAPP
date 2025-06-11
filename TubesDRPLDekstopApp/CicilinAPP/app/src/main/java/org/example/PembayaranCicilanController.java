package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class PembayaranCicilanController {
    @FXML
    public void handleLogOut(ActionEvent event) {   
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/loginAja.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("CICILIN - Logout");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handlePembayaran(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/pembayaran.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("CICILIN - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}