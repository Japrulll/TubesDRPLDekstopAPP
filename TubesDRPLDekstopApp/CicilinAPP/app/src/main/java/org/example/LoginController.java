package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Label loginMessageLabel;

    @FXML
    public void loginButtonOnAction(ActionEvent e) {
        if (emailTextField.getText().isBlank() || passwordPasswordField.getText().isBlank()) {
            loginMessageLabel.setText("Please enter both email and password!");
            return;
        }

        loginMessageLabel.setText("Logging in, please wait...");

        Task<String> loginTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                DatabaseConnection connectNow = new DatabaseConnection();
                // PERHATIAN: Query ini membandingkan password teks polos (tidak aman)
                String sql = "SELECT id FROM \"User\" WHERE email = ? AND password = ?";

                try (Connection connectDB = connectNow.getConnection();
                     PreparedStatement preparedStatement = connectDB.prepareStatement(sql)) {

                    preparedStatement.setString(1, emailTextField.getText().trim());
                    preparedStatement.setString(2, passwordPasswordField.getText().trim());

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            // Jika query menghasilkan baris, berarti email dan password cocok
                            return resultSet.getString("email"); // Berhasil, kembalikan User ID
                        }
                    }
                }
                // Jika tidak ada hasil, login gagal
                return null;
            }
        };

        loginTask.setOnSucceeded(event -> {
            String userId = loginTask.getValue();
            if (userId != null) {
                navigateToDashboard(userId);
            } else {
                loginMessageLabel.setText("Invalid email or password!");
            }
        });

        loginTask.setOnFailed(event -> {
            loginMessageLabel.setText("Database error. Please check connection.");
            loginTask.getException().printStackTrace();
        });

        new Thread(loginTask).start();
    }

    private void navigateToDashboard(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(userId);

            Stage stage = (Stage) loginMessageLabel.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            loginMessageLabel.setText("Error loading dashboard.");
        }
    }
}