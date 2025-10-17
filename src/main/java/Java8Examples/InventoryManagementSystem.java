package Java8Examples;

import java.util.*;
import java.util.stream.*;
import java.time.*;

public class InventoryManagementSystem {

    static class Product {
        private String productId;
        private String name;
        private String category;
        private double price;
        private int quantity;
        private double rating;
        private LocalDate expiryDate;

        public Product(String productId, String name, String category, double price,
                       int quantity, double rating, LocalDate expiryDate) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.rating = rating;
            this.expiryDate = expiryDate;
        }

        public String getProductId() { return productId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getRating() { return rating; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public double getTotalValue() { return price * quantity; }

        public boolean isExpired() {
            return expiryDate.isBefore(LocalDate.now());
        }

        public boolean isLowStock() {
            return quantity < 10;
        }

        @Override
        public String toString() {
            return String.format("Product[ID:%s, Name:%s, Category:%s, Price:%.2f, Qty:%d, Rating:%.1f]",
                    productId, name, category, price, quantity, rating);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== INVENTORY MANAGEMENT SYSTEM - Java 8 Collectors & Parallel Streams ===\n");

        List<Product> products = createSampleProducts();

        // 1. Advanced Collectors - groupingBy, partitioningBy
        System.out.println("--- 1. Advanced Collectors ---");

        Map<String, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory));

        productsByCategory.forEach((category, productList) -> {
            System.out.printf("\n%s Category (%d products):\n", category, productList.size());
            productList.forEach(p -> System.out.println("  • " + p.getName()));
        });

        // Partition by low stock
        Map<Boolean, List<Product>> partitionedByStock = products.stream()
                .collect(Collectors.partitioningBy(Product::isLowStock));

        System.out.println("\n--- Low Stock Products ---");
        partitionedByStock.get(true).forEach(p ->
                System.out.printf("  ⚠ %s - Only %d left\n", p.getName(), p.getQuantity())
        );

        // 2. Complex Collectors - summarizing, joining
        System.out.println("\n--- 2. Complex Collectors ---");

        DoubleSummaryStatistics priceStats = products.stream()
                .collect(Collectors.summarizingDouble(Product::getPrice));
        System.out.printf("Price Statistics: Min=%.2f, Max=%.2f, Avg=%.2f\n",
                priceStats.getMin(), priceStats.getMax(), priceStats.getAverage());

        String allProductNames = products.stream()
                .map(Product::getName)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("All Products: " + allProductNames);

        // 3. Parallel Streams for large data processing
        System.out.println("\n--- 3. Parallel Streams ---");

        long startTime = System.currentTimeMillis();
        double sequentialTotal = products.stream()
                .mapToDouble(Product::getTotalValue)
                .sum();
        long sequentialTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        double parallelTotal = products.parallelStream()
                .mapToDouble(Product::getTotalValue)
                .sum();
        long parallelTime = System.currentTimeMillis() - startTime;

        System.out.printf("Sequential Total: $%.2f (Time: %d ms)\n", sequentialTotal, sequentialTime);
        System.out.printf("Parallel Total: $%.2f (Time: %d ms)\n", parallelTotal, parallelTime);
        if (parallelTime > 0) {
            System.out.printf("Parallel speedup: %.2fx\n", (double) sequentialTime / parallelTime);
        }

        // 4. Traditional approach instead of ConcurrentHashMap with computeIfAbsent
        System.out.println("\n--- 4. Category Count ---");
        Map<String, Integer> categoryCount = new HashMap<>();

        products.forEach(product -> {
            String category = product.getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        });

        System.out.println("Products per category:");
        categoryCount.forEach((category, count) ->
                System.out.printf("  %s: %d products\n", category, count)
        );

        // 5. Custom Collector for complex aggregation
        System.out.println("\n--- 5. Custom Aggregation ---");

        Map<String, Double> categoryAvgRating = products.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.averagingDouble(Product::getRating)
                ));

        System.out.println("Average Rating by Category:");
        categoryAvgRating.forEach((category, avgRating) ->
                System.out.printf("  %s: %.1f/5.0\n", category, avgRating)
        );

        // 6. Filtering and sorting with comparators
        System.out.println("\n--- 6. Advanced Filtering & Sorting ---");

        List<Product> topRatedProducts = products.stream()
                .filter(p -> p.getRating() >= 4.0)
                .sorted(Comparator.comparingDouble(Product::getRating).reversed()
                        .thenComparing(Product::getPrice))
                .collect(Collectors.toList());

        System.out.println("Top Rated Products (Rating >= 4.0):");
        topRatedProducts.forEach(p ->
                System.out.printf("  ★ %.1f - %s ($%.2f)\n", p.getRating(), p.getName(), p.getPrice())
        );

        // Output for next class
        String inventorySummary = String.format(
                "INVENTORY_SUMMARY:TOTAL_PRODUCTS_%d,TOTAL_VALUE_%.2f,CATEGORIES_%d",
                products.size(), parallelTotal, productsByCategory.size()
        );
        System.out.println("\n" + inventorySummary);
    }

    private static List<Product> createSampleProducts() {
        return Arrays.asList(
                new Product("P001", "Laptop", "Electronics", 899.99, 15, 4.5, LocalDate.of(2026, 12, 31)),
                new Product("P002", "Smartphone", "Electronics", 699.99, 8, 4.3, LocalDate.of(2026, 12, 31)),
                new Product("P003", "Desk Chair", "Furniture", 199.99, 25, 4.2, LocalDate.of(2028, 6, 30)),
                new Product("P004", "Coffee Maker", "Appliances", 89.99, 12, 4.0, LocalDate.of(2025, 9, 15)),
                new Product("P005", "Notebook", "Stationery", 4.99, 150, 4.1, LocalDate.of(2025, 3, 31))
        );
    }
}

// Output

/*
=== INVENTORY MANAGEMENT SYSTEM - Java 8 Collectors & Parallel Streams ===

        --- 1. Advanced Collectors ---

Appliances Category (1 products):
        • Coffee Maker

Electronics Category (2 products):
        • Laptop
  • Smartphone

Stationery Category (1 products):
        • Notebook

Furniture Category (1 products):
        • Desk Chair

--- Low Stock Products ---
        ⚠ Smartphone - Only 8 left

--- 2. Complex Collectors ---
Price Statistics: Min=4.99, Max=899.99, Avg=378.99
All Products: [Laptop, Smartphone, Desk Chair, Coffee Maker, Notebook]

        --- 3. Parallel Streams ---
Sequential Total: $25927.90 (Time: 2 ms)
Parallel Total: $25927.90 (Time: 2 ms)
Parallel speedup: 1.00x

--- 4. Category Count ---
Products per category:
Appliances: 1 products
Electronics: 2 products
Furniture: 1 products
Stationery: 1 products

--- 5. Custom Aggregation ---
Average Rating by Category:
Appliances: 4.0/5.0
Electronics: 4.4/5.0
Stationery: 4.1/5.0
Furniture: 4.2/5.0

        --- 6. Advanced Filtering & Sorting ---
Top Rated Products (Rating >= 4.0):
        ★ 4.5 - Laptop ($899.99)
  ★ 4.3 - Smartphone ($699.99)
  ★ 4.2 - Desk Chair ($199.99)
  ★ 4.1 - Notebook ($4.99)
  ★ 4.0 - Coffee Maker ($89.99)

INVENTORY_SUMMARY:TOTAL_PRODUCTS_5,TOTAL_VALUE_25927.90,CATEGORIES_4*/
