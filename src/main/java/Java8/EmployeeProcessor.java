package Java8;

import java.util.*;
import java.util.stream.*;
import java.time.*;

public class EmployeeProcessor {

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

    // Method to create sample employees
    public static List<Employee> createSampleEmployees() {
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

    // 1. Lambda Expressions with forEach
    public static void displayEmployeesWithLambda(List<Employee> employees) {
        employees.forEach(emp ->
                System.out.println("• " + emp.getName() + " - $" + emp.getSalary())
        );
    }

    // 2. Method References
    public static List<String> getEmployeeNamesWithMethodReference(List<Employee> employees) {
        List<String> employeeNames = employees.stream()
                .map(Employee::getName)
                .collect(Collectors.toList());
        employeeNames.forEach(System.out::println);
        return employeeNames;
    }

    // 3. Stream Filtering
    public static List<Employee> filterHighPaidDevelopers(List<Employee> employees) {
        List<Employee> highPaidDevs = employees.stream()
                .filter(emp -> emp.getSalary() > 75000)
                .filter(emp -> "Development".equals(emp.getDepartment()))
                .collect(Collectors.toList());
        highPaidDevs.forEach(System.out::println);
        return highPaidDevs;
    }

    // 4. Stream Mapping and Collection
    public static Map<String, Double> getDepartmentAverageSalary(List<Employee> employees) {
        Map<String, Double> deptAvgSalary = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
        deptAvgSalary.forEach((dept, avg) ->
                System.out.printf("Department: %s - Average Salary: $%.2f\n", dept, avg)
        );
        return deptAvgSalary;
    }

    // 5. Stream Reduction - Statistics
    public static DoubleSummaryStatistics getSalaryStatistics(List<Employee> employees) {
        DoubleSummaryStatistics stats = employees.stream()
                .mapToDouble(Employee::getSalary)
                .summaryStatistics();
        System.out.printf("Salary Statistics: Count=%d, Min=%.2f, Max=%.2f, Average=%.2f, Sum=%.2f\n",
                stats.getCount(), stats.getMin(), stats.getMax(),
                stats.getAverage(), stats.getSum());
        return stats;
    }

    // 6. FlatMap for nested collections
    public static List<String> getAllUniqueSkills(List<Employee> employees) {
        List<String> allSkills = employees.stream()
                .flatMap(emp -> emp.getSkills().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("All unique skills: " + allSkills);
        return allSkills;
    }

    // 7. Custom Comparator with Lambda
    public static List<Employee> sortEmployeesBySalaryDescending(List<Employee> employees) {
        List<Employee> sortedBySalary = employees.stream()
                .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary()))
                .collect(Collectors.toList());
        sortedBySalary.forEach(emp ->
                System.out.printf("%s: $%.2f\n", emp.getName(), emp.getSalary())
        );
        return sortedBySalary;
    }
}