package org.example.realestatemanager.contoller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.realestatemanager.Main;
import org.example.realestatemanager.entity.Property;
import org.example.realestatemanager.entity.User;
import org.example.realestatemanager.utils.DatabaseUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Controller class for the main application window.
 */
public class MainController {

    // --- Property Table ---
    @FXML
    private TableView<Property> propertyTable;
    @FXML
    private TableColumn<Property, Number> idColumn;
    @FXML
    private TableColumn<Property, String> ownerColumn;
    @FXML
    private TableColumn<Property, String> descriptionColumn;
    @FXML
    private TableColumn<Property, String> locationColumn;
    @FXML
    private TableColumn<Property, Number> sizeColumn;
    @FXML
    private TableColumn<Property, Number> priceColumn;

    // --- User Table ---
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Number> userIdColumn;
    @FXML
    private TableColumn<User, String> userNameColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;

    @FXML
    private Pagination pagination;

    private static final int ROWS_PER_PAGE = 20;

    @FXML
    private ComboBox<User> ownerComboBox;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField priceField;

    @FXML
    private TextField generalFilterField;

    @FXML
    private TextField filterOwnerField;
    @FXML
    private TextField filterLocationField;
    @FXML
    private TextField filterMinPriceField;
    @FXML
    private TextField filterMaxPriceField;

    private ObservableList<Property> propertyList = FXCollections.observableArrayList();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private DatabaseUtil db;

    private FilteredList<Property> filteredData;
    private SortedList<Property> sortedData;

    /**
     * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        db = new DatabaseUtil();
        loadUsers();
        loadProperties();

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        ownerColumn.setCellValueFactory(cellData -> {
            User owner = cellData.getValue().getOwner();
            return owner != null ? owner.nameProperty() : new SimpleStringProperty("Unknown");
        });
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());

        userTable.setItems(userList);

        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        userNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        userEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        filteredData = new FilteredList<>(propertyList, p -> true);

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(propertyTable.comparatorProperty());

        propertyTable.setItems(sortedData);

        ownerComboBox.setItems(userList);

        addFilterListeners();

        propertyTable.setRowFactory(_ -> {
            TableRow<Property> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Property clickedRow = row.getItem();
                    openEditWindow(clickedRow);
                }
            });

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem("Edit");
                    editItem.setOnAction(e -> openEditWindow(row.getItem()));
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> deleteProperty(row.getItem()));
                    contextMenu.getItems().addAll(editItem, deleteItem);
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });

        userTable.setRowFactory(_ -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    User clickedUser = row.getItem();
                    openEditUserWindow(clickedUser);
                }
            });

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem("Edit");
                    editItem.setOnAction(e -> openEditUserWindow(row.getItem()));
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> deleteUser(row.getItem()));
                    contextMenu.getItems().addAll(editItem, deleteItem);
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });

        setupPagination();
    }

    /**
     * Adds listeners to the filter input fields to update the FilteredList predicate.
     */
    private void addFilterListeners() {
        generalFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        filterOwnerField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        filterLocationField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        filterMinPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        filterMaxPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    /**
     * Updates the predicate of the FilteredList based on filter input fields.
     */
    private void updateFilters() {
        filteredData.setPredicate(property -> {
            if (property == null) {
                return false;
            }

            String generalFilter = generalFilterField.getText().toLowerCase().trim();
            if (!generalFilter.isEmpty()) {
                if (!(property.getOwner() != null && property.getOwner().getName().toLowerCase().contains(generalFilter) ||
                        property.getLocation().toLowerCase().contains(generalFilter) ||
                        property.getDescription().toLowerCase().contains(generalFilter) ||
                        String.valueOf(property.getPrice()).contains(generalFilter) ||
                        String.valueOf(property.getSize()).contains(generalFilter))) {
                    return false;
                }
            }

            String ownerFilter = filterOwnerField.getText().toLowerCase().trim();
            if (!ownerFilter.isEmpty()) {
                if (property.getOwner() == null || !property.getOwner().getName().toLowerCase().contains(ownerFilter)) {
                    return false;
                }
            }

            String locationFilter = filterLocationField.getText().toLowerCase().trim();
            if (!locationFilter.isEmpty() && !property.getLocation().toLowerCase().contains(locationFilter)) {
                return false;
            }

            String minPriceText = filterMinPriceField.getText().trim();
            double minPrice = 0.0;
            if (!minPriceText.isEmpty()) {
                try {
                    minPrice = Double.parseDouble(minPriceText);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (property.getPrice() < minPrice) {
                    return false;
                }
            }

            String maxPriceText = filterMaxPriceField.getText().trim();
            double maxPrice = Double.MAX_VALUE;
            if (!maxPriceText.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(maxPriceText);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (property.getPrice() > maxPrice) {
                    return false;
                }
            }

            return true;
        });

        setupPagination();
    }

    /**
     * Sets up the pagination control for the property table based on the filtered and sorted data.
     */
    private void setupPagination() {
        int totalProperties = filteredData.size();
        int pageCount = (int) Math.ceil((double) totalProperties / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    /**
     * Creates a page for the pagination control.
     *
     * @param pageIndex the index of the page to create
     * @return a VBox containing the table of properties
     */
    private VBox createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, sortedData.size());
        propertyTable.setItems(FXCollections.observableArrayList(sortedData.subList(fromIndex, toIndex)));
        return new VBox(propertyTable);
    }

    /**
     * Loads all properties from the database into the property list.
     */
    private void loadProperties() {
        propertyList.clear();
        try {
            List<Property> properties = db.getAllProperties();
            propertyList.addAll(properties);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Loads all users from the database into the user list.
     */
    private void loadUsers() {
        userList.clear();
        try {
            List<User> users = db.getAllUsers();
            userList.addAll(users);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
        userTable.setItems(userList);
    }

    /**
     * Handles the action of adding a new property.
     *
     * @param event the action event
     */
    @FXML
    private void handleAddProperty(ActionEvent event) {
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

        Property newProperty = new Property(0, selectedUser.getId(), description, location, size, price);
        try {
            db.addProperty(newProperty);
            loadProperties();
            updateFilters();
            clearPropertyInputFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Property added successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Add Error", e.getMessage());
        }
    }

    /**
     * Clears the input fields in the property form.
     */
    private void clearPropertyInputFields() {
        ownerComboBox.getSelectionModel().clearSelection();
        descriptionField.clear();
        locationField.clear();
        sizeField.clear();
        priceField.clear();
    }

    /**
     * Opens the edit window for a selected property.
     *
     * @param property the property to edit
     */
    private void openEditWindow(Property property) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("edit.fxml"));
            Parent root = loader.load();

            EditController controller = loader.getController();
            controller.setProperty(property);
            controller.setDatabase(db);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Property");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(propertyTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProperties();
            updateFilters();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Refreshes the property table.
     */
    public void refreshTable() {
        loadProperties();
        loadUsers();
        updateFilters();
    }

    /**
     * Deletes a selected property.
     *
     * @param property the property to delete
     */
    private void deleteProperty(Property property) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this property?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                db.deleteProperty(property.getId());
                loadProperties();
                updateFilters();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Property deleted successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", e.getMessage());
            }
        }
    }

    // ------------------- User Management Methods -------------------

    /**
     * Handles the action of adding a new user.
     *
     * @param event the action event
     */
    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("addUser.fxml"));
            Parent root = loader.load();

            AddUserController controller = loader.getController();
            controller.setDatabase(db);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadUsers();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Opens the edit window for a selected user.
     *
     * @param user the user to edit
     */
    private void openEditUserWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("editUser.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);
            controller.setDatabase(db);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadUsers();
            loadProperties();
            updateFilters();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Deletes a selected user.
     *
     * @param user the user to delete
     */
    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this user? All associated properties will also be deleted.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                db.deleteUser(user.getId());
                loadUsers();
                loadProperties();
                updateFilters();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", e.getMessage());
            }
        }
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
        Stage stage = (Stage) propertyTable.getScene().getWindow();
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the exit action from the menu.
     *
     * @param event the action event
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) propertyTable.getScene().getWindow();
        stage.close();
    }
}
