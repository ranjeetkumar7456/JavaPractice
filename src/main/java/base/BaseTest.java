package base;

import ConfigUtil.ExtentReportManager;
import driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.AdvancedLogger;
import utils.ConfigReader;

import java.lang.reflect.Method;

/**
 * Ultimate BaseTest Class - Extend this for all tests
 * Automatic reporting, logging, screenshot capture
 */
public abstract class BaseTest {

    protected WebDriver driver;
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * Suite Level Setup
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuiteSetup(ITestContext context) {
        // Setup Extent Report
        ExtentReportManager.setupExtentReport();

        AdvancedLogger.info("🚀 ========== AUTOMATION SUITE STARTED ==========");
        AdvancedLogger.info("📋 Suite Name: " + context.getSuite().getName());
        AdvancedLogger.info("🌐 Base URL: " + ConfigReader.getProperty("base.url"));
        AdvancedLogger.info("🔧 Browser: " + ConfigReader.getProperty("browser"));
        AdvancedLogger.info("⚡ Execution Type: " + ConfigReader.getProperty("execution.type"));
        AdvancedLogger.info("👤 Username: " + ConfigReader.getProperty("username"));
    }

    /**
     * Test Level Setup
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTestSetup(ITestContext context) {
        AdvancedLogger.info("🔨 Test Context Setup: " + context.getName());
    }

    /**
     * Method Level Setup - Runs before each test method
     */
    @BeforeMethod(alwaysRun = true)
    public void baseSetup(Method method, ITestContext context) {
        String methodName = method.getName();
        String className = this.getClass().getSimpleName();

        AdvancedLogger.info("🎬 Preparing to execute: " + className + "." + methodName);

        // Initialize driver based on execution type
        String executionType = ConfigReader.getProperty("execution.type", "single");
        String threadName = Thread.currentThread().getName();

        if ("parallel".equalsIgnoreCase(executionType)) {
            DriverManager.initDriver(threadName);
            driver = DriverManager.getDriver();
            driverThreadLocal.set(driver);
            AdvancedLogger.info("🖥️ Parallel Driver initialized for thread: " + threadName);
        } else {
            DriverManager.initDriver("MainThread");
            driver = DriverManager.getDriver();
            driverThreadLocal.set(driver);
            AdvancedLogger.info("🖥️ Single Driver initialized");
        }

        // Navigate to base URL
        String baseUrl = ConfigReader.getProperty("base.url");
        driver.get(baseUrl);
        AdvancedLogger.info("🌍 Navigated to: " + baseUrl);

        // Maximize window
        driver.manage().window().maximize();
        AdvancedLogger.info("📱 Browser window maximized");
    }

    /**
     * Method Level Teardown - Runs after each test method
     */
    @AfterMethod(alwaysRun = true)
    public void baseTeardown(ITestResult result, Method method) {
        String methodName = method.getName();
        String className = this.getClass().getSimpleName();

        // Capture screenshot and log result based on test status
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                ExtentReportManager.pass("✅ Test PASSED: " + className + "." + methodName);
                AdvancedLogger.pass("Test completed successfully: " + className + "." + methodName);
                break;

            case ITestResult.FAILURE:
                captureScreenshot("FAILURE_" + className + "_" + methodName);
                ExtentReportManager.fail("❌ Test FAILED: " + className + "." + methodName);
                ExtentReportManager.logException(result.getThrowable());
                AdvancedLogger.fail("Test failed: " + className + "." + methodName);
                break;

            case ITestResult.SKIP:
                ExtentReportManager.skip("⏭️ Test SKIPPED: " + className + "." + methodName);
                AdvancedLogger.skip("Test skipped: " + className + "." + methodName);
                break;
        }

        // Quit driver
        if (driver != null) {
            DriverManager.quitDriver();
            AdvancedLogger.info("🔚 Driver quit successfully");
        }

        ExtentReportManager.removeTest();
        AdvancedLogger.info("🎬 Test execution completed: " + className + "." + methodName + "\n");
    }

    /**
     * Suite Level Teardown
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuiteTeardown() {
        ExtentReportManager.flushReport();
        AdvancedLogger.info("🏁 ========== AUTOMATION SUITE COMPLETED ==========");
    }

    /**
     * Capture screenshot and attach to report
     */
    private void captureScreenshot(String screenshotName) {
        try {
            WebDriver currentDriver = driverThreadLocal.get();
            if (currentDriver != null) {
                String base64Screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BASE64);
                ExtentReportManager.addScreenshot(base64Screenshot, screenshotName);
                AdvancedLogger.info("📸 Screenshot captured: " + screenshotName);
            }
        } catch (Exception e) {
            AdvancedLogger.error("Failed to capture screenshot: " + e.getMessage());
        }
    }

    /**
     * Get current driver instance
     */
    protected WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * Common utility methods for all tests
     */
    protected void logStep(String stepDescription) {
        ExtentReportManager.info("🔹 STEP: " + stepDescription);
        AdvancedLogger.info("Step: " + stepDescription);
    }

    protected void logVerification(String verificationDescription) {
        ExtentReportManager.info("🔍 VERIFICATION: " + verificationDescription);
        AdvancedLogger.info("Verification: " + verificationDescription);
    }

    protected void logData(String dataDescription) {
        ExtentReportManager.info("📊 DATA: " + dataDescription);
        AdvancedLogger.info("Data: " + dataDescription);
    }

    protected void logWarning(String warningDescription) {
        ExtentReportManager.warning("⚠️ WARNING: " + warningDescription);
        AdvancedLogger.warning("Warning: " + warningDescription);
    }
}