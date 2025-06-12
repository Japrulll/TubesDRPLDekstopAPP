package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("/loginFRPL.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("CICILIN");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
    
    public void loginScene(Stage loginStage) throws Exception {
        loginStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        loginStage.initStyle(StageStyle.UNDECORATED);
        loginStage.setTitle("CICILIN");
        loginStage.setScene(new Scene(root, 600, 400));
        loginStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
