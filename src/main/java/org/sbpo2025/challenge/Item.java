package org.sbpo2025.challenge;

import java.util.*;

public class Item {
    public int id;
    public Map<Integer, Integer> orders;
    public Map<Integer, Integer> aisles;

    public Item(int id, Map<Integer, Integer> orders, Map<Integer, Integer> aisles) {
        this.id = id;
        this.orders = orders;
        this.aisles = aisles;
    }

    public void addOrder(int orderId, int quantity) {
        this.orders.put(orderId, quantity);
    }

    public void addAisle(int aisleId, int capacity) {
        this.aisles.put(aisleId, capacity);
    }

    // public float averageDemand() {
    //     return (float) this.orders.values().stream().mapToInt(Integer::intValue).average().orElse(0);
    // }

    // public float averageCapacity() {
    //     return (float) this.aisles.values().stream().mapToInt(Integer::intValue).average().orElse(0);
    // }
}
