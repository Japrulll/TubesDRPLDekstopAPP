package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("/loginFRPL.fxml"));
        primaryStage.setTitle("CICILIN");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
    public void loginScene(Stage loginStage) throws Exception {
        loginStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("/loginAja.fxml"));
        loginStage.setTitle("CICILIN");
        loginStage.setScene(new Scene(root, 600, 400));
        loginStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
