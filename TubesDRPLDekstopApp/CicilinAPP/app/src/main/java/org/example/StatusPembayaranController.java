package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusPembayaranController {
    private String currentUserEmail;

    @FXML
    private Label totalCicilanLabel;

    @FXML
    private VBox cicilanListVBox;

    // Data class untuk menyimpan informasi cicilan
    public static class CicilanInfo {
        public int nomorCicilan;
        public BigDecimal jumlah;
        public LocalDate tenggat;
        public boolean status;

        public CicilanInfo(int nomorCicilan, BigDecimal jumlah, LocalDate tenggat, boolean status) {
            this.nomorCicilan = nomorCicilan;
            this.jumlah = jumlah;
            this.tenggat = tenggat;
            this.status = status;
        }
    }

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Status Pembayaran - User has been set to: " + email);
        loadPaymentStatus(); // Load data when user is set
    }

    private void loadPaymentStatus() {
        Task<List<CicilanInfo>> task = new Task<>() {
            @Override
            protected List<CicilanInfo> call() throws Exception {
                // Query untuk mendapatkan semua cicilan user yang aktif
                String sql = "SELECT c.nomor_cicilan, c.jumlah, c.tenggat_waktu, c.status_cicilan " +
                             "FROM \"Cicilan\" c " +
                             "JOIN \"Pinjaman\" p ON c.id_pinjaman = p.id_pinjaman " +
                             "JOIN \"User\" u ON p.id_user = u.id_user " +
                             "WHERE u.email = ? AND c.status_cicilan = true " +
                             "ORDER BY c.nomor_cicilan ASC";

                DatabaseConnection db = new DatabaseConnection();
                List<CicilanInfo> cicilanList = new ArrayList<>();

                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, currentUserEmail);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int nomorCicilan = rs.getInt("nomor_cicilan");
                        BigDecimal jumlah = rs.getBigDecimal("jumlah");
                        LocalDate tenggat = rs.getDate("tenggat_waktu").toLocalDate();
                        boolean status = rs.getBoolean("status_cicilan");

                        cicilanList.add(new CicilanInfo(nomorCicilan, jumlah, tenggat, status));
                    }
                }
                return cicilanList;
            }
        };

        task.setOnSucceeded(e -> {
            List<CicilanInfo> cicilanList = task.getValue();
            displayPaymentStatus(cicilanList);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            // Show error message
            totalCicilanLabel.setText("Gagal memuat data");
        });

        new Thread(task).start();
    }

    private void displayPaymentStatus(List<CicilanInfo> cicilanList) {
        // Clear existing content
        cicilanListVBox.getChildren().clear();

        if (cicilanList.isEmpty()) {
            totalCicilanLabel.setText("Rp 0");
            Label noDataLabel = new Label("Tidak ada cicilan aktif");
            noDataLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            cicilanListVBox.getChildren().add(noDataLabel);
            return;
        }

        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (CicilanInfo cicilan : cicilanList) {
            total = total.add(cicilan.jumlah);
        }

        // Format currency
        Locale indonesia = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy", indonesia);

        // Set total label
        totalCicilanLabel.setText(formatRupiah.format(total));

        // Add each cicilan row
        for (CicilanInfo cicilan : cicilanList) {
            HBox cicilanRow = createCicilanRow(cicilan, formatRupiah, dateFormat);
            cicilanListVBox.getChildren().add(cicilanRow);
        }
    }

    private HBox createCicilanRow(CicilanInfo cicilan, NumberFormat formatRupiah, DateTimeFormatter dateFormat) {
        HBox row = new HBox();
        row.setSpacing(10.0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Nomor cicilan
        Label nomorLabel = new Label(cicilan.nomorCicilan + ".");
        nomorLabel.setPrefWidth(30.0);
        nomorLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        // Jumlah
        Label jumlahLabel = new Label(formatRupiah.format(cicilan.jumlah));
        jumlahLabel.setPrefWidth(120.0);
        jumlahLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        // Status
        String statusText = determinePaymentStatus(cicilan.tenggat);
        Label statusLabel = new Label(statusText);
        statusLabel.setPrefWidth(100.0);
        
        // Set color based on status
        if (statusText.equals("Belum bayar")) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #51cf66; -fx-font-size: 12px;");
        }

        // Tanggal tenggat
        Label tenggattLabel = new Label(dateFormat.format(cicilan.tenggat));
        tenggattLabel.setPrefWidth(100.0);
        tenggattLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        row.getChildren().addAll(nomorLabel, jumlahLabel, statusLabel, tenggattLabel);
        return row;
    }

    private String determinePaymentStatus(LocalDate tenggat) {
        LocalDate today = LocalDate.now();
        
        // If due date is today or in the past, it should be paid
        if (tenggat.isBefore(today) || tenggat.isEqual(today)) {
            return "Belum bayar";
        } else {
            return "Belum bayar";
        }
    }

    @FXML
    public void handleLogOut(ActionEvent event) {   
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginAja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN - Logout");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePembayaran(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pembayaran.fxml"));
            Parent root = loader.load();
            
            // Pass current user to pembayaran controller
            PembayaranController pembayaranController = loader.getController();
            pembayaranController.setCurrentUser(this.currentUserEmail);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
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
            
            // Pass current user to dashboard controller
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(this.currentUserEmail);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("CICILIN");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}