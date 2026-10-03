package com.iit.dp.dp_pos.util.memento;

import com.iit.dp.dp_pos.model.CartItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Memento class that stores the complete state of a sale transaction
 * This is immutable to preserve the integrity of the saved state
 */
public class SaleMemento {
    private final String customerName;
    private final String customerEmail;
    private final List<CartItem> cartItems;
    private final double totalAmount;
    private final LocalDateTime pausedAt;
    private final String sessionId;
    private final String employeeId;

    public SaleMemento(String customerName, String customerEmail, List<CartItem> cartItems,
                      double totalAmount, LocalDateTime pausedAt, String sessionId, String employeeId) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        // Create defensive copy to ensure immutability
        this.cartItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            this.cartItems.add(new CartItem(item.getProductId(), item.getProductName(),
                                          item.getPrice(), item.getQuantity()));
        }
        this.totalAmount = totalAmount;
        this.pausedAt = pausedAt;
        this.sessionId = sessionId;
        this.employeeId = employeeId;
    }

    // Getters only - no setters to maintain immutability
    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<CartItem> getCartItems() {
        // Return defensive copy to prevent external modification
        List<CartItem> copy = new ArrayList<>();
        for (CartItem item : cartItems) {
            copy.add(new CartItem(item.getProductId(), item.getProductName(),
                                item.getPrice(), item.getQuantity()));
        }
        return copy;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    @Override
    public String toString() {
        return String.format("SaleMemento{customer='%s', items=%d, total=%.2f, pausedAt=%s}",
                           customerName, cartItems.size(), totalAmount, pausedAt);
    }
}
