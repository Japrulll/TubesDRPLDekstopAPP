package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PembayaranController {

    private String currentUserEmail;

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Menu Pembayaran untuk user: " + this.currentUserEmail);
    }

    private void switchSceneAndPassUser(String fxmlFile, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Cek halaman tujuan dan panggil setCurrentUser di controller yang sesuai
        if (fxmlFile.equals("/pembayaranCicilan.fxml")) {
            PembayaranCicilanController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        } else if (fxmlFile.equals("/statusPembayaran.fxml")) {
            StatusPembayaranController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        } else if (fxmlFile.equals("/dashboard.fxml")) {
            DashboardController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        }

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToPembayaranCicilan(ActionEvent event) {
        try {
            switchSceneAndPassUser("/pembayaranCicilan.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToStatusPembayaran(ActionEvent event) {
        try {
            switchSceneAndPassUser("/statusPembayaran.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDashboard(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}