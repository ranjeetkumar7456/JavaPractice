package driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import utils.AdvancedLogger;
import utils.ConfigReader;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DriverManager - Handles WebDriver creation and management
 * Supports Chrome, Firefox, Edge, Safari with thread safety
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, WebDriver> driverPool = new ConcurrentHashMap<>();

    /**
     * Initialize WebDriver instance for a specific thread
     */
    public static synchronized void initDriver(String threadName) {
        try {
            // Check if driver already exists for this thread
            if (driverThreadLocal.get() != null) {
                AdvancedLogger.info("[" + threadName + "] ♻️ WebDriver already initialized, reusing...");
                return;
            }

            String browser = ConfigReader.getProperty("browser", "chrome").toLowerCase();
            boolean headless = ConfigReader.getBooleanProperty("headless");
            int implicitWait = ConfigReader.getIntProperty("implicit.wait");
            int pageLoadTimeout = ConfigReader.getIntProperty("page.load.timeout");

            WebDriver driver = createDriver(browser, headless);

            // Apply timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            driver.manage().window().maximize();

            driverThreadLocal.set(driver);
            driverPool.put(threadName, driver);

            AdvancedLogger.info("[" + threadName + "] ✅ " + browser.toUpperCase() + " WebDriver initialized successfully");

        } catch (Exception e) {
            AdvancedLogger.error("❌ Driver initialization failed in thread: " + threadName);
            throw new RuntimeException("Driver initialization failed", e);
        }
    }

    /**
     * Create WebDriver instance based on browser type
     */
    private static WebDriver createDriver(String browser, boolean headless) {
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--disable-gpu", "--remote-allow-origins=*", "--start-maximized",
                        "--disable-blink-features=AutomationControlled");
                return new ChromeDriver(chromeOptions);

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                firefoxOptions.addArguments("--width=1920", "--height=1080");
                return new FirefoxDriver(firefoxOptions);

            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();

            case "safari":
                // Safari preinstalled on macOS
                return new SafariDriver();

            default:
                throw new IllegalArgumentException("❌ Unsupported browser: " + browser);
        }
    }

    /**
     * Returns WebDriver for the current thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("❌ WebDriver is not initialized. Call initDriver() first.");
        }
        return driver;
    }

    /**
     * Quits the driver instance for the current thread
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                AdvancedLogger.info("🚗 Driver quit successfully for thread: " + Thread.currentThread().getName());
            } catch (Exception e) {
                AdvancedLogger.error("❌ Error while quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
                // Remove from pool as well
                driverPool.entrySet().removeIf(entry -> entry.getValue().equals(driver));
            }
        }
    }

    /**
     * Check if driver is initialized
     */
    public static boolean isDriverInitialized() {
        return driverThreadLocal.get() != null;
    }

    /**
     * Get current browser name
     */
    public static String getCurrentBrowser() {
        return ConfigReader.getProperty("browser", "chrome");
    }

    /**
     * Clean up all drivers (for suite level cleanup)
     */
    public static void cleanupAllDrivers() {
        driverPool.forEach((threadName, driver) -> {
            try {
                if (driver != null) {
                    driver.quit();
                    AdvancedLogger.info("🧹 Cleaned up driver for thread: " + threadName);
                }
            } catch (Exception e) {
                AdvancedLogger.error("❌ Error cleaning up driver for thread: " + threadName);
            }
        });
        driverPool.clear();
        driverThreadLocal.remove();
    }
}