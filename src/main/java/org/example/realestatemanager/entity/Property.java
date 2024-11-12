package org.example.realestatemanager.entity;

import javafx.beans.property.*;

/**
 * Represents a property in the real estate management system.
 */
public class Property {
    private final IntegerProperty id;
    private final StringProperty owner;
    private final StringProperty description;
    private final StringProperty location;
    private final DoubleProperty size;
    private final DoubleProperty price;

    /**
     * Default constructor initializing the property with default values.
     */
    public Property() {
        this(0, "", "", "", 0.0, 0.0);
    }

    /**
     * Parameterized constructor initializing the property with specified values.
     *
     * @param id the ID of the property
     * @param owner the owner of the property
     * @param description the description of the property
     * @param location the location of the property
     * @param size the size of the property
     * @param price the price of the property
     */
    public Property(int id, String owner, String description, String location, double size, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.owner = new SimpleStringProperty(owner);
        this.description = new SimpleStringProperty(description);
        this.location = new SimpleStringProperty(location);
        this.size = new SimpleDoubleProperty(size);
        this.price = new SimpleDoubleProperty(price);
    }

    // Getters and Setters

    /**
     * Gets the ID of the property.
     *
     * @return the ID of the property
     */
    public int getId() { return id.get(); }

    /**
     * Sets the ID of the property.
     *
     * @param id the ID to set
     */
    public void setId(int id) { this.id.set(id); }

    /**
     * Gets the ID property.
     *
     * @return the ID property
     */
    public IntegerProperty idProperty() { return id; }

    /**
     * Gets the owner of the property.
     *
     * @return the owner of the property
     */
    public String getOwner() { return owner.get(); }

    /**
     * Sets the owner of the property.
     *
     * @param owner the owner to set
     */
    public void setOwner(String owner) { this.owner.set(owner); }

    /**
     * Gets the owner property.
     *
     * @return the owner property
     */
    public StringProperty ownerProperty() { return owner; }

    /**
     * Gets the description of the property.
     *
     * @return the description of the property
     */
    public String getDescription() { return description.get(); }

    /**
     * Sets the description of the property.
     *
     * @param description the description to set
     */
    public void setDescription(String description) { this.description.set(description); }

    /**
     * Gets the description property.
     *
     * @return the description property
     */
    public StringProperty descriptionProperty() { return description; }

    /**
     * Gets the location of the property.
     *
     * @return the location of the property
     */
    public String getLocation() { return location.get(); }

    /**
     * Sets the location of the property.
     *
     * @param location the location to set
     */
    public void setLocation(String location) { this.location.set(location); }

    /**
     * Gets the location property.
     *
     * @return the location property
     */
    public StringProperty locationProperty() { return location; }

    /**
     * Gets the size of the property.
     *
     * @return the size of the property
     */
    public double getSize() { return size.get(); }

    /**
     * Sets the size of the property.
     *
     * @param size the size to set
     */
    public void setSize(double size) { this.size.set(size); }

    /**
     * Gets the size property.
     *
     * @return the size property
     */
    public DoubleProperty sizeProperty() { return size; }

    /**
     * Gets the price of the property.
     *
     * @return the price of the property
     */
    public double getPrice() { return price.get(); }

    /**
     * Sets the price of the property.
     *
     * @param price the price to set
     */
    public void setPrice(double price) { this.price.set(price); }

    /**
     * Gets the price property.
     *
     * @return the price property
     */
    public DoubleProperty priceProperty() { return price; }
}