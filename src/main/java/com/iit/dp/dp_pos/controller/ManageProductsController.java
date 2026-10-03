package com.iit.dp.dp_pos.controller;

import com.iit.dp.dp_pos.model.Product;
import com.iit.dp.dp_pos.util.ActivityLogger;
import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import com.iit.dp.dp_pos.util.observer.ProductObserver;
import com.iit.dp.dp_pos.util.observer.ProductAlertManager;
import com.iit.dp.dp_pos.util.observer.AlertDialogHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;

public class ManageProductsController implements ProductObserver {
    private MainController mainController;
    private String returnView = "employee-dashboard-view.fxml"; // Default to employee dashboard

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> buyingPriceColumn;
    @FXML private TableColumn<Product, Double> sellingPriceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, LocalDate> addingDateColumn;
    @FXML private TableColumn<Product, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Product, Void> actionColumn;
    @FXML private TableColumn<Product, String> categoryColumn;

    @FXML private TextField newProductNameField;
    @FXML private TextField newProductBuyingPriceField;
    @FXML private TextField newProductSellingPriceField;
    @FXML private TextField newProductQuantityField;
    @FXML private DatePicker newProductAddingDatePicker;
    @FXML private DatePicker newProductExpiryDatePicker;
    @FXML private TextField newProductCategoryField;
    @FXML private ComboBox<String> newProductCategoryComboBox;
    @FXML private Label messageLabel;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set which view to return to when back button is clicked
     * @param returnView The FXML file to return to (e.g., "hello-view.fxml" for admin, "employee-dashboard-view.fxml" for employee)
     */
    public void setReturnView(String returnView) {
        this.returnView = returnView;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBox();
        loadProducts();

        // Register as observer for product alerts
        ProductAlertManager.getInstance().addObserver(this);

        // Check for alerts when page loads
        ProductAlertManager.getInstance().checkProductAlerts();
    }

    private void setupComboBox() {
        // Add predefined categories to the ComboBox
        ObservableList<String> categories = FXCollections.observableArrayList(
            "Edible", "Wearable", "General", "Electronics", "Books", "Sports", "Health & Beauty"
        );
        newProductCategoryComboBox.setItems(categories);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Buying Price column with styling
        buyingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("buyingPrice"));
        buyingPriceColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label price = new Label("$" + String.format("%.2f", item));
                    price.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
                    setGraphic(price);
                }
            }
        });

        // Selling Price column with styling
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        sellingPriceColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label price = new Label("$" + String.format("%.2f", item));
                    price.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    setGraphic(price);
                }
            }
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        addingDateColumn.setCellValueFactory(new PropertyValueFactory<>("addingDate"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
    }

    // Load products from database with all new fields
    private void loadProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, buying_price, selling_price, quantity, adding_date, expiry_date, category FROM products");
            while (rs.next()) {
                // Safe date parsing with better error handling for TEXT date fields
                LocalDate addingDate = null;
                LocalDate expiryDate = null;

                try {
                    // Parse TEXT date format (YYYY-MM-DD)
                    String addingDateStr = rs.getString("adding_date");
                    if (addingDateStr != null && !addingDateStr.isEmpty()) {
                        addingDate = LocalDate.parse(addingDateStr);
                    }
                } catch (Exception dateEx) {
                    addingDate = LocalDate.now(); // Default to today
                }

                try {
                    // Parse TEXT date format (YYYY-MM-DD)
                    String expiryDateStr = rs.getString("expiry_date");
                    if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                        expiryDate = LocalDate.parse(expiryDateStr);
                    }
                } catch (Exception dateEx) {
                    expiryDate = LocalDate.now().plusYears(1); // Default to one year from now
                }

                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"), // Category should be third parameter
                    rs.getDouble("buying_price"),
                    rs.getDouble("selling_price"),
                    rs.getInt("quantity"),
                    addingDate,
                    expiryDate
                );
                products.add(product);
            }

            if (products.isEmpty()) {
                messageLabel.setText("No products found. Add your first product above!");
                messageLabel.setStyle("-fx-text-fill: #666;");
            } else {
                messageLabel.setText("");
            }
        } catch (Exception e) {
            // Removed silent error handling: if loading fails, show a message for debugging
            messageLabel.setText("Error loading products: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #F44336;");
        }
        productTable.setItems(products);
    }

    @FXML
    private void onAddProductClick() {
        String name = newProductNameField.getText().trim();
        String buyingPriceText = newProductBuyingPriceField.getText().trim();
        String sellingPriceText = newProductSellingPriceField.getText().trim();
        String quantityText = newProductQuantityField.getText().trim();
        LocalDate addingDate = newProductAddingDatePicker.getValue();
        LocalDate expiryDate = newProductExpiryDatePicker.getValue();
        String category = newProductCategoryComboBox.getValue(); // Use ComboBox instead of text field

        // Validation
        if (name.isEmpty() || buyingPriceText.isEmpty() || sellingPriceText.isEmpty() ||
            quantityText.isEmpty() || addingDate == null || expiryDate == null || category == null || category.isEmpty()) {
            messageLabel.setText("❌ Please fill in all fields!");
            messageLabel.setStyle("-fx-text-fill: #F44336;");
            return;
        }

        try {
            double buyingPrice = Double.parseDouble(buyingPriceText);
            double sellingPrice = Double.parseDouble(sellingPriceText);
            int quantity = Integer.parseInt(quantityText);

            if (buyingPrice < 0 || sellingPrice < 0 || quantity < 0) {
                messageLabel.setText("❌ Prices and quantity must be positive!");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            if (sellingPrice <= buyingPrice) {
                messageLabel.setText("❌ Selling price must be greater than buying price!");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            if (expiryDate.isBefore(addingDate)) {
                messageLabel.setText("❌ Expiry date must be after adding date!");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            // Add product to database
            addProductToDatabase(name, buyingPrice, sellingPrice, quantity, addingDate, expiryDate, category);

        } catch (NumberFormatException e) {
            messageLabel.setText("❌ Please enter valid numbers for prices and quantity!");
            messageLabel.setStyle("-fx-text-fill: #F44336;");
        }
    }

    private void addProductToDatabase(String name, double buyingPrice, double sellingPrice, int quantity, LocalDate addingDate, LocalDate expiryDate, String category) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "INSERT INTO products (name, buying_price, selling_price, quantity, adding_date, expiry_date, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setDouble(2, buyingPrice);
            stmt.setDouble(3, sellingPrice);
            stmt.setInt(4, quantity);
            // Use TEXT format for dates instead of Date objects
            stmt.setString(5, addingDate.toString()); // Converts to YYYY-MM-DD format
            stmt.setString(6, expiryDate.toString()); // Converts to YYYY-MM-DD format
            stmt.setString(7, category);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                messageLabel.setText("✅ Product added successfully!");
                messageLabel.setStyle("-fx-text-fill: #4CAF50;");
                clearFields();
                loadProducts(); // Refresh the table

                // Log the activity
                String userEmail = ActivityLogger.getEmailByUserId(AuthController.loggedInUserId);
                ActivityLogger.log(userEmail, "Add Product",
                    String.format("Added product: %s (Buying: $%.2f, Selling: $%.2f, Qty: %d, Category: %s)",
                    name, buyingPrice, sellingPrice, quantity, category));
            } else {
                messageLabel.setText("❌ Failed to add product!");
                messageLabel.setStyle("-fx-text-fill: #F44336;");
            }
        } catch (Exception e) {
            messageLabel.setText("❌ Error adding product: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #F44336;");
            e.printStackTrace(); // Print full stack trace for debugging
        }
    }

    private void clearFields() {
        newProductNameField.clear();
        newProductBuyingPriceField.clear();
        newProductSellingPriceField.clear();
        newProductQuantityField.clear();
        newProductCategoryComboBox.setValue(null); // Clear ComboBox instead of text field
        newProductAddingDatePicker.setValue(LocalDate.of(2025, 9, 18));
        newProductExpiryDatePicker.setValue(null);
    }

    // Action buttons for increment/decrement quantity
    private void updateQuantity(Product item, int delta) {
        int oldQuantity = item.getQuantity();
        int newQuantity = oldQuantity + delta;
        if (newQuantity < 0) return;

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?");
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, item.getId());
            stmt.executeUpdate();

            item.setQuantity(newQuantity);
            productTable.refresh();

            // Log the activity
            String userEmail = ActivityLogger.getEmailByUserId(AuthController.loggedInUserId);
            String action = (delta > 0) ? "Restock Product" : "Reduce Product Stock";
            ActivityLogger.log(userEmail, action,
                String.format("Updated %s quantity from %d to %d", item.getName(), oldQuantity, newQuantity));

        } catch (Exception e) {
            messageLabel.setText("Error updating quantity: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #F44336;");
        }
    }

    @FXML
    private void onBackClick() {
        if (mainController != null) {
            // Unregister from product alerts to avoid duplicate dialogs when returning later
            try {
                com.iit.dp.dp_pos.util.observer.ProductAlertManager.getInstance().removeObserver(this);
            } catch (Exception ignored) {}

            mainController.loadView(returnView);
        }
    }

    @Override
    public void onProductOutOfStock(Product product) {
        AlertDialogHelper.showProductAlerts(
            ProductAlertManager.getInstance().getOutOfStockProducts(),
            ProductAlertManager.getInstance().getExpiredProducts()
        );
    }

    @Override
    public void onProductExpired(Product product) {
        AlertDialogHelper.showProductAlerts(
            ProductAlertManager.getInstance().getOutOfStockProducts(),
            ProductAlertManager.getInstance().getExpiredProducts()
        );
    }
}
