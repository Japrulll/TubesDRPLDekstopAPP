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

public class PembayaranCicilanController {
    
    // FXML Elements
    @FXML private Label totalCicilanLabel;
    @FXML private VBox cicilanListVBox;
    @FXML private VBox cicilanListView;
    @FXML private VBox paymentFormView;
    @FXML private VBox successView;
    @FXML private Label selectedAmountLabel;
    @FXML private Label successAmountLabel;
    @FXML private Button confirmPaymentButton;
    @FXML private Button backToMainButton;

    // Data
    private String currentUserEmail;
    private Integer selectedCicilanId;
    private BigDecimal selectedAmount;
    
    // Data class untuk menyimpan informasi cicilan
    public static class CicilanInfo {
        public int idCicilan;
        public int nomorCicilan;
        public BigDecimal jumlah;
        public LocalDate tenggat;
        public boolean status;

        public CicilanInfo(int idCicilan, int nomorCicilan, BigDecimal jumlah, LocalDate tenggat, boolean status) {
            this.idCicilan = idCicilan;
            this.nomorCicilan = nomorCicilan;
            this.jumlah = jumlah;
            this.tenggat = tenggat;
            this.status = status;
        }
    }

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Pembayaran Cicilan for user: " + this.currentUserEmail);
        loadCicilanData();
    }

    private void loadCicilanData() {
        Task<List<CicilanInfo>> task = new Task<>() {
            @Override
            protected List<CicilanInfo> call() throws Exception {
                // Query untuk mendapatkan semua cicilan user yang belum dibayar
                String sql = "SELECT c.id_cicilan, c.nomor_cicilan, c.jumlah, c.tenggat_waktu, c.status_cicilan " +
                             "FROM \"Cicilan\" c " +
                             "JOIN \"Pinjaman\" p ON c.id_pinjaman = p.id_pinjaman " +
                             "JOIN \"User\" u ON p.id_user = u.id_user " +
                             "WHERE u.email = ? AND c.status_cicilan = true " +
                             "ORDER BY c.tenggat_waktu ASC";

                DatabaseConnection db = new DatabaseConnection();
                List<CicilanInfo> cicilanList = new ArrayList<>();

                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, currentUserEmail);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int idCicilan = rs.getInt("id_cicilan");
                        int nomorCicilan = rs.getInt("nomor_cicilan");
                        BigDecimal jumlah = rs.getBigDecimal("jumlah");
                        LocalDate tenggat = rs.getDate("tenggat_waktu").toLocalDate();
                        boolean status = rs.getBoolean("status_cicilan");

                        cicilanList.add(new CicilanInfo(idCicilan, nomorCicilan, jumlah, tenggat, status));
                    }
                }
                return cicilanList;
            }
        };

        task.setOnSucceeded(e -> {
            List<CicilanInfo> cicilanList = task.getValue();
            displayCicilanList(cicilanList);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            totalCicilanLabel.setText("Gagal memuat data");
        });

        new Thread(task).start();
    }

    private void displayCicilanList(List<CicilanInfo> cicilanList) {
        // Clear existing content
        cicilanListVBox.getChildren().clear();

        if (cicilanList.isEmpty()) {
            totalCicilanLabel.setText("Rp 0");
            Label noDataLabel = new Label("Tidak ada cicilan yang perlu dibayar");
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

        // Bayar Button
        Button bayarButton = new Button("Bayar");
        bayarButton.setPrefWidth(80.0);
        bayarButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15; -fx-font-size: 11px;");
        bayarButton.setOnAction(e -> showPaymentForm(cicilan));

        // Tanggal tenggat
        Label tenggattLabel = new Label(dateFormat.format(cicilan.tenggat));
        tenggattLabel.setPrefWidth(100.0);
        tenggattLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        row.getChildren().addAll(nomorLabel, jumlahLabel, bayarButton, tenggattLabel);
        return row;
    }

    private void showPaymentForm(CicilanInfo cicilan) {
        selectedCicilanId = cicilan.idCicilan;
        selectedAmount = cicilan.jumlah;

        // Format currency
        Locale indonesia = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
        selectedAmountLabel.setText(formatRupiah.format(selectedAmount));

        // Hide list view, show payment form
        cicilanListView.setVisible(false);
        cicilanListView.setManaged(false);
        paymentFormView.setVisible(true);
        paymentFormView.setManaged(true);
    }

    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        if (selectedCicilanId == null || selectedAmount == null) {
            return;
        }

        confirmPaymentButton.setDisable(true);

        Task<Boolean> paymentTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                DatabaseConnection connectNow = new DatabaseConnection();
                String updateSql = "UPDATE \"Cicilan\" SET status_cicilan = false WHERE id_cicilan = ?";

                try (Connection conn = connectNow.getConnection();
                     PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, selectedCicilanId);
                    int rowsAffected = ps.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        };

        paymentTask.setOnSucceeded(e -> {
            if (paymentTask.getValue()) {
                Platform.runLater(() -> {
                    showSuccessView();
                });
            } else {
                Platform.runLater(() -> {
                    confirmPaymentButton.setDisable(false);
                    // Show error message
                });
            }
        });

        paymentTask.setOnFailed(e -> {
            paymentTask.getException().printStackTrace();
            Platform.runLater(() -> {
                confirmPaymentButton.setDisable(false);
                // Show error message
            });
        });

        new Thread(paymentTask).start();
    }

    private void showSuccessView() {
        // Format currency for success message
        Locale indonesia = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
        successAmountLabel.setText(formatRupiah.format(selectedAmount));

        // Hide payment form, show success view
        paymentFormView.setVisible(false);
        paymentFormView.setManaged(false);
        successView.setVisible(true);
        successView.setManaged(true);
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(this.currentUserEmail);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("CICILIN - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
            
            PembayaranController pembayaranController = loader.getController();
            pembayaranController.setCurrentUser(this.currentUserEmail);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
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
            
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(this.currentUserEmail);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("CICILIN");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}