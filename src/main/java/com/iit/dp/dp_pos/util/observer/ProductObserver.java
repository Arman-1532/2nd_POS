package com.iit.dp.dp_pos.util.observer;

import com.iit.dp.dp_pos.model.Product;

public interface ProductObserver {
    void onProductOutOfStock(Product product);
    void onProductExpired(Product product);
}
