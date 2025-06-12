package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
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
    private TableView<CicilanStatus> statusTableView;
    @FXML
    private TableColumn<CicilanStatus, Integer> cicilanColumn;
    @FXML
    private TableColumn<CicilanStatus, BigDecimal> jumlahColumn;
    @FXML
    private TableColumn<CicilanStatus, LocalDate> jatuhTempoColumn;
    @FXML
    private TableColumn<CicilanStatus, String> statusColumn;

    @FXML
    public void initialize() {
        cicilanColumn.setCellValueFactory(new PropertyValueFactory<>("nomorCicilan"));
        jumlahColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        jatuhTempoColumn.setCellValueFactory(new PropertyValueFactory<>("jatuhTempo"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        formatJumlahColumn();
        formatTanggalColumn();
        formatStatusColumn();
    }

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Status Pembayaran for user: " + this.currentUserEmail);
        loadStatusData();
    }

    private void loadStatusData() {
        Task<List<CicilanStatus>> task = new Task<>() {
            @Override
            protected List<CicilanStatus> call() throws Exception {
                List<CicilanStatus> result = new ArrayList<>();
                
                // --- PERBAIKAN UTAMA ADA DI SINI ---
                // Query ini mengambil SEMUA cicilan tanpa memfilter status lunas/belum lunas
                String sql = "SELECT c.nomor_cicilan, c.jumlah, c.tenggat_waktu, c.status_cicilan " +
                             "FROM \"Cicilan\" c " +
                             "JOIN \"Pinjaman\" p ON c.id_pinjaman = p.id_pinjaman " +
                             "JOIN \"User\" u ON p.id_user = u.id_user " +
                             "WHERE u.email = ? " +
                             "ORDER BY c.nomor_cicilan ASC";

                DatabaseConnection db = new DatabaseConnection();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    if (conn == null) throw new IOException("Tidak dapat terhubung ke database.");

                    ps.setString(1, currentUserEmail);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int noCicilan = rs.getInt("nomor_cicilan");
                        BigDecimal jumlah = rs.getBigDecimal("jumlah");
                        LocalDate jatuhTempo = rs.getDate("tenggat_waktu").toLocalDate();
                        boolean isLunas = rs.getBoolean("status_cicilan");
                        result.add(new CicilanStatus(noCicilan, jumlah, jatuhTempo, isLunas));
                    }
                }
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<CicilanStatus> data = FXCollections.observableArrayList(task.getValue());
            statusTableView.setItems(data);
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }
    
    // --- Method Helper untuk Formatting Kolom ---

    private void formatJumlahColumn() {
        Locale indonesia = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
        jumlahColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatRupiah.format(item));
            }
        });
    }

    private void formatTanggalColumn() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM uuuu", new Locale("id", "ID"));
        jatuhTempoColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : dateFormat.format(item));
            }
        });
    }

    private void formatStatusColumn() {
        statusColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(item.equals("Lunas") ? Color.GREEN : Color.RED);
                }
            }
        });
    }

    // --- Kode Navigasi ---

    private void switchScene(String fxmlFile, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    @FXML
    public void handleLogOut(ActionEvent event) {
        try {
            switchScene("/Login.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePembayaran(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pembayaran.fxml"));
            Parent root = loader.load();
            PembayaranController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
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
            DashboardController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}