package com.iit.dp.dp_pos.util.observer;

import com.iit.dp.dp_pos.model.Product;
import com.iit.dp.dp_pos.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductAlertManager implements ProductSubject {
    private static ProductAlertManager instance;
    private final List<ProductObserver> observers;
    private final List<Product> outOfStockProducts;
    private final List<Product> expiredProducts;

    private ProductAlertManager() {
        this.observers = new ArrayList<>();
        this.outOfStockProducts = new ArrayList<>();
        this.expiredProducts = new ArrayList<>();
    }

    public static synchronized ProductAlertManager getInstance() {
        if (instance == null) {
            instance = new ProductAlertManager();
        }
        return instance;
    }

    @Override
    public void addObserver(ProductObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(ProductObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        // Notify all observers about alerts
        for (ProductObserver observer : observers) {
            // Notify about out of stock products
            for (Product product : outOfStockProducts) {
                observer.onProductOutOfStock(product);
            }

            // Notify about expired products
            for (Product product : expiredProducts) {
                observer.onProductExpired(product);
            }
        }
    }

    public void checkProductAlerts() {
        outOfStockProducts.clear();
        expiredProducts.clear();

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Check for out of stock products
            String outOfStockQuery = "SELECT * FROM products WHERE quantity = 0";
            try (PreparedStatement stmt = conn.prepareStatement(outOfStockQuery);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Product product = createProductFromResultSet(rs);
                    outOfStockProducts.add(product);
                }
            }

            // Check for expired products
            String expiredQuery = "SELECT * FROM products WHERE expiry_date <= ?";
            try (PreparedStatement stmt = conn.prepareStatement(expiredQuery)) {
                stmt.setString(1, LocalDate.now().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Product product = createProductFromResultSet(rs);
                        expiredProducts.add(product);
                    }
                }
            }

            // Notify observers if there are alerts
            if (!outOfStockProducts.isEmpty() || !expiredProducts.isEmpty()) {
                notifyObservers();
            }

        } catch (SQLException e) {
            System.err.println("Error checking product alerts: " + e.getMessage());
        }
    }

    private Product createProductFromResultSet(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getDouble("buying_price"),
            rs.getDouble("selling_price"),
            rs.getInt("quantity"),
            LocalDate.parse(rs.getString("adding_date")),
            LocalDate.parse(rs.getString("expiry_date"))
        );
    }

    public List<Product> getOutOfStockProducts() {
        return new ArrayList<>(outOfStockProducts);
    }

    public List<Product> getExpiredProducts() {
        return new ArrayList<>(expiredProducts);
    }
}
