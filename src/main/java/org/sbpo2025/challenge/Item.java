package org.sbpo2025.challenge;

import java.util.*;

public class Item {
    public int id;
    public Map<Integer, Integer> orders;
    public Map<Integer, Integer> aisles;
    public int stock;

    public Item(int id, Map<Integer, Integer> orders, Map<Integer, Integer> aisles) {
        this.id = id;
        this.orders = orders;
        this.aisles = aisles;
        this.stock = 0;
    }

    public void addOrder(int orderId, int quantity) {
        this.orders.put(orderId, quantity);
    }

    public void addAisle(int aisleId, int capacity) {
        this.aisles.put(aisleId, capacity);
    }

    public void addStock(int stock) {
        this.stock += stock;
    }

    public void removeStock(int stock) {
        this.stock -= stock;
    }

    public void resetStock() {
        this.stock = 0;
    }

}
