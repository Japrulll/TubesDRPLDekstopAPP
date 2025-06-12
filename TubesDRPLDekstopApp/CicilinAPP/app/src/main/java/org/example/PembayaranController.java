package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PembayaranController {
    @FXML
    private Button bayarCicilanButton;
    @FXML
    private Button statusPembayaranButton;
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
    private void goToPembayaranCicilan(ActionEvent event) {
        try {
            FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/pembayaranCicilan.fxml"));
            Parent root = loader.load();
            Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("CICILIN - Pembayaran Cicilan");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToStatusPembayaran(ActionEvent event) {
        try {
            FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/statusPembayaran.fxml"));
            Parent root = loader.load();
            Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("CICILIN - Pembayaran Cicilan");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}