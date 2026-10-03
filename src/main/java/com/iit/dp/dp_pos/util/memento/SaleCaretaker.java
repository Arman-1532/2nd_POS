package com.iit.dp.dp_pos.util.memento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caretaker class that manages multiple sale mementos
 * Provides functionality to save, retrieve, and manage paused sales
 */
public class SaleCaretaker {
    private static SaleCaretaker instance;
    private final Map<String, SaleMemento> pausedSales;
    private final Map<String, List<SaleMemento>> saleHistory;
    private final int maxHistoryPerCustomer = 5;

    private SaleCaretaker() {
        this.pausedSales = new HashMap<>();
        this.saleHistory = new HashMap<>();
    }

    /**
     * Get singleton instance of SaleCaretaker
     */
    public static synchronized SaleCaretaker getInstance() {
        if (instance == null) {
            instance = new SaleCaretaker();
        }
        return instance;
    }

    /**
     * Save a sale memento for a customer
     */
    public void saveSaleMemento(String customerKey, SaleMemento memento) {
        // Save current paused sale
        pausedSales.put(customerKey, memento);

        // Also add to history
        saleHistory.computeIfAbsent(customerKey, k -> new ArrayList<>()).add(memento);

        // Maintain history limit
        List<SaleMemento> history = saleHistory.get(customerKey);
        if (history.size() > maxHistoryPerCustomer) {
            history.remove(0); // Remove oldest
        }

        System.out.println("Sale memento saved for customer: " + customerKey);
    }

    /**
     * Retrieve a paused sale memento by customer key (email or name)
     */
    public SaleMemento getSaleMemento(String customerKey) {
        return pausedSales.get(customerKey);
    }

    /**
     * Check if a customer has a paused sale
     */
    public boolean hasPausedSale(String customerKey) {
        return pausedSales.containsKey(customerKey);
    }

    /**
     * Remove a paused sale (when resumed or cancelled)
     */
    public void removePausedSale(String customerKey) {
        SaleMemento removed = pausedSales.remove(customerKey);
        if (removed != null) {
            System.out.println("Paused sale removed for customer: " + customerKey);
        }
    }

    /**
     * Get all paused sales
     */
    public Map<String, SaleMemento> getAllPausedSales() {
        return new HashMap<>(pausedSales);
    }

    /**
     * Get sale history for a customer
     */
    public List<SaleMemento> getSaleHistory(String customerKey) {
        return new ArrayList<>(saleHistory.getOrDefault(customerKey, new ArrayList<>()));
    }

    /**
     * Get all customers with paused sales
     */
    public List<String> getCustomersWithPausedSales() {
        return new ArrayList<>(pausedSales.keySet());
    }

    /**
     * Clear old paused sales (older than specified hours)
     */
    public void clearOldPausedSales(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);

        List<String> toRemove = pausedSales.entrySet().stream()
            .filter(entry -> entry.getValue().getPausedAt().isBefore(cutoffTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (String customerKey : toRemove) {
            pausedSales.remove(customerKey);
            System.out.println("Removed old paused sale for: " + customerKey);
        }

        if (!toRemove.isEmpty()) {
            System.out.println("Cleared " + toRemove.size() + " old paused sales");
        }
    }

    /**
     * Get statistics about paused sales
     */
    public Map<String, Object> getPausedSalesStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPausedSales", pausedSales.size());
        stats.put("totalCustomersWithHistory", saleHistory.size());

        double totalValue = pausedSales.values().stream()
            .mapToDouble(SaleMemento::getTotalAmount)
            .sum();
        stats.put("totalPausedValue", totalValue);

        return stats;
    }

    /**
     * Clear all paused sales (for testing or maintenance)
     */
    public void clearAllPausedSales() {
        int count = pausedSales.size();
        pausedSales.clear();
        System.out.println("Cleared all " + count + " paused sales");
    }
}
