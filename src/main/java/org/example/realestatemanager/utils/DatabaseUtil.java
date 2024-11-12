package org.example.realestatemanager.utils;

import org.example.realestatemanager.Main;
import org.example.realestatemanager.entity.Property;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Utility class for database operations related to properties.
 */
public class DatabaseUtil {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try (InputStream input = Objects.requireNonNull(Main.class.getResource("application.properties")).openStream()) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find application.properties");
            }
            // Load the properties file
            prop.load(input);
            URL = prop.getProperty("db.url");
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Fetches all properties from the database.
     *
     * @return a list of all properties
     * @throws SQLException if a database access error occurs
     */
    public List<Property> getAllProperties() throws SQLException {
        List<Property> list = new ArrayList<>();
        String query = "SELECT * FROM properties";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Property property = new Property(
                        rs.getInt("id"),
                        rs.getString("owner"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("size"),
                        rs.getDouble("price")
                );
                list.add(property);
            }
        }
        return list;
    }

    /**
     * Adds a new property to the database.
     *
     * @param property the property to add
     * @throws SQLException if a database access error occurs
     */
    public void addProperty(Property property) throws SQLException {
        String query = "INSERT INTO properties (owner, description, location, size, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, property.getOwner());
            pstmt.setString(2, property.getDescription());
            pstmt.setString(3, property.getLocation());
            pstmt.setDouble(4, property.getSize());
            pstmt.setDouble(5, property.getPrice());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing property in the database.
     *
     * @param property the property to update
     * @throws SQLException if a database access error occurs
     */
    public void updateProperty(Property property) throws SQLException {
        String query = "UPDATE properties SET owner=?, description=?, location=?, size=?, price=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, property.getOwner());
            pstmt.setString(2, property.getDescription());
            pstmt.setString(3, property.getLocation());
            pstmt.setDouble(4, property.getSize());
            pstmt.setDouble(5, property.getPrice());
            pstmt.setInt(6, property.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a property from the database.
     *
     * @param id the ID of the property to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteProperty(int id) throws SQLException {
        String query = "DELETE FROM properties WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}