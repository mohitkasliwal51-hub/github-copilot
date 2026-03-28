package com.mohit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// inventory manager class that uses a map and adds/removes products
public class InventryMannager {
    private final Map<String, List<Product>> inventory;

    public InventryMannager() {
        this.inventory = new HashMap<>();
    }

    public void addProduct(String category, Product product) {
        validateCategory(category);
        Objects.requireNonNull(product, "product must not be null");

        inventory.computeIfAbsent(category, k -> new ArrayList<>()).add(product);
    }

    public boolean removeProduct(String category, int productId) {
        validateCategory(category);

        List<Product> products = inventory.get(category);
        if (products == null) {
            return false;
        }

        boolean removed = products.removeIf(p -> p.getId() == productId);
        if (products.isEmpty()) {
            inventory.remove(category);
        }
        return removed;
    }

    public boolean updateProductQuantity(String category, int productId, int newQuantity) {
        validateCategory(category);
        if (newQuantity < 0) {
            throw new IllegalArgumentException("newQuantity must be >= 0");
        }

        List<Product> products = inventory.get(category);
        if (products == null) {
            return false;
        }

        for (Product product : products) {
            if (product.getId() == productId) {
                product.setQuantity(newQuantity);
                return true;
            }
        }
        return false;
    }

    public List<Product> getProductsByCategory(String category) {
        validateCategory(category);
        List<Product> products = inventory.getOrDefault(category, Collections.emptyList());
        return Collections.unmodifiableList(new ArrayList<>(products));
    }

    public Map<String, List<Product>> getInventorySnapshot() {
        Map<String, List<Product>> copy = new HashMap<>();
        for (Map.Entry<String, List<Product>> entry : inventory.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be null or blank");
        }
    }

    // main method with user input
    public static void main(String[] args) {
        InventryMannager manager = new InventryMannager();
        manager.addProduct("Electronics", new Product(1, "Laptop", 10));
        manager.addProduct("Electronics", new Product(2, "Smartphone", 20));
        manager.addProduct("Groceries", new Product(3, "Apple", 100));

        System.out.println("Inventory Snapshot:");
        for (Map.Entry<String, List<Product>> entry : manager.getInventorySnapshot().entrySet()) {
            System.out.println("Category: " + entry.getKey());
            for (Product product : entry.getValue()) {
                System.out.println("  " + product);
            }
        }
    }
}
