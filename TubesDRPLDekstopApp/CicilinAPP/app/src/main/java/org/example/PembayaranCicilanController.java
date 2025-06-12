package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    private Integer currentUserId;
    private Integer currentCicilanId;
    private BigDecimal currentCicilanAmount;

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Pembayaran Cicilan for user: " + this.currentUserEmail);
        loadCicilanData();
    }

    private void loadCicilanData() {
        cicilanAmountLabel.setText("Memuat...");
        cicilanDueDateLabel.setText("Mohon tunggu...");
        paymentMessageLabel.setText("");

        Task<Map<String, Object>> dataLoaderTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                DatabaseConnection connectNow = new DatabaseConnection();
                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    conn = connectNow.getConnection();

                    // LANGKAH 1: Cari id_user berdasarkan email user yang login
                    String findIdSql = "SELECT id_user FROM \"User\" WHERE email = ?";
                    ps = conn.prepareStatement(findIdSql);
                    ps.setString(1, currentUserEmail);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        currentUserId = rs.getInt("id_user");
                    }
                    rs.close();
                    ps.close();

                    // LANGKAH 2: Jika user ditemukan, ambil cicilan terdekat yang belum lunas
                    if (currentUserId != null) {
                        String cicilanSql =
                            "SELECT c.id_cicilan, c.jumlah, c.tenggat_waktu " +
                            "FROM \"Cicilan\" c " +
                            "JOIN \"Pinjaman\" p ON c.id_pinjaman = p.id_pinjaman " +
                            "WHERE p.id_user = ? AND c.status_cicilan = false " +
                            "ORDER BY c.tenggat_waktu ASC LIMIT 4";
                        ps = conn.prepareStatement(cicilanSql);
                        ps.setInt(1, currentUserId);
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            currentCicilanId = rs.getInt("id_cicilan");
                            currentCicilanAmount = rs.getBigDecimal("jumlah");
                            LocalDate dueDate = rs.getDate("tenggat_waktu").toLocalDate();

                            Map<String, Object> result = new HashMap<>();
                            result.put("id_cicilan", currentCicilanId);
                            result.put("amount", currentCicilanAmount);
                            result.put("dueDate", dueDate);
                            // rs.close(); // Pindahkan rs.close() ke finally
                            // ps.close(); // Pindahkan ps.close() ke finally
                            return result;
                        }
                    }
                } finally {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                    if (conn != null) conn.close();
                }
                return null;
            }
        };

        dataLoaderTask.setOnSucceeded(event -> {
            Map<String, Object> data = dataLoaderTask.getValue();
            if (data != null) {
                BigDecimal amount = (BigDecimal) data.get("amount");
                LocalDate dueDate = (LocalDate) data.get("dueDate");

                Locale indonesia = new Locale("id", "ID");
                NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indonesia);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM uuuu", indonesia);

                Platform.runLater(() -> {
                    cicilanAmountLabel.setText(rupiahFormat.format(amount));
                    cicilanDueDateLabel.setText(dateFormat.format(dueDate));
                    confirmPaymentButton.setDisable(false);
                });
            } else {
                Platform.runLater(() -> {
                    cicilanAmountLabel.setText("Rp 0");
                    cicilanDueDateLabel.setText("Tidak ada tagihan aktif");
                    paymentMessageLabel.setText("Tidak ada cicilan yang perlu dibayar.");
                    confirmPaymentButton.setDisable(true);
                });
            }
        });

        dataLoaderTask.setOnFailed(event -> {
            dataLoaderTask.getException().printStackTrace();
            Platform.runLater(() -> {
                paymentMessageLabel.setText("Gagal memuat data cicilan.");
                cicilanAmountLabel.setText("Error");
                cicilanDueDateLabel.setText("Error");
                confirmPaymentButton.setDisable(true);
            });
        });

        new Thread(dataLoaderTask).start();
    }

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
                    loadCicilanData();
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

    @FXML
    public void handleLogOut(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/loginAja.fxml"));
            javafx.scene.Parent root = loader.load();
            // Perbaikan: Hapus tanda kurung tutup ')' yang berlebihan
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
            // Penting: Teruskan user email jika PembayaranController membutuhkannya
            PembayaranController pembayaranController = loader.getController();
            pembayaranController.setCurrentUser(this.currentUserEmail);

            // Perbaikan: Hapus tanda kurung tutup ')' yang berlebihan
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 600, 400));
            stage.setTitle("CICILIN - Pembayaran");
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
            // Penting: Teruskan user email ke DashboardController
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(this.currentUserEmail);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}