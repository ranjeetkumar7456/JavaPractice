package Java8;

import org.testng.annotations.*;
import java.util.*;
import java.util.stream.*;
import static org.testng.Assert.*;

public class EmployeeProcessorTest {

    private List<EmployeeProcessor.Employee> employees;

    @BeforeClass
    public void setUp() {
        employees = EmployeeProcessor.createSampleEmployees();
    }

    @Test
    public void testEmployeeCreation() {
        System.out.println("=== Testing Employee Creation ===");
        assertNotNull(employees);
        assertEquals(employees.size(), 8);

        EmployeeProcessor.Employee firstEmployee = employees.get(0);
        assertEquals(firstEmployee.getName(), "Aarav Sharma");
        assertEquals(firstEmployee.getSalary(), 85000.0, 0.01);
        assertEquals(firstEmployee.getDepartment(), "Development");
    }

    @Test
    public void testLambdaExpressionsWithForEach() {
        System.out.println("\n--- Testing Lambda Expressions with forEach ---");
        EmployeeProcessor.displayEmployeesWithLambda(employees);
        assertNotNull(employees);
        assertEquals(employees.size(), 8);
    }

    @Test
    public void testMethodReferences() {
        System.out.println("\n--- Testing Method References ---");
        List<String> employeeNames = EmployeeProcessor.getEmployeeNamesWithMethodReference(employees);

        assertEquals(employeeNames.size(), 8);
        assertEquals(employeeNames.get(0), "Aarav Sharma");
        assertEquals(employeeNames.get(1), "Priya Patel");
    }

    @Test
    public void testStreamFiltering() {
        System.out.println("\n--- Testing Stream Filtering ---");
        List<EmployeeProcessor.Employee> highPaidDevs = EmployeeProcessor.filterHighPaidDevelopers(employees);

        assertEquals(highPaidDevs.size(), 4);
        assertTrue(highPaidDevs.stream().allMatch(emp -> emp.getSalary() > 75000));
        assertTrue(highPaidDevs.stream().allMatch(emp -> "Development".equals(emp.getDepartment())));
    }

    @Test
    public void testStreamMappingAndCollection() {
        System.out.println("\n--- Testing Stream Mapping ---");
        Map<String, Double> deptAvgSalary = EmployeeProcessor.getDepartmentAverageSalary(employees);

        assertEquals(deptAvgSalary.size(), 4);
        assertEquals(deptAvgSalary.get("Development"), 85750.0, 0.01);
        assertEquals(deptAvgSalary.get("HR"), 69000.0, 0.01);
    }

    @Test
    public void testStreamReductionAndStatistics() {
        System.out.println("\n--- Testing Stream Reduction & Statistics ---");
        DoubleSummaryStatistics stats = EmployeeProcessor.getSalaryStatistics(employees);

        assertEquals(stats.getCount(), 8);
        assertEquals(stats.getMin(), 68000.0, 0.01);
        assertEquals(stats.getMax(), 95000.0, 0.01);
        assertEquals(stats.getSum(), 647000.0, 0.01);
    }

    @Test
    public void testFlatMapForSkills() {
        System.out.println("\n--- Testing FlatMap for Skills ---");
        List<String> allSkills = EmployeeProcessor.getAllUniqueSkills(employees);

        assertTrue(allSkills.size() > 0);
        assertTrue(allSkills.contains("Java"));
        assertTrue(allSkills.contains("Python"));
        assertTrue(allSkills.contains("React"));
    }

    @Test
    public void testSortingWithLambdaComparator() {
        System.out.println("\n--- Testing Sorting with Lambda Comparator ---");
        List<EmployeeProcessor.Employee> sortedBySalary = EmployeeProcessor.sortEmployeesBySalaryDescending(employees);

        assertEquals(sortedBySalary.get(0).getName(), "Vikram Gupta");
        assertEquals(sortedBySalary.get(0).getSalary(), 95000.0, 0.01);
        assertEquals(sortedBySalary.get(7).getName(), "Rahul Kumar");
        assertEquals(sortedBySalary.get(7).getSalary(), 68000.0, 0.01);
    }

    @Test
    public void testEmployeeSkills() {
        System.out.println("\n--- Testing Employee Skills ---");
        EmployeeProcessor.Employee emp = employees.get(0);

        assertNotNull(emp.getSkills());
        assertEquals(emp.getSkills().size(), 3);
        assertTrue(emp.getSkills().contains("Java"));
        assertTrue(emp.getSkills().contains("Spring"));
        assertTrue(emp.getSkills().contains("MySQL"));
    }

    @DataProvider(name = "employeeData")
    public Object[][] provideEmployeeData() {
        return new Object[][] {
                {0, "Aarav Sharma", 85000.0, "Development"},
                {1, "Priya Patel", 92000.0, "Development"},
                {2, "Rahul Kumar", 68000.0, "Testing"},
                {3, "Anjali Singh", 78000.0, "Development"}
        };
    }

    @Test(dataProvider = "employeeData")
    public void testEmployeeDataWithDataProvider(int index, String expectedName,
                                                 double expectedSalary, String expectedDept) {
        EmployeeProcessor.Employee emp = employees.get(index);
        assertEquals(emp.getName(), expectedName);
        assertEquals(emp.getSalary(), expectedSalary, 0.01);
        assertEquals(emp.getDepartment(), expectedDept);
    }

    @AfterMethod
    public void afterMethod() {
        System.out.println("Test method completed");
    }

    @AfterClass
    public void tearDown() {
        System.out.println("\n=== ALL TESTS COMPLETED ===");
        employees = null;
    }
}