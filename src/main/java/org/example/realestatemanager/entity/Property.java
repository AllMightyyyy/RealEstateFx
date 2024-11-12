package org.example.realestatemanager.entity;

import javafx.beans.property.*;

/**
 * Represents a property in the real estate management system.
 */
public class Property {
    private final IntegerProperty id;
    private final IntegerProperty ownerId; // Foreign key to User
    private final StringProperty description;
    private final StringProperty location;
    private final DoubleProperty size;
    private final DoubleProperty price;

    private User owner;

    /**
     * Default constructor initializing the property with default values.
     */
    public Property() {
        this(0, 0, "", "", 0.0, 0.0);
    }

    /**
     * Parameterized constructor initializing the property with specified values.
     *
     * @param id          the ID of the property
     * @param ownerId     the ID of the owner (user)
     * @param description the description of the property
     * @param location    the location of the property
     * @param size        the size of the property
     * @param price       the price of the property
     */
    public Property(int id, int ownerId, String description, String location, double size, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.ownerId = new SimpleIntegerProperty(ownerId);
        this.description = new SimpleStringProperty(description);
        this.location = new SimpleStringProperty(location);
        this.size = new SimpleDoubleProperty(size);
        this.price = new SimpleDoubleProperty(price);
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

    public int getOwnerId() {
        return ownerId.get();
    }

    public void setOwnerId(int ownerId) {
        this.ownerId.set(ownerId);
    }

    public IntegerProperty ownerIdProperty() {
        return ownerId;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public StringProperty locationProperty() {
        return location;
    }

    public double getSize() {
        return size.get();
    }

    public void setSize(double size) {
        this.size.set(size);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
