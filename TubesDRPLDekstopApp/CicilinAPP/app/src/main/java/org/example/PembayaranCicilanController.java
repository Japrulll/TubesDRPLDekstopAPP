package org.example;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class PembayaranCicilanController {

    @FXML
    private Label cicilanAmountLabel;
    @FXML
    private Label cicilanDueDateLabel;
    @FXML
    private TextField paymentAmountField;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private Label paymentMessageLabel;

    private String currentUserEmail;
    private Integer currentCicilanId;
    private BigDecimal currentCicilanAmount;

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Halaman Bayar Cicilan untuk user: " + this.currentUserEmail);
        loadCicilanData();
    }

    // Kode ini sudah baik, tidak perlu diubah
    private void loadCicilanData() {
        // ... (seluruh isi method loadCicilanData dari file yang Anda berikan sudah benar)
    }

    // --- PERBAIKAN UTAMA ADA DI METHOD INI ---
    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        if (currentCicilanId == null || currentCicilanAmount == null) {
            paymentMessageLabel.setText("Tidak ada cicilan yang dipilih untuk dibayar.");
            return;
        }

        BigDecimal inputAmount;
        try {
            inputAmount = new BigDecimal(paymentAmountField.getText());
        } catch (NumberFormatException e) {
            paymentMessageLabel.setText("Jumlah pembayaran tidak valid.");
            return;
        }

        if (inputAmount.compareTo(currentCicilanAmount) < 0) {
            paymentMessageLabel.setText("Jumlah yang dibayar kurang dari jumlah tagihan.");
            return;
        }

        confirmPaymentButton.setDisable(true);
        paymentMessageLabel.setText("Memproses pembayaran...");

        Task<Boolean> paymentTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                DatabaseConnection connectNow = new DatabaseConnection();
                String updateSql = "UPDATE \"Cicilan\" SET status_cicilan = true WHERE id_cicilan = ?";

                try (Connection conn = connectNow.getConnection();
                     PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, currentCicilanId);
                    int rowsAffected = ps.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        };

        paymentTask.setOnSucceeded(e -> {
            if (paymentTask.getValue()) {
                Platform.runLater(() -> {
                    paymentMessageLabel.setText("Pembayaran berhasil!");
                    // PENTING: Panggil ini untuk memuat ulang data di halaman ini
                    // agar menampilkan cicilan berikutnya atau pesan "Tidak ada tagihan"
                    loadCicilanData();
                    // Kosongkan field input setelah berhasil
                    paymentAmountField.clear();
                });
            } else {
                Platform.runLater(() -> {
                    paymentMessageLabel.setText("Pembayaran gagal. Coba lagi.");
                    confirmPaymentButton.setDisable(false);
                });
            }
        });

        paymentTask.setOnFailed(e -> {
            paymentTask.getException().printStackTrace();
            Platform.runLater(() -> {
                paymentMessageLabel.setText("Terjadi kesalahan database saat pembayaran.");
                confirmPaymentButton.setDisable(false);
            });
        });

        new Thread(paymentTask).start();
    }

    // --- Bagian Navigasi ---
    // Pastikan navigasi di sini juga meneruskan data user
    private void switchSceneAndPassUser(String fxmlFile, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        if (fxmlFile.equals("/dashboard.fxml")) {
            DashboardController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        } else if (fxmlFile.equals("/pembayaran.fxml")) {
            PembayaranController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        }
        
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void handleLogOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginAja.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handlePembayaran(ActionEvent event) {
        try {
            switchSceneAndPassUser("/pembayaran.fxml", event);
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