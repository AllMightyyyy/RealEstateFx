package org.example.realestatemanager.utils;

import org.example.realestatemanager.Main;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Utility class for initializing the database.
 */
public class DatabaseInitializer {
    private static String URL;
    private static String DATABASE_NAME;
    private static String USER;
    private static String PASSWORD;
    private static String FULL_DB_URL;

    static {
        try (InputStream input = Objects.requireNonNull(Main.class.getResource("application.properties")).openStream()) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find application.properties");
            }
            // Load the properties file
            prop.load(input);
            URL = prop.getProperty("db.url");
            DATABASE_NAME = prop.getProperty("db.name");
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
            FULL_DB_URL = URL + DATABASE_NAME + "?useSSL=false&serverTimezone=UTC";
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initializes the database by creating it if it does not exist, creating tables if they do not exist,
     * and seeding initial data.
     */
    public static void initializeDatabase() {
        try {
            createDatabaseIfNotExists();
            createTablesIfNotExists();
            seedInitialData();
            System.out.println("Database initialization complete.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    /**
     * Creates the database if it does not exist.
     *
     * @throws SQLException if a database access error occurs
     */
    private static void createDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
            System.out.println("Database checked/created: " + DATABASE_NAME);
        }
    }

    /**
     * Creates the tables if they do not exist.
     *
     * @throws SQLException if a database access error occurs
     */
    private static void createTablesIfNotExists() throws SQLException {
        String createPropertiesTableSQL = "CREATE TABLE IF NOT EXISTS properties (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "owner VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "location VARCHAR(100), " +
                "size DOUBLE, " +
                "price DOUBLE" +
                ");";

        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createPropertiesTableSQL);
            System.out.println("Table checked/created: properties");
        }
    }

    /**
     * Seeds initial data into the database if the tables are empty.
     *
     * @throws SQLException if a database access error occurs
     */
    private static void seedInitialData() throws SQLException {
        String checkDataSQL = "SELECT COUNT(*) AS count FROM properties";
        String insertSampleDataSQL = "INSERT INTO properties (owner, description, location, size, price) VALUES " +
                "('John Doe', 'A lovely three-bedroom apartment', 'New York', 1200, 750000), " +
                "('Jane Smith', 'Spacious two-bedroom condo', 'Los Angeles', 900, 620000), " +
                "('Paul Brown', 'Beautiful villa with garden', 'Miami', 2500, 1250000);";

        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(checkDataSQL);
            rs.next();
            int rowCount = rs.getInt("count");

            if (rowCount == 0) {
                stmt.executeUpdate(insertSampleDataSQL);
                System.out.println("Initial data seeded into properties table.");
            } else {
                System.out.println("Properties table already contains data; skipping seeding.");
            }
        }
    }
}