package org.example.realestatemanager.contoller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.realestatemanager.entity.User;
import org.example.realestatemanager.utils.DatabaseUtil;

import java.sql.SQLException;

/**
 * Controller class for editing an existing user.
 */
public class EditUserController {

    @FXML
    private Label idLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;

    private User user;
    private DatabaseUtil db;
    private MainController mainController;

    /**
     * Sets the user to be edited.
     *
     * @param user the user to edit
     */
    public void setUser(User user) {
        this.user = user;
        idLabel.setText(String.valueOf(user.getId()));
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
    }

    /**
     * Sets the database utility.
     *
     * @param db the database utility
     */
    public void setDatabase(DatabaseUtil db) {
        this.db = db;
    }

    /**
     * Sets the main controller to refresh the user table after editing.
     *
     * @param controller the main controller
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    /**
     * Handles the save action for editing a user.
     *
     * @param event the action event
     */
    @FXML
    private void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Name field cannot be empty.");
            return;
        }

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Email field cannot be empty.");
            return;
        }

        user.setName(name);
        user.setEmail(email);

        try {
            db.updateUser(user);
            mainController.refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", e.getMessage());
        }
    }

    /**
     * Closes the edit user window.
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
