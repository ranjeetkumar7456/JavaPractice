package Java8Examples;

import java.util.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FinancialCalculator {

    static class Transaction {
        private String id;
        private double amount;
        private LocalDateTime timestamp;
        private String type;
        private String category;
        private String description;

        public Transaction(String id, double amount, LocalDateTime timestamp,
                           String type, String category, String description) {
            this.id = id;
            this.amount = amount;
            this.timestamp = timestamp;
            this.type = type;
            this.category = category;
            this.description = description;
        }

        public String getId() { return id; }
        public double getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }

        // Java 8 compatible optional-like method
        public String getDescriptionSafe() {
            return description != null ? description : "No description provided";
        }
    }

    static class FinancialReport {
        private double totalIncome;
        private double totalExpense;
        private double netProfit;
        private Map<String, Double> categoryWiseExpense;
        private String analysis;

        public FinancialReport(double totalIncome, double totalExpense,
                               Map<String, Double> categoryWiseExpense, String analysis) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netProfit = totalIncome - totalExpense;
            this.categoryWiseExpense = categoryWiseExpense;
            this.analysis = analysis;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== FINANCIAL REPORT ===\n");
            sb.append(String.format("Total Income: $%.2f\n", totalIncome));
            sb.append(String.format("Total Expense: $%.2f\n", totalExpense));
            sb.append(String.format("Net Profit: $%.2f\n", netProfit));
            sb.append("\nCategory-wise Expenses:\n");
            categoryWiseExpense.forEach((category, amount) ->
                    sb.append(String.format("  %s: $%.2f\n", category, amount))
            );
            if (analysis != null) {
                sb.append("\nAnalysis: ").append(analysis);
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== FINANCIAL CALCULATOR - Java 8 Optional & Date/Time API ===\n");

        List<Transaction> transactions = createSampleTransactions();

        // 1. Null-safe operations without Optional
        System.out.println("--- 1. Null-safe Operations ---");
        transactions.forEach(transaction -> {
            String desc = transaction.getDescriptionSafe();
            System.out.printf("Transaction %s: %s - %s\n",
                    transaction.getId(), desc, transaction.getAmount());
        });

        // 2. Date/Time API operations
        System.out.println("\n--- 2. Date/Time API Operations ---");
        LocalDateTime now = LocalDateTime.now();
        transactions.stream()
                .filter(t -> t.getTimestamp().isAfter(now.minusMonths(1)))
                .forEach(t -> {
                    long daysAgo = ChronoUnit.DAYS.between(t.getTimestamp(), now);
                    System.out.printf("Recent transaction: %s - %.2f (%d days ago)\n",
                            t.getCategory(), t.getAmount(), daysAgo);
                });

        // 3. Grouping transactions by date periods
        System.out.println("\n--- 3. Grouping by Date Periods ---");
        Map<YearMonth, List<Transaction>> monthlyTransactions = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTimestamp())
                ));

        monthlyTransactions.forEach((yearMonth, transList) -> {
            double monthlyTotal = transList.stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            System.out.printf("%s: %d transactions, Total: $%.2f\n",
                    yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    transList.size(), monthlyTotal);
        });

        // 4. CompletableFuture for async calculations
        System.out.println("\n--- 4. CompletableFuture for Async Processing ---");
        CompletableFuture<FinancialReport> reportFuture =
                CompletableFuture.supplyAsync(() -> generateFinancialReport(transactions));

        System.out.println("Generating financial report asynchronously...");
        System.out.println("Main thread can do other work here...");

        // Get the result
        FinancialReport report = reportFuture.get();
        System.out.println("\n" + report);

        // 5. Traditional null checking instead of Optional
        System.out.println("--- 5. Traditional Null Checking ---");
        String detailedAnalysis = getDetailedAnalysis(transactions);
        if (detailedAnalysis != null) {
            System.out.println("Detailed Analysis: " + detailedAnalysis);
        } else {
            System.out.println("No detailed analysis available");
        }

        // Output for next class
        String output = String.format("FINANCIAL_DATA_PROCESSED:INCOME_%.2f_EXPENSE_%.2f_PROFIT_%.2f",
                report.totalIncome, report.totalExpense, report.netProfit);
        System.out.println("\n" + output);
    }

    private static FinancialReport generateFinancialReport(List<Transaction> transactions) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        double totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        Map<String, Double> categoryExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        String analysis = totalIncome > totalExpense ?
                "Healthy financial status with positive cash flow" :
                "Attention needed: Expenses exceeding income";

        return new FinancialReport(totalIncome, totalExpense, categoryExpense, analysis);
    }

    private static String getDetailedAnalysis(List<Transaction> transactions) {
        Optional<Transaction> maxTransaction = transactions.stream()
                .max(Comparator.comparingDouble(Transaction::getAmount));

        if (maxTransaction.isPresent()) {
            Transaction t = maxTransaction.get();
            return t.getDescription() != null ?
                    "Largest transaction: " + t.getDescription() :
                    "Largest transaction amount: " + t.getAmount();
        }
        return null;
    }

    private static List<Transaction> createSampleTransactions() {
        return Arrays.asList(
                new Transaction("T001", 5000.0,
                        LocalDateTime.of(2024, 1, 15, 10, 30),
                        "INCOME", "Salary", "Monthly salary"),
                new Transaction("T002", 1500.0,
                        LocalDateTime.of(2024, 1, 16, 14, 0),
                        "EXPENSE", "Rent", "Office rent"),
                new Transaction("T003", 300.0,
                        LocalDateTime.of(2024, 1, 17, 9, 15),
                        "EXPENSE", "Utilities", "Electricity bill"),
                new Transaction("T004", 800.0,
                        LocalDateTime.of(2024, 1, 18, 11, 0),
                        "INCOME", "Freelance", null),
                new Transaction("T005", 200.0,
                        LocalDateTime.of(2024, 1, 19, 16, 45),
                        "EXPENSE", "Food", "Team lunch")
        );
    }
}


// Output

/*
=== FINANCIAL CALCULATOR - Java 8 Optional & Date/Time API ===

        --- 1. Null-safe Operations ---
Transaction T001: Monthly salary - 5000.0
Transaction T002: Office rent - 1500.0
Transaction T003: Electricity bill - 300.0
Transaction T004: No description provided - 800.0
Transaction T005: Team lunch - 200.0

        --- 2. Date/Time API Operations ---

        --- 3. Grouping by Date Periods ---
Jan 2024: 5 transactions, Total: $7800.00

        --- 4. CompletableFuture for Async Processing ---
Generating financial report asynchronously...
Main thread can do other work here...

        === FINANCIAL REPORT ===
Total Income: $5800.00
Total Expense: $2000.00
Net Profit: $3800.00

Category-wise Expenses:
Utilities: $300.00
Food: $200.00
Rent: $1500.00

Analysis: Healthy financial status with positive cash flow
--- 5. Traditional Null Checking ---
Detailed Analysis: Largest transaction: Monthly salary

FINANCIAL_DATA_PROCESSED:INCOME_5800.00_EXPENSE_2000.00_PROFIT_3800.00*/
