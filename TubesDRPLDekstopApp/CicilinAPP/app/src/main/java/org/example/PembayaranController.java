package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PembayaranController {
    @FXML
    private Button bayarCicilanButton;
    @FXML
    private Button statusPembayaranButton;

    private String currentUserEmail; // Tambahkan ini

    // Metode untuk menerima email dari controller sebelumnya (e.g., DashboardController)
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        // Optionally, add code here to refresh the view with the user's data
        System.out.println("Current user set in StatusPembayaranController: " + this.currentUserEmail);
    }

    @FXML
    public void handleLogOut(ActionEvent event) {   
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginAja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN - Logout");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToPembayaranCicilan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pembayaranCicilan.fxml"));
            Parent root = loader.load();
            PembayaranCicilanController pembayaranCicilanController = loader.getController(); // Dapatkan controller
            pembayaranCicilanController.setCurrentUser(this.currentUserEmail); // Teruskan email

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN - Pembayaran Cicilan");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToStatusPembayaran(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statusPembayaran.fxml"));
            Parent root = loader.load();
            StatusPembayaranController statusPembayaranController = loader.getController(); // Dapatkan controller
            statusPembayaranController.setCurrentUser(this.currentUserEmail); // Teruskan email ke StatusPembayaranController juga jika diperlukan

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN - Status Pembayaran");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDashboard(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController(); // Dapatkan controller
            dashboardController.setCurrentUser(this.currentUserEmail); // Teruskan email

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}