package org.example.realestatemanager.utils;

import org.example.realestatemanager.Main;
import org.example.realestatemanager.entity.Property;
import org.example.realestatemanager.entity.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Utility class for database operations related to properties and users.
 */
public class DatabaseUtil {
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
            prop.load(input);
            URL = prop.getProperty("db.url");
            DATABASE_NAME = prop.getProperty("db.name");
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
            FULL_DB_URL = URL + DATABASE_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Fetches all properties from the database, including owner details.
     *
     * @return a list of all properties
     * @throws SQLException if a database access error occurs
     */
    public List<Property> getAllProperties() throws SQLException {
        List<Property> list = new ArrayList<>();
        String query = "SELECT p.id, p.owner_id, u.name AS owner_name, u.email AS owner_email, p.description, p.location, p.size, p.price " +
                "FROM properties p JOIN users u ON p.owner_id = u.id";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Property property = new Property(
                        rs.getInt("id"),
                        rs.getInt("owner_id"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("size"),
                        rs.getDouble("price")
                );

                User owner = new User(
                        rs.getInt("owner_id"),
                        rs.getString("owner_name"),
                        rs.getString("owner_email")
                );

                property.setOwner(owner);
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
        String query = "INSERT INTO properties (owner_id, description, location, size, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, property.getOwnerId());
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
        String query = "UPDATE properties SET owner_id=?, description=?, location=?, size=?, price=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, property.getOwnerId());
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
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // ------------------- User Management Methods -------------------

    /**
     * Fetches all users from the database.
     *
     * @return a list of all users
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
                list.add(user);
            }
        }
        return list;
    }

    /**
     * Adds a new user to the database.
     *
     * @param user the user to add
     * @throws SQLException if a database access error occurs
     */
    public void addUser(User user) throws SQLException {
        String query = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user the user to update
     * @throws SQLException if a database access error occurs
     */
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE users SET name=?, email=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a user from the database.
     *
     * @param userId the ID of the user to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM users WHERE id=?";
        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
}
