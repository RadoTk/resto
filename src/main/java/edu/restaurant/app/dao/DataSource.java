package edu.restaurant.app.dao;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static final int DEFAULT_PORT = 5432;
    private final String host;
    private final String user;
    private final String password;
    private final String database;
    private final String jdbcUrl;

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Charger explicitement le driver PostgreSQL
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("⚠️ Driver PostgreSQL non trouvé !", e);
        }
    }

    public DataSource() {
        // Charger les variables depuis .env
        Dotenv dotenv = Dotenv.load();
        this.host = dotenv.get("DATABASE_HOST");
        this.user = dotenv.get("DATABASE_USER");
        this.password = dotenv.get("DATABASE_PASSWORD");
        this.database = dotenv.get("DATABASE_NAME");

        System.out.println("🔍 DATABASE_HOST: " + host);
        System.out.println("🔍 DATABASE_USER: " + user);
        System.out.println("🔍 DATABASE_PASSWORD: " + password);
        System.out.println("🔍 DATABASE_NAME: " + database);

        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + DEFAULT_PORT + "/" + database;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur de connexion à PostgreSQL", e);
        }
    }
}
