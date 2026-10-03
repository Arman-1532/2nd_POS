package com.iit.dp.dp_pos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

/**
 * Singleton Database Connection Manager
 * Ensures only one database connection instance exists throughout the application
 */
public class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;
    private static Connection connection;
    private final String url = "jdbc:sqlite:pos.db";
    
    // Private constructor to prevent external instantiation
    private DatabaseConnectionManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }
    
    /**
     * Get the singleton instance of DatabaseConnectionManager
     * Thread-safe implementation using double-checked locking
     */
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get the database connection
     * Creates a new connection if one doesn't exist or if the current connection is closed
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnectionManager.class) {
                if (connection == null || connection.isClosed()) {
                    connection = DriverManager.getConnection(url);
                    System.out.println("New database connection established.");
                }
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize database schema and perform lightweight migrations so runtime code and reports work
     */
    public void initializeSchema() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // PRODUCTS: ensure quantity
            if (!hasColumn(metaData, "products", "quantity")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE products ADD COLUMN quantity INTEGER DEFAULT 0");
                    // seed a sensible default so products are sellable
                    stmt.execute("UPDATE products SET quantity = 100 WHERE quantity IS NULL OR quantity = 0");
                    System.out.println("Added quantity column to products and initialized stock.");
                }
            }
            // PRODUCTS: ensure selling_price, migrate from legacy 'price' if present
            boolean hasSelling = hasColumn(metaData, "products", "selling_price");
            if (!hasSelling) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE products ADD COLUMN selling_price REAL DEFAULT 0");
                    // if legacy 'price' exists, migrate values into selling_price
                    if (hasColumn(metaData, "products", "price")) {
                        stmt.execute("UPDATE products SET selling_price = price WHERE selling_price = 0");
                    }
                    System.out.println("Ensured selling_price column on products and migrated from price if available.");
                }
            }
            // PRODUCTS: ensure optional buying_price for profit calc (fallback to 0 if absent)
            if (!hasColumn(metaData, "products", "buying_price")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE products ADD COLUMN buying_price REAL DEFAULT 0");
                    System.out.println("Added buying_price column to products (default 0).");
                }
            }
            // PRODUCTS: ensure category (already added previously but keep idempotent)
            if (!hasColumn(metaData, "products", "category")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE products ADD COLUMN category TEXT DEFAULT 'General'");
                    System.out.println("Added category column to products table.");
                }
            }

            // ORDERS: ensure date
            if (!hasColumn(metaData, "orders", "date")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN date TEXT DEFAULT (datetime('now'))");
                    System.out.println("Added date column to orders table.");
                }
            }
            // Backfill missing order dates
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE orders SET date = datetime('now') WHERE date IS NULL OR date = ''");
            }
            // ORDERS: ensure status
            if (!hasColumn(metaData, "orders", "status")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN status VARCHAR(50) DEFAULT 'COMPLETED'");
                    System.out.println("Added status column to orders table.");
                }
            }
            // Backfill missing status
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE orders SET status = 'COMPLETED' WHERE status IS NULL OR status = ''");
            }

            // ORDER_ITEMS: ensure unit_price
            if (!hasColumn(metaData, "order_items", "unit_price")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE order_items ADD COLUMN unit_price REAL DEFAULT 0");
                    System.out.println("Added unit_price column to order_items table.");
                }
            }
            // ORDER_ITEMS: ensure total_price
            if (!hasColumn(metaData, "order_items", "total_price")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE order_items ADD COLUMN total_price REAL DEFAULT 0");
                    System.out.println("Added total_price column to order_items table.");
                }
            }
            // Backfill unit_price using products.selling_price if missing
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "UPDATE order_items SET unit_price = (SELECT selling_price FROM products p WHERE p.id = order_items.product_id) " +
                    "WHERE (unit_price IS NULL OR unit_price = 0) "
                );
                stmt.executeUpdate(
                    "UPDATE order_items SET total_price = (unit_price * quantity) WHERE (total_price IS NULL OR total_price = 0)"
                );
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }

    private boolean hasColumn(DatabaseMetaData metaData, String table, String column) {
        try (ResultSet columns = metaData.getColumns(null, null, table, column)) {
            return columns.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Check if the connection is valid and open
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Get connection status information
     */
    public String getConnectionStatus() {
        try {
            if (connection == null) {
                return "No connection established";
            } else if (connection.isClosed()) {
                return "Connection is closed";
            } else if (connection.isValid(5)) {
                return "Connection is active and valid";
            } else {
                return "Connection is invalid";
            }
        } catch (SQLException e) {
            return "Error checking connection status: " + e.getMessage();
        }
    }
    
    /**
     * Reconnect to the database
     */
    public void reconnect() throws SQLException {
        closeConnection();
        getConnection();
    }

    /**
     * Utility: Ensure at least one admin user exists for login
     */
    public void ensureDefaultAdminUser() {
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM users WHERE type='admin'";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(checkSql);
                if (rs.next() && rs.getInt(1) == 0) {
                    // No admin user exists, insert one
                    String email = "admin@admin.com";
                    String password = org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt());
                    String insertSql = "INSERT INTO users (email, password, type) VALUES (?, ?, 'admin')";
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setString(1, email);
                        ps.setString(2, password);
                        ps.executeUpdate();
                        System.out.println("Default admin user created: " + email + " / admin123");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to ensure default admin user: " + e.getMessage());
        }
    }
}
