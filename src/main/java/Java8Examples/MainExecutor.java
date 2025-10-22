package Java8Examples;

import java.util.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class MainExecutor {

    public static void main(String[] args) {
        System.out.println("=== MAIN EXECUTOR - All 4 Classes Execution (Java 8 Compatible) ===");

        try {
            // Execution start time
            LocalDateTime startTime = LocalDateTime.now();
            System.out.println("Execution started at: " +
                    startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Execute all classes sequentially
            Map<String, String> results = new HashMap<>();

            System.out.println("\n" + repeatString("=", 60));
            System.out.println("STEP 1: Executing EmployeeDataProcessorUpdated");
            System.out.println(repeatString("=", 60));
            String employeeResult = executeEmployeeDataProcessor();
            results.put("EMPLOYEE", employeeResult);
            System.out.println("Employee Result: " + employeeResult);

            System.out.println("\n" + repeatString("=", 60));
            System.out.println("STEP 2: Executing FinancialCalculator");
            System.out.println(repeatString("=", 60));
            String financialResult = executeFinancialCalculator();
            results.put("FINANCIAL", financialResult);
            System.out.println("Financial Result: " + financialResult);

            System.out.println("\n" + repeatString("=", 60));
            System.out.println("STEP 3: Executing InventoryManagementSystem");
            System.out.println(repeatString("=", 60));
            String inventoryResult = executeInventoryManagementSystem();
            results.put("INVENTORY", inventoryResult);
            System.out.println("Inventory Result: " + inventoryResult);

            System.out.println("\n" + repeatString("=", 60));
            System.out.println("STEP 4: Executing OrderProcessingSystem");
            System.out.println(repeatString("=", 60));
            String orderResult = executeOrderProcessingSystem();
            results.put("ORDER", orderResult);
            System.out.println("Order Result: " + orderResult);

            // Execution end time
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            // Print final summary
            printFinalSummary(results, duration);

        } catch (Exception e) {
            System.err.println("Error in main execution: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String executeEmployeeDataProcessor() {
        try {
            System.out.println("Processing Employee Data...");

            List<EmployeeDataProcessor.Employee> employees = createSampleEmployees();

            // Employee data processing
            long employeeCount = employees.size();
            double avgSalary = employees.stream()
                    .mapToDouble(EmployeeDataProcessor.Employee::getSalary)
                    .average()
                    .orElse(0.0);

            List<String> highPaidEmployees = employees.stream()
                    .filter(emp -> emp.getSalary() > 75000)
                    .map(EmployeeDataProcessor.Employee::getName)
                    .collect(Collectors.toList());

            double maxSalary = employees.stream()
                    .mapToDouble(EmployeeDataProcessor.Employee::getSalary)
                    .max()
                    .orElse(0.0);

            System.out.println("Total Employees: " + employeeCount);
            System.out.println("Average Salary: $" + String.format("%.2f", avgSalary));
            System.out.println("Max Salary: $" + String.format("%.2f", maxSalary));
            System.out.println("High Paid Employees: " + highPaidEmployees);

            return "EMPLOYEE_PROCESSED:" + employeeCount + "_EMPLOYEES_AVG_SALARY_" +
                    String.format("%.2f", avgSalary);

        } catch (Exception e) {
            return "EMPLOYEE_ERROR:" + e.getMessage();
        }
    }

    private static String executeFinancialCalculator() {
        try {
            System.out.println("Processing Financial Data...");

            List<FinancialCalculator.Transaction> transactions = createSampleTransactions();

            // Financial calculations
            double totalIncome = transactions.stream()
                    .filter(t -> "INCOME".equals(t.getType()))
                    .mapToDouble(FinancialCalculator.Transaction::getAmount)
                    .sum();

            double totalExpense = transactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getType()))
                    .mapToDouble(FinancialCalculator.Transaction::getAmount)
                    .sum();

            double netProfit = totalIncome - totalExpense;

            long transactionCount = transactions.stream().count();

            System.out.println("Total Income: $" + String.format("%.2f", totalIncome));
            System.out.println("Total Expense: $" + String.format("%.2f", totalExpense));
            System.out.println("Net Profit: $" + String.format("%.2f", netProfit));
            System.out.println("Total Transactions: " + transactionCount);

            return "FINANCIAL_PROCESSED:INCOME_" + String.format("%.2f", totalIncome) +
                    "_EXPENSE_" + String.format("%.2f", totalExpense);

        } catch (Exception e) {
            return "FINANCIAL_ERROR:" + e.getMessage();
        }
    }

    private static String executeInventoryManagementSystem() {
        try {
            System.out.println("Processing Inventory Data...");

            List<InventoryManagementSystem.Product> products = createSampleProducts();

            // Inventory analysis
            long totalProducts = products.size();
            double totalValue = products.stream()
                    .mapToDouble(InventoryManagementSystem.Product::getTotalValue)
                    .sum();

            long lowStockCount = products.stream()
                    .filter(InventoryManagementSystem.Product::isLowStock)
                    .count();

            Map<String, Long> productsByCategory = products.stream()
                    .collect(Collectors.groupingBy(
                            InventoryManagementSystem.Product::getCategory,
                            Collectors.counting()
                    ));

            List<String> lowStockItems = products.stream()
                    .filter(InventoryManagementSystem.Product::isLowStock)
                    .map(InventoryManagementSystem.Product::getName)
                    .collect(Collectors.toList());

            System.out.println("Total Products: " + totalProducts);
            System.out.println("Total Inventory Value: $" + String.format("%.2f", totalValue));
            System.out.println("Low Stock Items: " + lowStockCount);
            System.out.println("Low Stock Products: " + lowStockItems);
            System.out.println("Products by Category: " + productsByCategory);

            return "INVENTORY_PROCESSED:PRODUCTS_" + totalProducts + "_VALUE_" +
                    String.format("%.2f", totalValue);

        } catch (Exception e) {
            return "INVENTORY_ERROR:" + e.getMessage();
        }
    }

    private static String executeOrderProcessingSystem() {
        try {
            System.out.println("Processing Order Data...");

            List<OrderProcessingSystem.Order> orders = createSampleOrders();

            // Order processing
            long totalOrders = orders.size();
            long validOrders = orders.stream()
                    .filter(order -> order.getAmount() > 0)
                    .filter(order -> !"CANCELLED".equals(order.getStatus()))
                    .count();

            double totalOrderValue = orders.stream()
                    .mapToDouble(OrderProcessingSystem.Order::getAmount)
                    .sum();

            Map<String, Double> customerSpending = orders.stream()
                    .filter(order -> order.getAmount() > 0)
                    .collect(Collectors.groupingBy(
                            OrderProcessingSystem.Order::getCustomerName,
                            Collectors.summingDouble(OrderProcessingSystem.Order::getAmount)
                    ));

            // Find top customer
            Map.Entry<String, Double> topCustomer = customerSpending.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(new AbstractMap.SimpleEntry<String, Double>("None", 0.0));

            System.out.println("Total Orders: " + totalOrders);
            System.out.println("Valid Orders: " + validOrders);
            System.out.println("Total Order Value: $" + String.format("%.2f", totalOrderValue));
            System.out.println("Top Customer: " + topCustomer.getKey() + " ($" +
                    String.format("%.2f", topCustomer.getValue()) + ")");

            return "ORDER_PROCESSED:VALID_" + validOrders + "_TOTAL_VALUE_" +
                    String.format("%.2f", totalOrderValue);

        } catch (Exception e) {
            return "ORDER_ERROR:" + e.getMessage();
        }
    }

    private static void printFinalSummary(Map<String, String> results, Duration duration) {
        System.out.println("\n" + repeatString("=", 70));
        System.out.println("FINAL EXECUTION SUMMARY");
        System.out.println(repeatString("=", 70));

        System.out.println("Execution Date: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Total Duration: " + duration.toMillis() + " ms");

        System.out.println("\n" + repeatString("-", 70));
        System.out.println("RESULTS SUMMARY:");
        System.out.println(repeatString("-", 70));

        for (Map.Entry<String, String> entry : results.entrySet()) {
            String module = entry.getKey();
            String result = entry.getValue();
            String status = result.contains("ERROR") ? "FAILED" : "SUCCESS";
            System.out.printf("%-12s: %-8s - %s%n", module, status,
                    result.length() > 50 ? result.substring(0, 50) + "..." : result);
        }

        long successCount = results.values().stream()
                .filter(result -> !result.contains("ERROR"))
                .count();

        System.out.println("\n" + repeatString("-", 70));
        System.out.printf("OVERALL STATUS: %d/%d modules executed successfully%n",
                successCount, results.size());

        if (successCount == results.size()) {
            System.out.println("ALL MODULES EXECUTED SUCCESSFULLY!");

            String finalOutput = String.format(
                    "MAIN_EXECUTION_COMPLETED:%s|%s|%s|%s|DURATION_%dms",
                    results.get("EMPLOYEE"),
                    results.get("FINANCIAL"),
                    results.get("INVENTORY"),
                    results.get("ORDER"),
                    duration.toMillis()
            );

            System.out.println("\n" + finalOutput);

        } else {
            System.out.println("SOME MODULES FAILED - CHECK LOGS FOR DETAILS");
            System.exit(1);
        }
    }

    // ✅ Java 8-friendly String repeat helper
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // Sample data creation methods
    public static List<EmployeeDataProcessor.Employee> createSampleEmployees() {
        return Arrays.asList(
                new EmployeeDataProcessor.Employee("E001", "Aarav Sharma", 85000, "Development",
                        LocalDate.of(2020, 3, 15), 28,
                        Arrays.asList("Java", "Spring", "MySQL")),
                new EmployeeDataProcessor.Employee("E002", "Priya Patel", 92000, "Development",
                        LocalDate.of(2019, 7, 22), 32,
                        Arrays.asList("Python", "Django", "PostgreSQL")),
                new EmployeeDataProcessor.Employee("E003", "Rahul Kumar", 68000, "Testing",
                        LocalDate.of(2021, 1, 10), 25,
                        Arrays.asList("Selenium", "JUnit", "Java")),
                new EmployeeDataProcessor.Employee("E004", "Anjali Singh", 78000, "Development",
                        LocalDate.of(2020, 11, 5), 29,
                        Arrays.asList("JavaScript", "React", "Node.js"))
        );
    }

    private static List<FinancialCalculator.Transaction> createSampleTransactions() {
        return Arrays.asList(
                new FinancialCalculator.Transaction("T001", 5000.0,
                        LocalDateTime.of(2024, 1, 15, 10, 30),
                        "INCOME", "Salary", "Monthly salary"),
                new FinancialCalculator.Transaction("T002", 1500.0,
                        LocalDateTime.of(2024, 1, 16, 14, 0),
                        "EXPENSE", "Rent", "Office rent"),
                new FinancialCalculator.Transaction("T003", 300.0,
                        LocalDateTime.of(2024, 1, 17, 9, 15),
                        "EXPENSE", "Utilities", "Electricity bill"),
                new FinancialCalculator.Transaction("T004", 2500.0,
                        LocalDateTime.of(2024, 1, 18, 12, 0),
                        "INCOME", "Consulting", "Client project")
        );
    }

    private static List<InventoryManagementSystem.Product> createSampleProducts() {
        return Arrays.asList(
                new InventoryManagementSystem.Product("P001", "Laptop", "Electronics", 899.99, 15, 4.5, LocalDate.of(2026, 12, 31)),
                new InventoryManagementSystem.Product("P002", "Smartphone", "Electronics", 699.99, 8, 4.3, LocalDate.of(2026, 12, 31)),
                new InventoryManagementSystem.Product("P003", "Desk Chair", "Furniture", 199.99, 25, 4.2, LocalDate.of(2028, 6, 30)),
                new InventoryManagementSystem.Product("P004", "Monitor", "Electronics", 249.99, 3, 4.5, LocalDate.of(2026, 10, 31))
        );
    }

    private static List<OrderProcessingSystem.Order> createSampleOrders() {
        return Arrays.asList(
                new OrderProcessingSystem.Order("ORD001", "Rajesh Kumar", LocalDateTime.now().minusDays(2),
                        1200.50, "PENDING",
                        Arrays.asList("Laptop", "Mouse", "Keyboard"), "Mumbai"),
                new OrderProcessingSystem.Order("ORD002", "Priya Sharma", LocalDateTime.now().minusDays(5),
                        450.75, "PENDING",
                        Arrays.asList("Books", "Notebooks"), "Delhi"),
                new OrderProcessingSystem.Order("ORD003", "Amit Patel", LocalDateTime.now().minusDays(1),
                        890.00, "PENDING",
                        Arrays.asList("Smartphone", "Case"), "Bangalore"),
                new OrderProcessingSystem.Order("ORD004", "Neha Gupta", LocalDateTime.now().minusDays(3),
                        2300.25, "CANCELLED",
                        Arrays.asList("TV", "Sound System"), "Kolkata")
        );
    }
}
