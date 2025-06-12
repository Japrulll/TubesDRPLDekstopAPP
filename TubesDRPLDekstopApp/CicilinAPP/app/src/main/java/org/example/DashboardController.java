package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class DashboardController {

    private String currentUserEmail;

    @FXML
    private VBox notificationBox; // VBox untuk seluruh notifikasi

    @FXML
    private Label notificationAmountLabel; // Label untuk jumlah Rp

    @FXML
    private Label notificationDueDateLabel; // Label untuk tanggal

    @FXML
    private Button pembayaranButton;

    @FXML
    private Button logoutButton;

    // Method ini akan dipanggil dari scene sebelumnya (misal: Login)
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Dashboard for user: " + this.currentUserEmail);
        loadUpcomingDueDate(); // Memuat data saat user di-set
    }

    private void loadUpcomingDueDate() {
        // Sembunyikan notifikasi pada awalnya
        notificationBox.setVisible(false);
        notificationBox.setManaged(false);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                // Query untuk mendapatkan cicilan terdekat yang BELUM LUNAS
                String sql = "SELECT c.jumlah, c.tenggat_waktu " +
                             "FROM \"Cicilan\" c " +
                             "JOIN \"Pinjaman\" p ON c.id_pinjaman = p.id_pinjaman " +
                             "JOIN \"User\" u ON p.id_user = u.id_user " +
                             "WHERE u.email = ? AND c.status_cicilan = false " +
                             "ORDER BY c.tenggat_waktu ASC LIMIT 1";

                DatabaseConnection db = new DatabaseConnection();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, currentUserEmail);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        BigDecimal jumlah = rs.getBigDecimal("jumlah");
                        LocalDate jatuhTempo = rs.getDate("tenggat_waktu").toLocalDate();

                        Map<String, Object> result = new HashMap<>();
                        result.put("amount", jumlah);
                        result.put("dueDate", jatuhTempo);
                        return result;
                    }
                }
                return null; // Kembalikan null jika tidak ada cicilan aktif
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> result = task.getValue();
            if (result != null) {
                BigDecimal amount = (BigDecimal) result.get("amount");
                LocalDate dueDate = (LocalDate) result.get("dueDate");

                // Format data untuk ditampilkan
                Locale indonesia = new Locale("id", "ID");
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", indonesia);

                notificationAmountLabel.setText(formatRupiah.format(amount));
                notificationDueDateLabel.setText(dateFormat.format(dueDate));

                // Tampilkan kembali kotak notifikasi
                notificationBox.setVisible(true);
                notificationBox.setManaged(true);
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            notificationAmountLabel.setText("Gagal memuat data");
            notificationDueDateLabel.setText("");
            notificationBox.setVisible(true);
            notificationBox.setManaged(true);
        });

        new Thread(task).start();
    }

    @FXML
    void handlePembayaran(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pembayaran.fxml"));
        Parent root = loader.load();

        // Teruskan email user ke controller pembayaran
        PembayaranController pembayaranController = loader.getController();
        pembayaranController.setCurrentUser(this.currentUserEmail);

        Stage stage = (Stage) pembayaranButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void handleLogOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginAja.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}