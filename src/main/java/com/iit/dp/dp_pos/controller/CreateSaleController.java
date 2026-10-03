package com.iit.dp.dp_pos.controller;

import com.iit.dp.dp_pos.model.CartItem;
import com.iit.dp.dp_pos.model.Product;
import com.iit.dp.dp_pos.model.PausedOrder;
import com.iit.dp.dp_pos.util.ActivityLogger;
import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import com.iit.dp.dp_pos.util.memento.PausedSaleManager;
import com.iit.dp.dp_pos.util.memento.SaleMemento;
import com.iit.dp.dp_pos.util.observer.ProductObserver;
import com.iit.dp.dp_pos.util.observer.ProductAlertManager;
import com.iit.dp.dp_pos.util.observer.AlertDialogHelper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CreateSaleController implements ProductObserver {
    private MainController mainController;
    
    @FXML private TextField searchField;
    @FXML private TableView<ProductEntry> productTable;
    @FXML private TableView<CartEntry> cartTable;
    @FXML private Label totalLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private Button checkoutButton;
    @FXML private Button clearCartButton;
    @FXML private Button backButton;
    @FXML private Button pauseOrderButton;

    private final ObservableList<ProductEntry> products = FXCollections.observableArrayList();
    private final ObservableList<CartEntry> cartItems = FXCollections.observableArrayList();
    private FilteredList<ProductEntry> filteredProducts;

    // Replace old PausedOrderManager with new Memento-based manager
    private final PausedSaleManager pausedSaleManager = PausedSaleManager.getInstance();

    // Flag to prevent immediate detection after pausing
    private boolean justPausedOrder = false;
    // Flag to suppress detection when setting fields programmatically (prevents double prompts)
    private boolean suppressPausedDetection = false;

    // Utility to run code with paused detection suppressed
    private void withSuppressedDetection(Runnable r) {
        boolean prev = suppressPausedDetection;
        suppressPausedDetection = true;
        try { r.run(); } finally { suppressPausedDetection = prev; }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public static class ProductEntry {
        private final SimpleIntegerProperty productId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty quantity;

        public ProductEntry(int productId, String name, String category, double price, int quantity) {
            this.productId = new SimpleIntegerProperty(productId);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
        }

        public int getProductId() { return productId.get(); }
        public String getName() { return name.get(); }
        public String getCategory() { return category.get(); }
        public double getPrice() { return price.get(); }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int value) { quantity.set(value); }
    }

    public static class CartEntry {
        private final int productId;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty total;

        public CartEntry(int productId, String name, double price, int quantity) {
            this.productId = productId;
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.total = new SimpleDoubleProperty(price * quantity);

            this.quantity.addListener((obs, oldVal, newVal) ->
                this.total.set(this.price.get() * newVal.intValue()));
        }

        public int getProductId() { return productId; }
        public String getName() { return name.get(); }
        public double getPrice() { return price.get(); }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int value) { quantity.set(value); }
        public double getTotal() { return total.get(); }
    }

    @FXML
    public void initialize() {
        setupTables();
        setupSearch();
        setupButtons();
        setupCustomerFields();
        loadProducts();
        updateTotal();

        // Register as observer for product alerts
        ProductAlertManager.getInstance().addObserver(this);

        // Check for alerts when page loads
        ProductAlertManager.getInstance().checkProductAlerts();

        // Enable paused sale detection when typing name/email
        checkForPausedOrderOnEmailInput();
    }

    private void checkForPausedOrderOnEmailInput() {
        customerEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressPausedDetection) return;
            if (newVal != null && !newVal.trim().isEmpty()) {
                checkForPausedSaleMemento(customerNameField.getText(), newVal.trim());
            }
        });

        customerNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressPausedDetection) return;
            if (newVal != null && !newVal.trim().isEmpty()) {
                checkForPausedSaleMemento(newVal.trim(), customerEmailField.getText());
            }
        });
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

    /**
     * Check for paused sales using Memento pattern
     */
    private void checkForPausedSaleMemento(String customerName, String customerEmail) {
        // Skip detection if we just paused an order
        if (justPausedOrder) {
            justPausedOrder = false; // Reset flag
            return;
        }

        if ((customerName != null && !customerName.trim().isEmpty()) ||
            (customerEmail != null && !customerEmail.trim().isEmpty())) {

            SaleMemento pausedSale = pausedSaleManager.findPausedSale(customerName, customerEmail);
            if (pausedSale != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Paused Sale Found");
                alert.setHeaderText("Found a paused sale for " + pausedSale.getCustomerName());
                alert.setContentText(String.format("Paused on: %s\nItems: %d\nTotal: $%.2f\n\nWould you like to restore this sale?",
                    pausedSale.getPausedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    pausedSale.getCartItems().size(),
                    pausedSale.getTotalAmount()));

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        restorePausedSaleFromMemento(pausedSale);
                    }
                });
            }
        }
    }

    private void setupCustomerFields() {
        customerNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !customerNameField.getText().trim().isEmpty()) {
                lookupCustomerEmail(customerNameField.getText().trim());
            }
        });
        customerEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                lookupCustomerName(newVal.trim());
            }
            // Removed clearing of customerNameField when email is empty
        });
    }

    // auto email lookup based on customer name
    private void lookupCustomerEmail(String name) {
        String query = "SELECT email FROM customers WHERE name = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                withSuppressedDetection(() -> customerEmailField.setText(email));
            }
            // Removed clearing of customerEmailField if not found
        } catch (SQLException e) {
            showError("Database Error", "Could not lookup customer: " + e.getMessage());
        }
    }

    // auto name lookup based on customer email
    private void lookupCustomerName(String email) {
        String query = "SELECT name FROM customers WHERE email = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                withSuppressedDetection(() -> customerNameField.setText(name));
            }
            // Removed clearing of customerNameField if not found
        } catch (SQLException e) {
            showError("Database Error", "Could not lookup customer: " + e.getMessage());
        }
    }

    private void setupTables() {
        // Product table setup
        TableColumn<ProductEntry, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().productId);
        idCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText(id.toString());
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        idCol.setPrefWidth(60);

        TableColumn<ProductEntry, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> data.getValue().name);
        nameCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        nameCol.setPrefWidth(200);

        TableColumn<ProductEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().category);
        categoryCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        categoryCol.setPrefWidth(150);

        TableColumn<ProductEntry, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> data.getValue().price);
        priceCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price.doubleValue()));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                }
            }
        });
        priceCol.setPrefWidth(100);

        TableColumn<ProductEntry, Number> quantityCol = new TableColumn<>("Stock Level");
        quantityCol.setCellValueFactory(data -> data.getValue().quantity);
        quantityCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(quantity.toString() + " units");
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        quantityCol.setPrefWidth(100);

        productTable.getColumns().addAll(idCol, nameCol, categoryCol, priceCol, quantityCol);
        productTable.setItems(products);

        // Double-click handler for products table
        productTable.setRowFactory(tv -> {
            TableRow<ProductEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ProductEntry product = row.getItem();
                    if (product != null && product.getQuantity() > 0) {
                        addToCart(product);
                    }
                }
            });
            return row;
        });

        // Cart table setup
        TableColumn<CartEntry, String> cartNameCol = new TableColumn<>("Item");
        cartNameCol.setCellValueFactory(data -> data.getValue().name);
        cartNameCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        cartNameCol.setPrefWidth(200);

        TableColumn<CartEntry, Number> cartPriceCol = new TableColumn<>("Price");
        cartPriceCol.setCellValueFactory(data -> data.getValue().price);
        cartPriceCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) setText(null);
                else {
                    setText(String.format("$%.2f", price.doubleValue()));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            }
        });
        cartPriceCol.setPrefWidth(100);

        TableColumn<CartEntry, Number> cartQuantityCol = new TableColumn<>("Quantity");
        cartQuantityCol.setCellValueFactory(data -> data.getValue().quantity);
        cartQuantityCol.setCellFactory(tc -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 100, 1);

            {
                spinner.setEditable(true);
                spinner.setPrefWidth(80);
            }

            @Override
            protected void updateItem(Number quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CartEntry entry = getTableView().getItems().get(getIndex());
                    ProductEntry product = products.stream()
                        .filter(p -> p.getProductId() == entry.getProductId())
                        .findFirst()
                        .orElse(null);

                    if (product != null) {
                        int maxQuantity = product.getQuantity() + entry.getQuantity();
                        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            1, Math.max(1, maxQuantity), entry.getQuantity()));

                        // Remove previous listener if any
                        Object existing = spinner.getProperties().get("qtyListener");
                        if (existing instanceof javafx.beans.value.ChangeListener) {
                            spinner.valueProperty().removeListener((javafx.beans.value.ChangeListener<? super Integer>) existing);
                        }

                        javafx.beans.value.ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
                            if (oldVal == null || newVal == null) return;
                            int oldQ = oldVal.intValue();
                            int newQ = newVal.intValue();
                            int diff = newQ - oldQ; // positive if increased, negative if decreased

                            // When cart quantity increases, available product quantity should decrease accordingly
                            product.setQuantity(product.getQuantity() - diff);

                            // Ensure product quantity never goes negative
                            if (product.getQuantity() < 0) {
                                // revert change and notify user
                                product.setQuantity(product.getQuantity() + diff);
                                // set spinner back to old value
                                javafx.application.Platform.runLater(() -> spinner.getValueFactory().setValue(oldQ));
                                showError("Stock Limit", "Not enough stock available for this change.");
                                return;
                            }

                            entry.setQuantity(newQ);
                            productTable.refresh();
                            updateTotal();
                        };

                        spinner.valueProperty().addListener(listener);
                        spinner.getProperties().put("qtyListener", listener);
                    }
                    setGraphic(spinner);
                }
            }
        });
        cartQuantityCol.setPrefWidth(100);

        TableColumn<CartEntry, Number> cartTotalCol = new TableColumn<>("Total");
        cartTotalCol.setCellValueFactory(data -> data.getValue().total);
        cartTotalCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", total.doubleValue()));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            }
        });
        cartTotalCol.setPrefWidth(100);

        TableColumn<CartEntry, Void> removeCol = new TableColumn<>("Action");
        removeCol.setCellFactory(tc -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(e -> {
                    CartEntry entry = getTableView().getItems().get(getIndex());
                    removeFromCart(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });
        removeCol.setPrefWidth(80);

        cartTable.getColumns().addAll(cartNameCol, cartPriceCol, cartQuantityCol, cartTotalCol, removeCol);
        cartTable.setItems(cartItems);
    }

    private void setupSearch() {
        filteredProducts = new FilteredList<>(products, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
            filteredProducts.setPredicate(product ->
                newVal == null || newVal.isEmpty() ||
                product.getName().toLowerCase().contains(newVal.toLowerCase())
            )
        );
        productTable.setItems(filteredProducts);
    }

    private void setupButtons() {
        clearCartButton.setOnAction(e -> clearCart());
        checkoutButton.setOnAction(e -> processCheckout());
        backButton.setOnAction(e -> goBack(e));
        pauseOrderButton.setOnAction(e -> pauseOrder());
    }

    private void loadProducts() {
        String query = "SELECT id, name, quantity, selling_price, category FROM products WHERE quantity > 0";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(new ProductEntry(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("selling_price"),
                    rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not load products: " + e.getMessage());
        }
    }

    private void addToCart(ProductEntry product) {
        if (product.getQuantity() <= 0) {
            showError("Out of Stock", "This product is out of stock.");
            return;
        }

        // Check if product already in cart by productId
        for (CartEntry entry : cartItems) {
            if (entry.getProductId() == product.getProductId()) {
                // allow increment only if there is available stock
                if (product.getQuantity() > 0) {
                    entry.setQuantity(entry.getQuantity() + 1);
                    product.setQuantity(product.getQuantity() - 1);  // Update available quantity
                    cartTable.refresh();
                    productTable.refresh();
                    updateTotal();
                } else {
                    showError("Stock Limit", "Cannot add more of this item - stock limit reached.");
                }
                return;
            }
        }

        // Add new item to cart with productId
        cartItems.add(new CartEntry(product.getProductId(), product.getName(), product.getPrice(), 1));
        product.setQuantity(product.getQuantity() - 1);  // Update available quantity
        cartTable.refresh();
        productTable.refresh();
        updateTotal();
    }

    private void removeFromCart(CartEntry cartEntry) {
        // Find the corresponding product and restore its quantity
        for (ProductEntry product : products) {
            if (product.getProductId() == cartEntry.getProductId()) {
                product.setQuantity(product.getQuantity() + cartEntry.getQuantity());
                break;
            }
        }
        cartItems.remove(cartEntry);
        cartTable.refresh();
        productTable.refresh();
        updateTotal();
    }

    private void clearCart() {
        // Restore quantities of all products in the cart
        for (CartEntry entry : cartItems) {
            for (ProductEntry product : products) {
                if (product.getProductId() == entry.getProductId()) {
                    product.setQuantity(product.getQuantity() + entry.getQuantity());
                    break;
                }
            }
        }
        cartItems.clear();
        cartTable.refresh();
        productTable.refresh();
        updateTotal();
    }

    private void goBack(ActionEvent event) {
        // Unregister from product alerts to avoid duplicate dialogs when returning later
        try {
            com.iit.dp.dp_pos.util.observer.ProductAlertManager.getInstance().removeObserver(this);
        } catch (Exception ignored) {}

        // Navigate back to the appropriate dashboard using MainController
        String dashboard = "employee-dashboard-view.fxml";
        try {
            if (com.iit.dp.dp_pos.controller.AuthController.userType != null &&
                    com.iit.dp.dp_pos.controller.AuthController.userType.equalsIgnoreCase("admin")) {
                dashboard = "hello-view.fxml"; // admin dashboard
            }
        } catch (Exception ignored) {
        }

        if (mainController != null) {
            mainController.loadView(dashboard);
            return;
        }

        // Fallback: if mainController isn't available, replace the current stage content
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/iit/dp/dp_pos/" + dashboard));
            javafx.scene.Parent view = loader.load();

            // If the stage already has a scene, replace its root, otherwise set a new scene
            if (stage.getScene() != null) {
                stage.getScene().setRoot(view);
            } else {
                stage.setScene(new javafx.scene.Scene(view));
            }
            stage.show();
        } catch (Exception e) {
            // As a last resort, hide the window (previous behavior). But log the exception first.
            e.printStackTrace();
            ((Node) event.getSource()).getScene().getWindow().hide();
        }
    }

    private void pauseOrder() {
        // Gather cart items with correct mapping
        List<CartItem> itemsToPause = cartItems.stream()
            .map(entry -> new CartItem(entry.getProductId(), entry.getName(), entry.getPrice(), entry.getQuantity()))
            .collect(Collectors.toList());

        String customerName = customerNameField.getText().trim();
        String customerEmail = customerEmailField.getText().trim();
        double totalAmount = cartItems.stream().mapToDouble(CartEntry::getTotal).sum();
        String employeeId = String.valueOf(com.iit.dp.dp_pos.controller.AuthController.loggedInUserId);

        boolean ok = pausedSaleManager.pauseSale(customerName, customerEmail, itemsToPause, totalAmount, employeeId);
        if (ok) {
            justPausedOrder = true;
            clearCart();
            // Clear name and email fields automatically after pausing (suppress detection)
            withSuppressedDetection(() -> {
                customerNameField.clear();
                customerEmailField.clear();
            });
            showInfo("Order Paused", "The order has been paused. You can resume it later.");
        } else {
            showError("Pause Failed", "Could not pause the current sale. Please check inputs.");
        }
    }

    /**
     * Robust checkout implementation: dynamically builds INSERT statements based on the actual
     * columns present in the `orders` and `order_items` tables so the method works across
     * different schemas. Also creates customer if needed and updates product quantities when possible.
     */
    private void processCheckout() {
        if (cartItems.isEmpty()) {
            showError("Empty Cart", "Please add items to cart before checkout.");
            return;
        }
        String customerName = customerNameField.getText().trim();
        String customerEmail = customerEmailField.getText().trim();
        if (customerName.isEmpty() || customerEmail.isEmpty()) {
            showError("Missing Info", "Please enter customer name and email.");
            return;
        }

        double total = cartItems.stream().mapToDouble(CartEntry::getTotal).sum();
        String orderDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Connection conn = null;
        try {
            conn = DatabaseConnectionManager.getInstance().getConnection();
            DatabaseMetaData md = conn.getMetaData();

            // Ensure orders/customers/order_items tables exist before inserting orders
            ensureOrderTablesExist(conn);

            // Read available columns for orders and order_items and products
            java.util.Set<String> orderCols = new java.util.HashSet<>();
            try (ResultSet rs = md.getColumns(null, null, "orders", null)) {
                while (rs.next()) orderCols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            java.util.Set<String> itemCols = new java.util.HashSet<>();
            try (ResultSet rs = md.getColumns(null, null, "order_items", null)) {
                while (rs.next()) itemCols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            java.util.Set<String> productCols = new java.util.HashSet<>();
            try (ResultSet rs = md.getColumns(null, null, "products", null)) {
                while (rs.next()) productCols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }

            conn.setAutoCommit(false);

            // Ensure customer exists if orders table expects a customer_id
            Integer customerId = null;
            if (orderCols.contains("customer_id")) {
                String findSql = "SELECT id FROM customers WHERE email = ?";
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setString(1, customerEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) customerId = rs.getInt("id");
                    }
                }
                if (customerId == null) {
                    String insCust = "INSERT INTO customers (name, email) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insCust, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, customerName);
                        ps.setString(2, customerEmail);
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) customerId = gk.getInt(1);
                        }
                    }
                }
            }

            // Build order insert dynamically based on available columns
            java.util.List<String> orderInsertCols = new java.util.ArrayList<>();
            java.util.List<Object> orderValues = new java.util.ArrayList<>();

            // Prioritize customer_id if present
            if (orderCols.contains("customer_id") && customerId != null) {
                orderInsertCols.add("customer_id"); orderValues.add(customerId);
            }
            if (orderCols.contains("customer_name")) { orderInsertCols.add("customer_name"); orderValues.add(customerName); }
            if (orderCols.contains("customer_email")) { orderInsertCols.add("customer_email"); orderValues.add(customerEmail); }
            if (orderCols.contains("order_date")) { orderInsertCols.add("order_date"); orderValues.add(orderDate); }
            if (orderCols.contains("date") && !orderInsertCols.contains("order_date")) { orderInsertCols.add("date"); orderValues.add(orderDate); }
            if (orderCols.contains("total_price")) { orderInsertCols.add("total_price"); orderValues.add(total); }
            if (orderCols.contains("status")) { orderInsertCols.add("status"); orderValues.add("completed"); }

            if (orderInsertCols.isEmpty()) {
                throw new SQLException("Orders table does not have any recognized columns for inserting an order.");
            }

            StringBuilder sbCols = new StringBuilder();
            StringBuilder sbParams = new StringBuilder();
            for (int i = 0; i < orderInsertCols.size(); i++) {
                if (i > 0) { sbCols.append(", "); sbParams.append(", "); }
                sbCols.append(orderInsertCols.get(i));
                sbParams.append("?");
            }
            String orderSql = "INSERT INTO orders (" + sbCols + ") VALUES (" + sbParams + ")";

            int orderId = -1;
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < orderValues.size(); i++) {
                    Object v = orderValues.get(i);
                    if (v instanceof Integer) orderStmt.setInt(i + 1, (Integer) v);
                    else if (v instanceof Double) orderStmt.setDouble(i + 1, (Double) v);
                    else orderStmt.setString(i + 1, String.valueOf(v));
                }
                int affected = orderStmt.executeUpdate();
                if (affected == 0) throw new SQLException("Creating order failed, no rows affected.");
                try (ResultSet gk = orderStmt.getGeneratedKeys()) {
                    if (gk.next()) orderId = gk.getInt(1);
                }
            }

            // Fallback to SQLite last_insert_rowid() if generatedKeys not provided
            if (orderId == -1) {
                try (PreparedStatement last = conn.prepareStatement("SELECT last_insert_rowid()")) {
                    try (ResultSet rs = last.executeQuery()) {
                        if (rs.next()) orderId = rs.getInt(1);
                    }
                }
            }
            if (orderId == -1) throw new SQLException("Could not obtain order id after insert.");

            // Prepare order_items insert dynamically
            java.util.List<String> itemColsToInsert = new java.util.ArrayList<>();
            if (itemCols.contains("order_id")) itemColsToInsert.add("order_id");
            if (itemCols.contains("product_id")) itemColsToInsert.add("product_id");
            if (itemCols.contains("product_name")) itemColsToInsert.add("product_name");
            if (itemCols.contains("quantity")) itemColsToInsert.add("quantity");
            if (itemCols.contains("unit_price")) itemColsToInsert.add("unit_price");
            if (itemCols.contains("selling_price") && !itemColsToInsert.contains("unit_price")) itemColsToInsert.add("selling_price");
            // Ensure total_price is inserted when the column exists so totals are not left at default 0
            if (itemCols.contains("total_price") && !itemColsToInsert.contains("total_price")) itemColsToInsert.add("total_price");

            if (itemColsToInsert.isEmpty()) {
                throw new SQLException("order_items table does not have supported columns for inserting items.");
            }

            StringBuilder sbItemCols = new StringBuilder();
            StringBuilder sbItemParams = new StringBuilder();
            for (int i = 0; i < itemColsToInsert.size(); i++) {
                if (i > 0) { sbItemCols.append(", "); sbItemParams.append(", "); }
                sbItemCols.append(itemColsToInsert.get(i));
                sbItemParams.append("?");
            }
            String itemSql = "INSERT INTO order_items (" + sbItemCols + ") VALUES (" + sbItemParams + ")";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (CartEntry entry : cartItems) {
                    int param = 1;
                    for (String col : itemColsToInsert) {
                        switch (col) {
                            case "order_id" -> itemStmt.setInt(param++, orderId);
                            case "product_id" -> itemStmt.setInt(param++, entry.getProductId());
                            case "product_name" -> itemStmt.setString(param++, entry.getName());
                            case "quantity" -> itemStmt.setInt(param++, entry.getQuantity());
                            case "unit_price", "selling_price" -> itemStmt.setDouble(param++, entry.getPrice());
                            case "total_price" -> itemStmt.setDouble(param++, entry.getTotal());
                            default -> itemStmt.setString(param++, "");
                        }
                    }
                    itemStmt.addBatch();

                    // Update products.quantity if present
                    if (productCols.contains("quantity")) {
                        try (PreparedStatement ups = conn.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE id = ?")) {
                            ups.setInt(1, entry.getQuantity());
                            ups.setInt(2, entry.getProductId());
                            ups.executeUpdate();
                        } catch (SQLException ex) {
                            // ignore update failure but log
                            ex.printStackTrace();
                        }
                    }
                }
                itemStmt.executeBatch();
            }

            // If orders table has a total_price column, update it with the computed order total
            double computedOrderTotal = cartItems.stream().mapToDouble(CartEntry::getTotal).sum();
            if (orderCols.contains("total_price")) {
                try (PreparedStatement ups = conn.prepareStatement("UPDATE orders SET total_price = ? WHERE id = ?")) {
                    ups.setDouble(1, computedOrderTotal);
                    ups.setInt(2, orderId);
                    ups.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            conn.commit();
            // On successful order placement, remove any paused sale entries for this customer (by email or name)
            try {
                if (customerEmail != null && !customerEmail.isBlank()) {
                    pausedSaleManager.cancelPausedSale(customerEmail);
                }
                if (customerName != null && !customerName.isBlank()) {
                    pausedSaleManager.cancelPausedSale(customerName);
                }
            } catch (Exception ignored) {}
            showInfo("Order Complete", "Order has been placed successfully.");
            clearCart();
            // Clear name and email fields after successful checkout (suppress detection)
            withSuppressedDetection(() -> {
                customerNameField.clear();
                customerEmailField.clear();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            showError("Database Error", "Could not complete order: " + e.getMessage() + "\nSee console for details.");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    private void ensureOrderTablesExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Customers table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL" +
                    ")");

            // Orders table: include common columns used by code
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customer_id INT, " +
                    "customer_name VARCHAR(100), " +
                    "customer_email VARCHAR(100), " +
                    "order_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "date DATETIME, " +
                    "status VARCHAR(50), " +
                    "total_price REAL" +
                    ")");

            // order_items table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INT NOT NULL, " +
                    "product_id INT, " +
                    "product_name VARCHAR(255), " +
                    "quantity INT NOT NULL, " +
                    "unit_price REAL, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");
        }
    }

    // Helper methods
    private void showError(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartEntry::getTotal).sum();
        if (totalLabel != null) {
            totalLabel.setText(String.format("$%.2f", total));
        }
    }

    public void restorePausedSaleFromMemento(SaleMemento memento) {
        if (memento == null) return;
        try {
            withSuppressedDetection(() -> {
                // Set customer fields
                if (memento.getCustomerName() != null) customerNameField.setText(memento.getCustomerName());
                if (memento.getCustomerEmail() != null) customerEmailField.setText(memento.getCustomerEmail());

                // Restore cart items
                cartItems.clear();
                for (com.iit.dp.dp_pos.model.CartItem o : memento.getCartItems()) {
                    cartItems.add(new CartEntry(o.getProductId(), o.getProductName(), o.getPrice(), o.getQuantity()));
                }
                cartTable.refresh();
                productTable.refresh();
                updateTotal();
            });
        } catch (Exception e) {
            showInfo("Restore Paused Sale", "Could not fully restore paused sale: " + e.getMessage());
        }
    }

    // Add this method to allow restoring a paused order from another controller
    public void restorePausedOrder(com.iit.dp.dp_pos.model.PausedOrder order) {
        if (order == null) return;
        withSuppressedDetection(() -> {
            customerNameField.setText(order.getCustomerName());
            try {
                java.lang.reflect.Method getEmail = order.getClass().getMethod("getCustomerEmail");
                Object email = getEmail.invoke(order);
                if (email != null) customerEmailField.setText(String.valueOf(email));
            } catch (Exception ignored) {}
            cartItems.clear();
            for (com.iit.dp.dp_pos.model.CartItem item : order.getCartItems()) {
                cartItems.add(new CartEntry(item.getProductId(), item.getProductName(), item.getPrice(), item.getQuantity()));
            }
            cartTable.refresh();
            productTable.refresh();
            updateTotal();
        });
    }
}
