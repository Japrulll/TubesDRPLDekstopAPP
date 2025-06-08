package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public class DashboardController {

    @FXML
    private Label notificationAmountLabel;

    @FXML
    private Label notificationExpiredLabel;

    @FXML
    private Button logoutButton;

    private String currentUserId;

    // Method ini dipanggil setelah login, kirimkan userId/userName dari login controller
    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
        loadNotificationData();
    }

    private void loadNotificationData() {
        // Data dummy, nanti bisa diganti database
        notificationAmountLabel.setText("Rp. 5.041.325");
        notificationExpiredLabel.setText("19 Agustus 2030");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Logika logout, misal kembali ke halaman login
        System.out.println("Logout clicked");
        // Tutup window atau ganti scene sesuai aplikasi Anda
    }
}