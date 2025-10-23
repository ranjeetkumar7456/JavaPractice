package ConfigUtil;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import utils.AdvancedLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Advanced ExtentReport Manager with Thread Safety
 * Supports parallel execution, custom logging, screenshots
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static final ThreadLocal<String> testName = new ThreadLocal<>();
    private static final Map<String, ExtentTest> nodeMap = new HashMap<>();

    private static final String REPORT_PATH = "./extent-reports/";
    private static final String REPORT_NAME = "Amazon_Automation_Report_";

    /**
     * Setup Extent Report with advanced configuration
     */
    public static synchronized void setupExtentReport() {
        if (extent == null) {
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            String reportPath = REPORT_PATH + REPORT_NAME + timeStamp + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath)
                    .viewConfigurer()
                    .viewOrder()
                    .as(new ViewName[] {
                            ViewName.DASHBOARD,
                            ViewName.TEST,
                            ViewName.CATEGORY,
                            ViewName.AUTHOR,
                            ViewName.DEVICE
                    })
                    .apply();

            // Spark Reporter Configuration
            sparkReporter.config().setDocumentTitle("Amazon Automation Report");
            sparkReporter.config().setReportName("Amazon Selenium Framework");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
            sparkReporter.config().setEncoding("UTF-8");
            sparkReporter.config().setJs("$('.brand-logo').text('Amazon Automation');");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // System Information
            extent.setSystemInfo("Organization", "Amazon");
            extent.setSystemInfo("Automation Framework", "Selenium WebDriver");
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("User Name", System.getProperty("user.name"));

            AdvancedLogger.info("📊 Extent Report initialized at: " + reportPath);
        }
    }

    /**
     * ✅ CREATE TEST METHOD ADDED - This was missing
     */
    public static synchronized void createTest(String className, String methodName, String groups, String author) {
        String fullTestName = className + " - " + methodName;
        testName.set(fullTestName);

        ExtentTest extentTest = extent.createTest(fullTestName)
                .assignCategory(groups.split(","))
                .assignAuthor(author)
                .assignDevice(System.getProperty("os.name"));

        test.set(extentTest);
        AdvancedLogger.info("🧪 Test Created: " + fullTestName + " | Groups: " + groups + " | Author: " + author);
    }

    /**
     * Create node for test method (for better organization)
     */
    public static synchronized void createNode(String nodeName) {
        ExtentTest node = getTest().createNode(nodeName);
        nodeMap.put(Thread.currentThread().getName() + "_" + nodeName, node);
        AdvancedLogger.info("📁 Node Created: " + nodeName);
    }

    /**
     * Log messages with different levels
     */
    public static void log(Status status, String message) {
        if (getTest() != null) {
            getTest().log(status, message);
            AdvancedLogger.log(status.toString().toLowerCase(), message);
        }
    }

    public static void info(String message) {
        log(Status.INFO, message);
    }

    public static void pass(String message) {
        log(Status.PASS, message);
        AdvancedLogger.pass(message);
    }

    public static void fail(String message) {
        log(Status.FAIL, message);
        AdvancedLogger.fail(message);
    }

    public static void skip(String message) {
        log(Status.SKIP, message);
        AdvancedLogger.skip(message);
    }

    public static void warning(String message) {
        log(Status.WARNING, message);
        AdvancedLogger.warning(message);
    }

    /**
     * Log exception with stack trace
     */
    public static void logException(Throwable throwable) {
        if (getTest() != null) {
            getTest().fail(throwable);
            AdvancedLogger.error("Exception occurred: " + throwable.getMessage());
        }
    }

    /**
     * Add screenshot to report
     */
    public static void addScreenshot(String base64Image, String title) {
        if (getTest() != null) {
            getTest().info(MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image, title).build());
            AdvancedLogger.info("📸 Screenshot added: " + title);
        }
    }

    /**
     * Create label in report
     */
    public static void createLabel(String label, ExtentColor color) {
        if (getTest() != null) {
            Markup labelMarkup = MarkupHelper.createLabel(label, color);
            getTest().info(labelMarkup);
        }
    }

    /**
     * Getters
     */
    public static ExtentTest getTest() {
        return test.get();
    }

    public static ExtentReports getReport() {
        return extent;
    }

    public static String getTestName() {
        return testName.get();
    }

    /**
     * Flush report
     */
    public static void flushReport() {
        if (extent != null) {
            extent.flush();
            AdvancedLogger.info("📄 Report flushed successfully");
            ReportCleanupManager.cleanupOldReports();
        }
    }

    /**
     * Remove thread local instances
     */
    public static void removeTest() {
        test.remove();
        testName.remove();
    }
}