package org.example.realestatemanager.entity;

import javafx.beans.property.*;

/**
 * Represents a user (owner) in the real estate management system.
 */
public class User {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty email;

    /**
     * Default constructor initializing the user with default values.
     */
    public User() {
        this(0, "", "");
    }

    /**
     * Parameterized constructor initializing the user with specified values.
     *
     * @param id    the ID of the user
     * @param name  the name of the user
     * @param email the email of the user
     */
    public User(int id, String name, String email) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    @Override
    public String toString() {
        return name.get() + " (" + email.get() + ")";
    }
}
