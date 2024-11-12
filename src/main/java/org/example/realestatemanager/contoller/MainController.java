package org.example.realestatemanager.contoller;

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

    @FXML
    private Pagination pagination;

    private static final int ROWS_PER_PAGE = 20;

    // Existing input fields for adding properties
    @FXML
    private TextField ownerField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField priceField;

    // New filter input fields
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
    private DatabaseUtil db;

    // Filtered and Sorted Lists
    private FilteredList<Property> filteredData;
    private SortedList<Property> sortedData;

    /**
     * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        db = new DatabaseUtil();
        loadProperties();

        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        ownerColumn.setCellValueFactory(cellData -> cellData.getValue().ownerProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());

        // Initialize FilteredList with propertyList
        filteredData = new FilteredList<>(propertyList, p -> true);

        // Bind the SortedList comparator to the TableView comparator
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(propertyTable.comparatorProperty());

        // Set the SortedList as the items for the TableView
        propertyTable.setItems(sortedData);

        // Add listeners to filter input fields
        addFilterListeners();

        // Handle row double-click for editing
        propertyTable.setRowFactory(_ -> {
            TableRow<Property> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Property clickedRow = row.getItem();
                    openEditWindow(clickedRow);
                }
            });

            // Context menu for right-click
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

        setupPagination();
    }

    /**
     * Adds listeners to the filter input fields to update the FilteredList predicate.
     */
    private void addFilterListeners() {
        // Owner filter
        generalFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        filterOwnerField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        // Location filter
        filterLocationField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        // Min Price filter
        filterMinPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        // Max Price filter
        filterMaxPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    /**
     * Updates the predicate of the FilteredList based on filter input fields.
     */
    private void updateFilters() {
        filteredData.setPredicate(property -> {
            // If no filters are applied, display all properties
            if (property == null) {
                return false;
            }

            // General filter
            String generalFilter = generalFilterField.getText().toLowerCase().trim();
            if (!generalFilter.isEmpty()) {
                if (!(property.getOwner().toLowerCase().contains(generalFilter) ||
                        property.getLocation().toLowerCase().contains(generalFilter) ||
                        property.getDescription().toLowerCase().contains(generalFilter) ||
                        String.valueOf(property.getPrice()).contains(generalFilter) ||
                        String.valueOf(property.getSize()).contains(generalFilter))) {
                    return false;
                }
            }

            // Owner filter
            String ownerFilter = filterOwnerField.getText().toLowerCase().trim();
            if (!ownerFilter.isEmpty() && !property.getOwner().toLowerCase().contains(ownerFilter)) {
                return false;
            }

            // Location filter
            String locationFilter = filterLocationField.getText().toLowerCase().trim();
            if (!locationFilter.isEmpty() && !property.getLocation().toLowerCase().contains(locationFilter)) {
                return false;
            }

            // Min Price filter
            String minPriceText = filterMinPriceField.getText().trim();
            double minPrice = 0.0;
            if (!minPriceText.isEmpty()) {
                try {
                    minPrice = Double.parseDouble(minPriceText);
                } catch (NumberFormatException e) {
                    // Invalid input; could show an alert or ignore the filter
                    return false;
                }
                if (property.getPrice() < minPrice) {
                    return false;
                }
            }

            // Max Price filter
            String maxPriceText = filterMaxPriceField.getText().trim();
            double maxPrice = Double.MAX_VALUE;
            if (!maxPriceText.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(maxPriceText);
                } catch (NumberFormatException e) {
                    // Invalid input; could show an alert or ignore the filter
                    return false;
                }
                if (property.getPrice() > maxPrice) {
                    return false;
                }
            }

            // All filters passed
            return true;
        });

        // After filtering, reset pagination
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
            propertyList.addAll(db.getAllProperties());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Handles the action of adding a new property.
     *
     * @param event the action event
     */
    @FXML
    private void handleAddProperty(ActionEvent event) {
        String owner = ownerField.getText().trim();
        String description = descriptionField.getText().trim();
        String location = locationField.getText().trim();
        String sizeText = sizeField.getText().trim();
        String priceText = priceField.getText().trim();

        // Validate inputs
        if (owner.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Owner field cannot be empty.");
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

        Property newProperty = new Property(0, owner, description, location, size, price);
        try {
            db.addProperty(newProperty);
            loadProperties(); // Reload properties to include the new one
            updateFilters(); // Re-apply filters
            clearInputFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Property added successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Add Error", e.getMessage());
        }
    }

    /**
     * Clears the input fields in the form.
     */
    private void clearInputFields() {
        ownerField.clear();
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

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    /**
     * Refreshes the property table.
     */
    public void refreshTable() {
        loadProperties();
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

    /**
     * Shows an alert dialog.
     *
     * @param type    the type of alert
     * @param title   the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(propertyTable.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
