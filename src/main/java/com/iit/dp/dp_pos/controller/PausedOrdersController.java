package com.iit.dp.dp_pos.controller;

import com.iit.dp.dp_pos.util.memento.PausedSaleManager;
import com.iit.dp.dp_pos.util.memento.SaleMemento;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.util.Map;

public class PausedOrdersController {
    private MainController mainController;

    @FXML private TableView<PausedOrderEntry> pausedOrdersTable;
    @FXML private Button backButton;

    private final PausedSaleManager pausedSaleManager = PausedSaleManager.getInstance();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public static class PausedOrderEntry {
        private final SimpleStringProperty customerKey;
        private final SimpleStringProperty customerName;
        private final SimpleIntegerProperty itemCount;
        private final SimpleDoubleProperty totalAmount;

        public PausedOrderEntry(String customerKey, String customerName, int itemCount, double totalAmount) {
            this.customerKey = new SimpleStringProperty(customerKey);
            this.customerName = new SimpleStringProperty(customerName);
            this.itemCount = new SimpleIntegerProperty(itemCount);
            this.totalAmount = new SimpleDoubleProperty(totalAmount);
        }

        public String getCustomerKey() { return customerKey.get(); }
        public String getCustomerName() { return customerName.get(); }
        public int getItemCount() { return itemCount.get(); }
        public double getTotalAmount() { return totalAmount.get(); }
    }

    @FXML
    private void initialize() {
        setupTable();
        loadPausedOrders();

        // Allow FXML back button handler and keep programmatic as fallback
        if (backButton != null) {
            backButton.setOnAction(event -> navigateBack());
        }
    }

    // Matches FXML onAction="#goBack"
    @FXML
    private void goBack() {
        navigateBack();
    }

    @FXML
    private void onBackClick(ActionEvent event) { // legacy hook
        navigateBack();
    }

    private void navigateBack() {
        if (com.iit.dp.dp_pos.util.UserSession.isAdmin()) {
            mainController.loadView("hello-view.fxml");
        } else {
            mainController.loadView("employee-dashboard-view.fxml");
        }
    }

    private void setupTable() {
        pausedOrdersTable.getColumns().clear();
        // Customer Name Column
        TableColumn<PausedOrderEntry, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(data -> data.getValue().customerName);
        nameCol.setPrefWidth(200);

        // Item Count Column
        TableColumn<PausedOrderEntry, Number> itemCountCol = new TableColumn<>("Items");
        itemCountCol.setCellValueFactory(data -> data.getValue().itemCount);
        itemCountCol.setPrefWidth(100);

        // Total Amount Column
        TableColumn<PausedOrderEntry, Number> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(data -> data.getValue().totalAmount);
        totalCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) setText(null);
                else setText(String.format("$%.2f", price.doubleValue()));
            }
        });
        totalCol.setPrefWidth(150);

        // Actions Column
        TableColumn<PausedOrderEntry, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(tc -> new TableCell<>() {
            private final Button resumeButton = new Button("Resume");
            {
                resumeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                resumeButton.setOnAction(e -> {
                    PausedOrderEntry entry = getTableView().getItems().get(getIndex());
                    resumeOrder(entry.getCustomerKey());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : resumeButton);
            }
        });
        actionsCol.setPrefWidth(100);

        pausedOrdersTable.getColumns().addAll(nameCol, itemCountCol, totalCol, actionsCol);
    }

    /** shob paused order load krbe */
    private void loadPausedOrders() {
        ObservableList<PausedOrderEntry> entries = FXCollections.observableArrayList();
        for (Map.Entry<String, SaleMemento> e : pausedSaleManager.getAllPausedSales().entrySet()) {
            SaleMemento m = e.getValue();
            String name = m.getCustomerName() != null ? m.getCustomerName() : e.getKey();
            int items = m.getCartItems() != null ? m.getCartItems().size() : 0;
            double total = m.getTotalAmount();
            entries.add(new PausedOrderEntry(e.getKey(), name, items, total));
        }
        pausedOrdersTable.setItems(entries);
    }

    private void resumeOrder(String customerKey) {
        // Use manager to resume (also removes it from the paused list)
        SaleMemento m = pausedSaleManager.resumeSale(customerKey);
        if (m == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Paused sale not found or already resumed.");
            alert.showAndWait();
            return;
        }
        // Load Create Sale and inject memento
        mainController.loadView("create-sale-view.fxml", controller -> {
            if (controller instanceof CreateSaleController csc) {
                csc.restorePausedSaleFromMemento(m);
            }
        });
    }
}
