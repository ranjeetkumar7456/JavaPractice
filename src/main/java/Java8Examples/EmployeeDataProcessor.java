package Java8Examples;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class EmployeeDataProcessor {

    // Employee inner class
    public static class Employee {
        private String id;
        private String name;
        private double salary;
        private String department;
        private LocalDate joinDate;
        private int age;
        private List<String> skills;

        public Employee(String id, String name, double salary, String department,
                        LocalDate joinDate, int age, List<String> skills) {
            this.id = id;
            this.name = name;
            this.salary = salary;
            this.department = department;
            this.joinDate = joinDate;
            this.age = age;
            this.skills = skills;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public double getSalary() { return salary; }
        public String getDepartment() { return department; }
        public LocalDate getJoinDate() { return joinDate; }
        public int getAge() { return age; }
        public List<String> getSkills() { return skills; }

        @Override
        public String toString() {
            return String.format("Employee[ID:%s, Name:%s, Salary:%.2f, Dept:%s, Age:%d]",
                    id, name, salary, department, age);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== EMPLOYEE DATA PROCESSOR - Java 8 Streams & Lambda ===");

        List<Employee> employees = createSampleEmployees();

        // 1. Lambda Expressions with forEach
        System.out.println("\n--- 1. Lambda Expressions with forEach ---");
        employees.forEach(emp ->
                System.out.println("• " + emp.getName() + " - $" + emp.getSalary())
        );

        // 2. Method References
        System.out.println("\n--- 2. Method References ---");
        List<String> employeeNames = employees.stream()
                .map(Employee::getName)
                .collect(Collectors.toList());
        employeeNames.forEach(System.out::println);

        // 3. Stream Filtering
        System.out.println("\n--- 3. Stream Filtering ---");
        List<Employee> highPaidDevs = employees.stream()
                .filter(emp -> emp.getSalary() > 75000)
                .filter(emp -> "Development".equals(emp.getDepartment()))
                .collect(Collectors.toList());
        highPaidDevs.forEach(System.out::println);

        // 4. Stream Mapping and Collection
        System.out.println("\n--- 4. Stream Mapping ---");
        Map<String, Double> deptAvgSalary = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
        deptAvgSalary.forEach((dept, avg) ->
                System.out.printf("Department: %s - Average Salary: $%.2f\n", dept, avg)
        );

        // 5. Stream Reduction - Statistics
        System.out.println("\n--- 5. Stream Reduction & Statistics ---");
        DoubleSummaryStatistics stats = employees.stream()
                .mapToDouble(Employee::getSalary)
                .summaryStatistics();
        System.out.printf("Salary Statistics: Count=%d, Min=%.2f, Max=%.2f, Average=%.2f, Sum=%.2f\n",
                stats.getCount(), stats.getMin(), stats.getMax(),
                stats.getAverage(), stats.getSum());

        // 6. FlatMap for nested collections
        System.out.println("\n--- 6. FlatMap for Skills ---");
        List<String> allSkills = employees.stream()
                .flatMap(emp -> emp.getSkills().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("All unique skills: " + allSkills);

        // 7. Custom Comparator with Lambda
        System.out.println("\n--- 7. Sorting with Lambda Comparator ---");
        List<Employee> sortedBySalary = employees.stream()
                .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary()))
                .collect(Collectors.toList());
        sortedBySalary.forEach(emp ->
                System.out.printf("%s: $%.2f\n", emp.getName(), emp.getSalary())
        );

        // Output for next class
        String processedData = "EMPLOYEE_PROCESSING_COMPLETED:" + employees.size() + "_RECORDS";
        System.out.println("\n" + processedData);
    }

    private static List<Employee> createSampleEmployees() {
        return Arrays.asList(
                new Employee("E001", "Aarav Sharma", 85000, "Development",
                        LocalDate.of(2020, 3, 15), 28,
                        Arrays.asList("Java", "Spring", "MySQL")),
                new Employee("E002", "Priya Patel", 92000, "Development",
                        LocalDate.of(2019, 7, 22), 32,
                        Arrays.asList("Python", "Django", "PostgreSQL")),
                new Employee("E003", "Rahul Kumar", 68000, "Testing",
                        LocalDate.of(2021, 1, 10), 25,
                        Arrays.asList("Selenium", "JUnit", "Java")),
                new Employee("E004", "Anjali Singh", 78000, "Development",
                        LocalDate.of(2020, 11, 5), 29,
                        Arrays.asList("JavaScript", "React", "Node.js")),
                new Employee("E005", "Vikram Gupta", 95000, "Management",
                        LocalDate.of(2018, 5, 30), 35,
                        Arrays.asList("Leadership", "Project Management", "Agile")),
                new Employee("E006", "Neha Reddy", 72000, "Testing",
                        LocalDate.of(2022, 2, 14), 26,
                        Arrays.asList("Cypress", "JavaScript", "Automation")),
                new Employee("E007", "Sanjay Mishra", 88000, "Development",
                        LocalDate.of(2019, 9, 8), 31,
                        Arrays.asList("Java", "Microservices", "Docker")),
                new Employee("E008", "Pooja Desai", 69000, "HR",
                        LocalDate.of(2021, 6, 18), 27,
                        Arrays.asList("Recruitment", "Employee Relations", "Training"))
        );
    }
}


// Output

/*
=== EMPLOYEE DATA PROCESSOR - Java 8 Streams & Lambda ===

        --- 1. Lambda Expressions with forEach ---
        • Aarav Sharma - $85000.0
        • Priya Patel - $92000.0
        • Rahul Kumar - $68000.0
        • Anjali Singh - $78000.0
        • Vikram Gupta - $95000.0
        • Neha Reddy - $72000.0
        • Sanjay Mishra - $88000.0
        • Pooja Desai - $69000.0

        --- 2. Method References ---
Aarav Sharma
Priya Patel
Rahul Kumar
Anjali Singh
Vikram Gupta
Neha Reddy
Sanjay Mishra
Pooja Desai

--- 3. Stream Filtering ---
Employee[ID:E001, Name:Aarav Sharma, Salary:85000.00, Dept:Development, Age:28]
Employee[ID:E002, Name:Priya Patel, Salary:92000.00, Dept:Development, Age:32]
Employee[ID:E004, Name:Anjali Singh, Salary:78000.00, Dept:Development, Age:29]
Employee[ID:E007, Name:Sanjay Mishra, Salary:88000.00, Dept:Development, Age:31]

        --- 4. Stream Mapping ---
Department: Development - Average Salary: $85750.00
Department: HR - Average Salary: $69000.00
Department: Management - Average Salary: $95000.00
Department: Testing - Average Salary: $70000.00

        --- 5. Stream Reduction & Statistics ---
Salary Statistics: Count=8, Min=68000.00, Max=95000.00, Average=80875.00, Sum=647000.00

        --- 6. FlatMap for Skills ---
All unique skills: [Agile, Automation, Cypress, Django, Docker, Employee Relations, JUnit, Java, JavaScript, Leadership, Microservices, MySQL, Node.js, PostgreSQL, Project Management, Python, React, Recruitment, Selenium, Spring, Training]

        --- 7. Sorting with Lambda Comparator ---
Vikram Gupta: $95000.00
Priya Patel: $92000.00
Sanjay Mishra: $88000.00
Aarav Sharma: $85000.00
Anjali Singh: $78000.00
Neha Reddy: $72000.00
Pooja Desai: $69000.00
Rahul Kumar: $68000.00

EMPLOYEE_PROCESSING_COMPLETED:8_RECORDS*/
