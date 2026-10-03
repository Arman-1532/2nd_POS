package com.iit.dp.dp_pos.util.observer;

public interface ProductSubject {
    void addObserver(ProductObserver observer);
    void removeObserver(ProductObserver observer);
    void notifyObservers();
}
