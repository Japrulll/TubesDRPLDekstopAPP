package org.example;
import java.sql.Connection;
import java.sql.DriverManager;


public class DatabaseConnection {
    public Connection databaselink;
    
    public Connection getConnection(){
        String databaseName = "poc_Base";
        String databaseUser = "root";
        String databasePassword = "Stak1112";
        String url = "jdbc:mysql://localhost:3306/" + databaseName;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaselink = DriverManager.getConnection(url, databaseUser, databasePassword);
            System.out.println("database terhubung boy!");
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gagal terhubung ke database. Error: " + e.getMessage());
        }
        return databaselink;
    }
    
}
