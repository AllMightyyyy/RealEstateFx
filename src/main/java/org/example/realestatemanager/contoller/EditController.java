package org.example.realestatemanager.contoller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.realestatemanager.contoller.MainController;
import org.example.realestatemanager.entity.Property;
import org.example.realestatemanager.entity.User;
import org.example.realestatemanager.utils.DatabaseUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller class for the edit property window.
 */
public class EditController {

    @FXML
    private Label idLabel;
    @FXML
    private ComboBox<User> ownerComboBox;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField priceField;

    private Property property;
    private DatabaseUtil db;
    private MainController mainController;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    /**
     * Sets the property to be edited.
     *
     * @param property the property to edit
     */
    public void setProperty(Property property) {
        this.property = property;
        idLabel.setText(String.valueOf(property.getId()));
        ownerComboBox.getSelectionModel().select(property.getOwner());
        descriptionField.setText(property.getDescription());
        locationField.setText(property.getLocation());
        sizeField.setText(String.valueOf(property.getSize()));
        priceField.setText(String.valueOf(property.getPrice()));
    }

    /**
     * Sets the database utility.
     *
     * @param db the database utility
     */
    public void setDatabase(DatabaseUtil db) {
        this.db = db;
        loadUsers();
    }

    /**
     * Sets the main controller.
     *
     * @param controller the main controller
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    /**
     * Loads all users into the ComboBox.
     */
    private void loadUsers() {
        try {
            List<User> users = db.getAllUsers();
            userList.setAll(users);
            ownerComboBox.setItems(userList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Handles the save action.
     *
     * @param event the action event
     */
    @FXML
    private void handleSave(ActionEvent event) {
        User selectedUser = ownerComboBox.getSelectionModel().getSelectedItem();
        String description = descriptionField.getText().trim();
        String location = locationField.getText().trim();
        String sizeText = sizeField.getText().trim();
        String priceText = priceField.getText().trim();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select an owner from the list.");
            return;
        }

        double size;
        double price;
        try {
            size = Double.parseDouble(sizeText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Size and Price must be valid numbers.");
            return;
        }

        property.setOwnerId(selectedUser.getId());
        property.setDescription(description);
        property.setLocation(location);
        property.setSize(size);
        property.setPrice(price);

        try {
            db.updateProperty(property);
            mainController.refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Property updated successfully.");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", e.getMessage());
        }
    }

    /**
     * Closes the edit window.
     */
    private void closeWindow() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an alert dialog.
     *
     * @param type    the type of alert
     * @param title   the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        Stage stage = (Stage) idLabel.getScene().getWindow();
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
