package com.iit.dp.dp_pos.util.memento;

import com.iit.dp.dp_pos.model.CartItem;
import java.util.List;
import java.util.Map;

/**
 * Facade class that simplifies interaction with the Memento pattern
 * Provides a clean interface for pausing and resuming sales
 */
public class PausedSaleManager {
    private static PausedSaleManager instance;
    private final SaleCaretaker caretaker;
    private final SaleOriginator originator;

    private PausedSaleManager() {
        this.caretaker = SaleCaretaker.getInstance();
        this.originator = new SaleOriginator();
    }

    /**
     * Get singleton instance
     */
    public static synchronized PausedSaleManager getInstance() {
        if (instance == null) {
            instance = new PausedSaleManager();
        }
        return instance;
    }

    /**
     * Pause a sale transaction
     */
    public boolean pauseSale(String customerName, String customerEmail,
                           List<CartItem> cartItems, double totalAmount, String employeeId) {

        if (customerName == null || customerName.trim().isEmpty()) {
            System.err.println("Cannot pause sale: Customer name is required");
            return false;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            System.err.println("Cannot pause sale: Cart is empty");
            return false;
        }

        try {
            // Set the sale state in originator
            originator.setSaleState(customerName, customerEmail, cartItems, totalAmount, employeeId);

            // Create memento
            SaleMemento memento = originator.createMemento();

            // Save memento using customer email as key (fallback to name if email is null)
            String customerKey = (customerEmail != null && !customerEmail.trim().isEmpty())
                               ? customerEmail : customerName;

            caretaker.saveSaleMemento(customerKey, memento);

            System.out.println("Sale paused successfully for: " + customerKey);
            return true;

        } catch (Exception e) {
            System.err.println("Error pausing sale: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resume a paused sale
     */
    // resume kore
    public SaleMemento resumeSale(String customerKey) {
        SaleMemento memento = caretaker.getSaleMemento(customerKey);

        if (memento != null) {
            // Restore state in originator
            originator.restoreFromMemento(memento);

            // Remove from paused sales since it's being resumed
            caretaker.removePausedSale(customerKey);

            System.out.println("Sale resumed for: " + customerKey);
            return memento;
        }

        System.out.println("No paused sale found for: " + customerKey);
        return null;
    }

    /**
     * Check if customer has a paused sale
     */
    public boolean hasPausedSale(String customerKey) {
        return caretaker.hasPausedSale(customerKey);
    }

    /**
     * Get all paused sales
     */
    public Map<String, SaleMemento> getAllPausedSales() {
        return caretaker.getAllPausedSales();
    }

    /**
     * Cancel a paused sale
     */
    public boolean cancelPausedSale(String customerKey) {
        if (caretaker.hasPausedSale(customerKey)) {
            caretaker.removePausedSale(customerKey);
            System.out.println("Paused sale cancelled for: " + customerKey);
            return true;
        }
        return false;
    }

    /**
     * Get sale history for a customer
     */
    public List<SaleMemento> getSaleHistory(String customerKey) {
        return caretaker.getSaleHistory(customerKey);
    }

    /**
     * Clean up old paused sales (older than 24 hours by default)
     */
    public void cleanupOldPausedSales() {
        cleanupOldPausedSales(24);
    }

    /**
     * Clean up old paused sales
     */
    public void cleanupOldPausedSales(int hoursOld) {
        caretaker.clearOldPausedSales(hoursOld);
    }

    /**
     * Get statistics about paused sales
     */
    public Map<String, Object> getStatistics() {
        return caretaker.getPausedSalesStatistics();
    }

    /**
     * Clear all paused sales (for maintenance)
     */
    public void clearAll() {
        caretaker.clearAllPausedSales();
    }

    /**
     * Try to find paused sale by customer name or email
     */
    // khuje
    public SaleMemento findPausedSale(String customerName, String customerEmail) {
        // First try email
        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            SaleMemento memento = caretaker.getSaleMemento(customerEmail);
            if (memento != null) {
                return memento;
            }
        }

        // Then try name
        if (customerName != null && !customerName.trim().isEmpty()) {
            return caretaker.getSaleMemento(customerName);
        }

        return null;
    }
}
