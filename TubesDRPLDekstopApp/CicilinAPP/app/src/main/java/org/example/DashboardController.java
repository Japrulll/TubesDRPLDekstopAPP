package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardController {
    @FXML
    private Label notificationAmountLabel;
    @FXML
    private Label notificationExpiredLabel;
    @FXML
    public Button logoutButton;
    @FXML
    public Button pembayaranButton;
    @FXML
    private String currentUserEmail;

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Dashboard for user: " + this.currentUserEmail);
        loadNotificationData();
    }

    private void loadNotificationData() {
        notificationAmountLabel.setText("Loading...");
        notificationExpiredLabel.setText("Please wait...");

        Task<Map<String, Object>> dataLoaderTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                DatabaseConnection connectNow = new DatabaseConnection();
                Integer userId = null;

                // LANGKAH 1: Cari ID pengguna (id_user) berdasarkan email
                String findIdSql = "SELECT id_user FROM \"User\" WHERE email = ?";
                try (Connection conn = connectNow.getConnection();
                     PreparedStatement ps = conn.prepareStatement(findIdSql)) {
                    
                    ps.setString(1, currentUserEmail); // Gunakan email yang diterima
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id_user"); // Dapatkan ID numeriknya
                        }
                    }
                }
                
                // Jika ID pengguna ditemukan, lanjutkan ke langkah 2
                if (userId != null) {
                    // LANGKAH 2: Cari data tagihan berdasarkan ID pengguna (userId)
                    String paymentSql = "SELECT jumlah_tagihan, tenggat_waktu FROM \"Payment\" WHERE id_user = ? AND status_pembayaran = false ORDER BY tenggat_waktu ASC LIMIT 1";
                    try (Connection conn = connectNow.getConnection();
                         PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                        
                        ps.setInt(1, userId); // Gunakan ID numerik yang sudah didapat
                        
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                BigDecimal amount = rs.getBigDecimal("jumlah_tagihan");
                                LocalDate dueDate = rs.getDate("tenggat_waktu").toLocalDate();

                                Map<String, Object> result = new HashMap<>();
                                result.put("amount", amount);
                                result.put("dueDate", dueDate);
                                return result; // Kembalikan data tagihan
                            }
                        }
                    }
                }
                
                // Kembalikan null jika user id tidak ditemukan atau tidak ada tagihan
                return null;
            }
        };

        // Bagian setOnSucceeded dan setOnFailed tidak perlu diubah, tetap sama
        dataLoaderTask.setOnSucceeded(event -> {
            Map<String, Object> data = dataLoaderTask.getValue();
            if (data != null) {
                BigDecimal amount = (BigDecimal) data.get("amount");
                LocalDate dueDate = (LocalDate) data.get("dueDate");

                Locale indonesia = new Locale("id", "ID");
                NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indonesia);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM uuuu", indonesia);

                Platform.runLater(() -> {
                    notificationAmountLabel.setText(rupiahFormat.format(amount));
                    notificationExpiredLabel.setText(dateFormat.format(dueDate));
                });
            } else {
                Platform.runLater(() -> {
                    notificationAmountLabel.setText("Rp 0");
                    notificationExpiredLabel.setText("Tidak ada tagihan aktif");
                });
            }
        });

        dataLoaderTask.setOnFailed(event -> {
            dataLoaderTask.getException().printStackTrace();
            Platform.runLater(() -> {
                notificationAmountLabel.setText("Error");
                notificationExpiredLabel.setText("Gagal memuat data");
            });
        });

        new Thread(dataLoaderTask).start();
    }
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