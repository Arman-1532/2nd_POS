package com.iit.dp.dp_pos.model;

import java.time.LocalDate;

public class Product {
    private int id;
    private String name;
    private String category;
    private double buyingPrice;
    private double sellingPrice;
    private int quantity;
    private LocalDate addingDate;
    private LocalDate expiryDate;

    // Constructor for existing products (from database)
    public Product(int id, String name, String category, double buyingPrice, double sellingPrice, int quantity, LocalDate addingDate, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.addingDate = addingDate;
        this.expiryDate = expiryDate;
    }

    // Constructor for new products (auto-generated ID)
    public Product(String name, String category, double buyingPrice, double sellingPrice, int quantity, LocalDate addingDate, LocalDate expiryDate) {
        this.name = name;
        this.category = category;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.addingDate = addingDate;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    // Keep the old price getter for backward compatibility (returns selling price)
    public double getPrice() {
        return sellingPrice;
    }

    public void setPrice(double price) {
        this.sellingPrice = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getAddingDate() {
        return addingDate;
    }

    public void setAddingDate(LocalDate addingDate) {
        this.addingDate = addingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', category='%s', buyingPrice=%.2f, sellingPrice=%.2f, quantity=%d, addingDate=%s, expiryDate=%s}",
                id, name, category, buyingPrice, sellingPrice, quantity, addingDate, expiryDate);
    }
}
