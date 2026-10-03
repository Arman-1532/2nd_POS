package com.iit.dp.dp_pos.controller;

import com.iit.dp.dp_pos.report.*;
import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

public class OrderHistoryController {
    private MainController mainController;

    @FXML private TableView<SalesReportRow> orderTable;
    @FXML private TableColumn<SalesReportRow, String> dateColumn;
    @FXML private TableColumn<SalesReportRow, String> customerNameColumn;
    @FXML private TableColumn<SalesReportRow, String> customerEmailColumn;
    @FXML private TableColumn<SalesReportRow, String> productNameColumn;
    @FXML private TableColumn<SalesReportRow, Integer> quantityColumn;
    @FXML private TableColumn<SalesReportRow, Double> priceColumn;
    @FXML private TableColumn<SalesReportRow, Double> totalColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportTypeComboBox;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Ensure DB connection is available
        try {
            DatabaseConnectionManager.getInstance().getConnection();
        } catch (SQLException e) {
            showAlert("Database connection error: " + e.getMessage());
            return;
        }

        // Setup table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "$" + String.format("%.2f", item));
            }
        });
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "$" + String.format("%.2f", item));
            }
        });

        // Show all sales records by default (no date filter)
        loadAllSales();
        // Set date pickers empty by default
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        // Initialize ComboBox items WITHOUT Visual option
        if (reportTypeComboBox != null) {
            ObservableList<String> reportTypes = FXCollections.observableArrayList("CSV", "PDF");
            reportTypeComboBox.setItems(reportTypes);
            reportTypeComboBox.getSelectionModel().select(0); // Default to CSV
        }
    }

    private void loadAllSales() {
        try {
            TableSalesReportGenerator generator = new TableSalesReportGenerator();
            List<SalesReportRow> rows = generator.generateReport(null, null); // null means no date filter
            orderTable.setItems(FXCollections.observableArrayList(rows));
            orderTable.refresh(); // Force UI refresh
        } catch (Exception e) {
            showAlert("Error loading sales: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onGenerateReportClick() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String type = reportTypeComboBox.getValue();

        if (type == null || type.isEmpty()) {
            showAlert("Please select a report type (CSV or PDF).");
            return;
        }
        if ((start != null && end != null) && end.isBefore(start)) {
            showAlert("End date cannot be before start date.");
            return;
        }

        // Always produce a visual report, with the selected type shown in the headline
        VisualReportGenerator generator = new VisualReportGenerator();
        generator.setReportFormat(type);
        List<SalesReportRow> rows = generator.generateReport(start, end);
        boolean success = generator.export(rows, start, end);
        if (!success) {
            showAlert("Failed to generate " + type + " report.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order History");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void onBackClick(javafx.event.ActionEvent event) {
        // Navigate back to appropriate dashboard based on user type
        if (mainController != null) {
            // Use AuthController's static userType to determine which dashboard to return to
            if ("admin".equalsIgnoreCase(com.iit.dp.dp_pos.controller.AuthController.userType)) {
                mainController.loadView("hello-view.fxml");
            } else {
                mainController.loadView("employee-dashboard-view.fxml");
            }
        }
    }
}
