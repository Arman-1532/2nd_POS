package com.iit.dp.dp_pos.util.memento;

import com.iit.dp.dp_pos.model.CartItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Originator class that manages the sale state and creates/restores mementos
 * This represents the current sale transaction state
 */
public class SaleOriginator {
    private String customerName;
    private String customerEmail;
    private List<CartItem> cartItems;
    private double totalAmount;
    private String sessionId;
    private String employeeId;

    public SaleOriginator() {
        this.sessionId = UUID.randomUUID().toString();
    }

    /**
     * Set the current sale state
     */
    public void setSaleState(String customerName, String customerEmail,
                           List<CartItem> cartItems, double totalAmount, String employeeId) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.cartItems = cartItems;
        this.totalAmount = totalAmount;
        this.employeeId = employeeId;
    }

    /**
     * Create a memento of the current sale state
     */
    public SaleMemento createMemento() {
        return new SaleMemento(customerName, customerEmail, cartItems,
                             totalAmount, LocalDateTime.now(), sessionId, employeeId);
    }

    /**
     * Restore the sale state from a memento
     */
    public void restoreFromMemento(SaleMemento memento) {
        this.customerName = memento.getCustomerName();
        this.customerEmail = memento.getCustomerEmail();
        this.cartItems = memento.getCartItems();
        this.totalAmount = memento.getTotalAmount();
        this.employeeId = memento.getEmployeeId();
        this.sessionId = memento.getSessionId();
    }

    /**
     * Check if the originator has valid sale data
     */
//    public boolean hasValidSaleData() {
//        return customerName != null && !customerName.trim().isEmpty() &&
//               cartItems != null && !cartItems.isEmpty();
//    }
//
//    /**
//     * Clear the current sale state
//     */
//    public void clearSaleState() {
//        this.customerName = null;
//        this.customerEmail = null;
//        this.cartItems = null;
//        this.totalAmount = 0.0;
//        this.sessionId = UUID.randomUUID().toString();
//    }

    // Getters for current state
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public List<CartItem> getCartItems() { return cartItems; }
    public double getTotalAmount() { return totalAmount; }
    public String getSessionId() { return sessionId; }
    public String getEmployeeId() { return employeeId; }
}
