package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    private String currentUserEmail;

    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationAmountLabel;
    @FXML
    private Label notificationDueDateLabel;

    // Method ini menjadi pintu masuk untuk memuat data di halaman ini
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        System.out.println("Dashboard for user: " + this.currentUserEmail);
        loadUpcomingDueDate(); // Panggil method untuk memuat data notifikasi
    }

    // Method untuk memuat data notifikasi dari database
    private void loadUpcomingDueDate() {
        notificationBox.setVisible(false);
        notificationBox.setManaged(false);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
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
                        Map<String, Object> result = new HashMap<>();
                        result.put("amount", rs.getBigDecimal("jumlah"));
                        result.put("dueDate", rs.getDate("tenggat_waktu").toLocalDate());
                        return result;
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> result = task.getValue();
            if (result != null) {
                BigDecimal amount = (BigDecimal) result.get("amount");
                LocalDate dueDate = (LocalDate) result.get("dueDate");

                Locale indonesia = new Locale("id", "ID");
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(indonesia);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM uuuu", indonesia);

                notificationAmountLabel.setText(formatRupiah.format(amount));
                notificationDueDateLabel.setText(dateFormat.format(dueDate));

                notificationBox.setVisible(true);
                notificationBox.setManaged(true);
            }
        });

        task.setOnFailed(e -> {
            notificationAmountLabel.setText("Gagal memuat data");
            notificationDueDateLabel.setText("");
            notificationBox.setVisible(true);
            notificationBox.setManaged(true);
        });

        new Thread(task).start();
    }

    // --- Navigasi ---
    private void switchScene(String fxmlFile, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        if (fxmlFile.equals("/pembayaran.fxml")) {
            PembayaranController controller = loader.getController();
            controller.setCurrentUser(this.currentUserEmail);
        }
        
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void handlePembayaran(ActionEvent event) {
        try {
            switchScene("/pembayaran.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogOut(ActionEvent event) {
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginAja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}