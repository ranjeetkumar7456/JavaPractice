package Java8Examples;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class OrderProcessingSystem {

    @FunctionalInterface
    interface OrderValidator {
        boolean validate(Order order);
    }

    @FunctionalInterface
    interface OrderProcessor {
        ProcessingResult process(Order order);
    }

    @FunctionalInterface
    interface OrderNotifier {
        void notify(Order order, String message);
    }

    static class Order {
        private String orderId;
        private String customerName;
        private LocalDateTime orderDate;
        private double amount;
        private String status;
        private List<String> items;
        private String shippingAddress;

        public Order(String orderId, String customerName, LocalDateTime orderDate,
                     double amount, String status, List<String> items, String shippingAddress) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.orderDate = orderDate;
            this.amount = amount;
            this.status = status;
            this.items = items;
            this.shippingAddress = shippingAddress;
        }

        public String getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public List<String> getItems() { return items; }
        public String getShippingAddress() { return shippingAddress; }

        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return String.format("Order[ID:%s, Customer:%s, Amount:%.2f, Status:%s, Items:%d]",
                    orderId, customerName, amount, status, items.size());
        }
    }

    static class ProcessingResult {
        private boolean success;
        private String message;
        private LocalDateTime processedAt;

        public ProcessingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.processedAt = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("ProcessingResult[Success:%s, Message:%s, Time:%s]",
                    success, message, processedAt.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== ORDER PROCESSING SYSTEM - Java 8 Functional Interfaces & Method References ===\n");

        List<Order> orders = createSampleOrders();

        // 1. Custom Functional Interfaces
        System.out.println("--- 1. Custom Functional Interfaces ---");

        OrderValidator amountValidator = order -> order.getAmount() > 0;
        OrderValidator statusValidator = order -> !"CANCELLED".equals(order.getStatus());

        Predicate<Order> combinedValidator = amountValidator::validate;

        List<Order> validOrders = orders.stream()
                .filter(combinedValidator)
                .filter(statusValidator::validate)
                .collect(Collectors.toList());

        System.out.println("Valid orders: " + validOrders.size() + "/" + orders.size());
        validOrders.forEach(order -> System.out.println("  ✓ " + order));

        // 2. Method References in different forms
        System.out.println("\n--- 2. Method References ---");

        OrderProcessor statusUpdater = OrderProcessingSystem::updateOrderStatus;

        Function<Order, String> orderSummarizer = OrderProcessingSystem::generateOrderSummary;

        Supplier<List<String>> listSupplier = ArrayList::new;

        validOrders.stream()
                .map(statusUpdater::process)
                .forEach(result -> System.out.println("  " + result));

        // 3. Function Composition
        System.out.println("\n--- 3. Function Composition ---");

        Function<Order, String> customerExtractor = Order::getCustomerName;
        Function<String, String> nameFormatter = name -> "Customer: " + name.toUpperCase();
        Function<Order, String> composedFunction = customerExtractor.andThen(nameFormatter);

        validOrders.stream()
                .map(composedFunction)
                .distinct()
                .forEach(System.out::println);

        // 4. Consumer and BiConsumer
        System.out.println("\n--- 4. Consumer Operations ---");

        Consumer<Order> orderLogger = order ->
                System.out.printf("[LOG] Order %s - %s - $%.2f\n",
                        order.getOrderId(), order.getStatus(), order.getAmount());

        BiConsumer<Order, String> statusUpdaterBi = (order, newStatus) -> {
            String oldStatus = order.getStatus();
            order.setStatus(newStatus);
            System.out.printf("Status updated: %s -> %s for order %s\n",
                    oldStatus, newStatus, order.getOrderId());
        };

        validOrders.forEach(orderLogger);
        validOrders.forEach(order -> statusUpdaterBi.accept(order, "PROCESSED"));

        // 5. Supplier for object creation
        System.out.println("\n--- 5. Supplier Usage ---");

        Supplier<Order> sampleOrderSupplier = () ->
                new Order("NEW_" + System.currentTimeMillis(), "New Customer",
                        LocalDateTime.now(), 99.99, "PENDING",
                        Arrays.asList("Sample Item"), "Sample Address");

        Order newOrder = sampleOrderSupplier.get();
        System.out.println("Generated sample order: " + newOrder);

        // 6. Predicate combinations
        System.out.println("\n--- 6. Predicate Combinations ---");

        Predicate<Order> highValue = order -> order.getAmount() > 500;
        Predicate<Order> recentOrder = order ->
                order.getOrderDate().isAfter(LocalDateTime.now().minusDays(7));
        Predicate<Order> urgentOrder = highValue.and(recentOrder);

        List<Order> urgentOrders = validOrders.stream()
                .filter(urgentOrder)
                .collect(Collectors.toList());

        System.out.println("Urgent orders (High value + Recent): " + urgentOrders.size());
        urgentOrders.forEach(order ->
                System.out.printf("  🚨 %s - $%.2f - %s\n",
                        order.getOrderId(), order.getAmount(),
                        order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
        );

        // 7. Complex stream processing with custom functions
        System.out.println("\n--- 7. Complex Stream Processing ---");

        Map<String, Double> customerTotalSpending = validOrders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomerName,
                        Collectors.summingDouble(Order::getAmount)
                ));

        System.out.println("Customer Total Spending:");
        customerTotalSpending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("  %s: $%.2f\n", entry.getKey(), entry.getValue())
                );

        // Final output
        String finalOutput = String.format(
                "ORDER_PROCESSING_COMPLETED:VALID_%d,URGENT_%d,TOTAL_AMOUNT_%.2f",
                validOrders.size(), urgentOrders.size(),
                validOrders.stream().mapToDouble(Order::getAmount).sum()
        );
        System.out.println("\n" + finalOutput);
    }

    private static ProcessingResult updateOrderStatus(Order order) {
        String newStatus = order.getAmount() > 1000 ? "PRIORITY" : "STANDARD";
        order.setStatus(newStatus);
        return new ProcessingResult(true, "Updated to " + newStatus);
    }

    private static String generateOrderSummary(Order order) {
        return String.format("Order %s: %s - %d items - $%.2f",
                order.getOrderId(), order.getCustomerName(),
                order.getItems().size(), order.getAmount());
    }

    private static List<Order> createSampleOrders() {
        return Arrays.asList(
                new Order("ORD001", "Rajesh Kumar", LocalDateTime.now().minusDays(2),
                        1200.50, "PENDING",
                        Arrays.asList("Laptop", "Mouse", "Keyboard"), "Mumbai"),
                new Order("ORD002", "Priya Sharma", LocalDateTime.now().minusDays(5),
                        450.75, "PENDING",
                        Arrays.asList("Books", "Notebooks"), "Delhi"),
                new Order("ORD003", "Amit Patel", LocalDateTime.now().minusDays(1),
                        890.00, "PENDING",
                        Arrays.asList("Smartphone", "Case"), "Bangalore"),
                new Order("ORD004", "Neha Gupta", LocalDateTime.now().minusDays(10),
                        0.0, "PENDING",
                        Arrays.asList("Sample"), "Chennai"),
                new Order("ORD005", "Sanjay Singh", LocalDateTime.now().minusDays(3),
                        2300.25, "CANCELLED",
                        Arrays.asList("TV", "Sound System"), "Kolkata")
        );
    }
}

//Output

/*
=== ORDER PROCESSING SYSTEM - Java 8 Functional Interfaces & Method References ===

        --- 1. Custom Functional Interfaces ---
Valid orders: 3/5
        ✓ Order[ID:ORD001, Customer:Rajesh Kumar, Amount:1200.50, Status:PENDING, Items:3]
        ✓ Order[ID:ORD002, Customer:Priya Sharma, Amount:450.75, Status:PENDING, Items:2]
        ✓ Order[ID:ORD003, Customer:Amit Patel, Amount:890.00, Status:PENDING, Items:2]

        --- 2. Method References ---
ProcessingResult[Success:true, Message:Updated to PRIORITY, Time:12:52:39.1901983]
ProcessingResult[Success:true, Message:Updated to STANDARD, Time:12:52:39.1921863]
ProcessingResult[Success:true, Message:Updated to STANDARD, Time:12:52:39.1921863]

        --- 3. Function Composition ---
Customer: RAJESH KUMAR
Customer: PRIYA SHARMA
Customer: AMIT PATEL

--- 4. Consumer Operations ---
        [LOG] Order ORD001 - PRIORITY - $1200.50
        [LOG] Order ORD002 - STANDARD - $450.75
        [LOG] Order ORD003 - STANDARD - $890.00
Status updated: PRIORITY -> PROCESSED for order ORD001
Status updated: STANDARD -> PROCESSED for order ORD002
Status updated: STANDARD -> PROCESSED for order ORD003

--- 5. Supplier Usage ---
Generated sample order: Order[ID:NEW_1760426559199, Customer:New Customer, Amount:99.99, Status:PENDING, Items:1]

        --- 6. Predicate Combinations ---
Urgent orders (High value + Recent): 2
        🚨 ORD001 - $1200.50 - 2025-10-12
        🚨 ORD003 - $890.00 - 2025-10-13

        --- 7. Complex Stream Processing ---
Customer Total Spending:
Rajesh Kumar: $1200.50
Amit Patel: $890.00
Priya Sharma: $450.75

ORDER_PROCESSING_COMPLETED:VALID_3,URGENT_2,TOTAL_AMOUNT_2541.25*/
