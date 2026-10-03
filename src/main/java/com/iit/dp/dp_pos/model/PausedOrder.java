package com.iit.dp.dp_pos.model;

import java.util.List;

public class PausedOrder {
    private String customerName;
    private List<CartItem> cartItems;

    public PausedOrder(String customerName, List<CartItem> cartItems) {
        this.customerName = customerName;
        this.cartItems = cartItems;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }
}

