package org.example;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.SQLException;

public class DatabaseConnection {
    public Connection databaselink;

    public Connection getConnection() {
        try {
            // Load env variables
            Dotenv dotenv = Dotenv.load();

            String host = dotenv.get("host");
            String port = dotenv.get("port");
            String dbname = dotenv.get("dbname");
            String user = dotenv.get("user");
            String password = dotenv.get("password");

            // Build JDBC URL
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to database
            databaselink = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Connected to PostgreSQL database!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to connect to database. Error: " + e.getMessage());
        }

        return databaselink;
    }
}
