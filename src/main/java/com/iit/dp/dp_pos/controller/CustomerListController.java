package com.iit.dp.dp_pos.controller;

import com.iit.dp.dp_pos.util.DatabaseConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class CustomerListController {
    private MainController mainController;
    
    @FXML private TableView<CustomerItem> customerTable;
    @FXML private TableColumn<CustomerItem, String> nameColumn;
    @FXML private TableColumn<CustomerItem, String> emailColumn;
    @FXML private TextField searchField;
    @FXML private Label totalCustomersLabel;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        allCustomers = getCustomerList();
        customerTable.setItems(allCustomers);
        updateTotalCustomersLabel(allCustomers.size());
        setupSearch();
    }

    private ObservableList<CustomerItem> allCustomers;

    private ObservableList<CustomerItem> getCustomerList() {
        ObservableList<CustomerItem> customers = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT c.name, c.email FROM customers c JOIN orders o ON c.id = o.customer_id";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                customers.add(new CustomerItem(
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customers;
    }

    private void updateTotalCustomersLabel(int count) {
        if (totalCustomersLabel != null) {
            totalCustomersLabel.setText(String.valueOf(count));
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String search = newValue.toLowerCase();
            if (search.isEmpty()) {
                customerTable.setItems(allCustomers);
                updateTotalCustomersLabel(allCustomers.size());
            } else {
                ObservableList<CustomerItem> filtered = FXCollections.observableArrayList();
                for (CustomerItem c : allCustomers) {
                    if (c.getName().toLowerCase().contains(search) || c.getEmail().toLowerCase().contains(search)) {
                        filtered.add(c);
                    }
                }
                customerTable.setItems(filtered);
                updateTotalCustomersLabel(filtered.size());
            }
        });
    }

    public static class CustomerItem {
        private final String name;
        private final String email;
        public CustomerItem(String name, String email) {
            this.name = name;
            this.email = email;
        }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    @FXML
    private void onBackClick(javafx.event.ActionEvent event) {
        // Navigate back to appropriate dashboard based on user type
        if (com.iit.dp.dp_pos.util.UserSession.isAdmin()) {
            mainController.loadView("hello-view.fxml");
        } else {
            mainController.loadView("employee-dashboard-view.fxml");
        }
    }
}
