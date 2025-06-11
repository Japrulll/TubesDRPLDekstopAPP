package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
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
    private Button logoutButton;

    private String currentUserEmail; // Variabel diubah untuk menampung email

    // Method ini sekarang menerima email
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
    private void handleLogout(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/loginFRPL.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("CICILIN Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}