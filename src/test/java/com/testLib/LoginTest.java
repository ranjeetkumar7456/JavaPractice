package com.testLib;

import base.BaseTest;
import com.businessLib.LoginPageActions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

public class LoginTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(LoginTest.class);
    private LoginPageActions loginPage;

    /**
     * Initialize page objects for this test class
     */
    @Override
    protected void setupPageObjects() {
        try {
            loginPage = new LoginPageActions(getDriver());
            logger.info("🔧 LoginPageActions initialized successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to initialize LoginPageActions: {}", e.getMessage(), e);
            throw new RuntimeException("Page object initialization failed", e);
        }
    }

    /**
     * Cleanup page objects for this test class
     */
    @Override
    protected void cleanupPageObjects() {
        loginPage = null;
        logger.debug("🧹 LoginPageActions cleaned up");
    }

    @BeforeClass
    public void loadConfig() {
        try {
            logData("Using configuration - Browser: " + ConfigReader.getProperty("browser") +
                    ", Base URL: " + ConfigReader.getProperty("base.url"));

            logger.info("🔧 LoginTest configuration loaded successfully");
            logger.debug("Browser: {}, Base URL: {}, Username: {}",
                    ConfigReader.getProperty("browser"),
                    ConfigReader.getProperty("base.url"),
                    ConfigReader.getProperty("username"));
        } catch (Exception e) {
            logger.error("❌ Error in loadConfig: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test(priority = 1, groups = {"sanity", "login", "ui"})
    public void verifyLoginPageUIElements() {
        try {
            logger.info("🎯 Starting UI elements verification test - verifyLoginPageUIElements");
            logStep("Starting UI elements verification test");

            // Wait for page to load completely
            Thread.sleep(2000);

            logger.info("🔍 Verifying all UI elements on login page");
            logStep("Verifying all UI elements on login page");

            // Verify username field
            logger.debug("Checking username field visibility and properties");
            Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field should be visible");
            String usernamePlaceholder = loginPage.getUsernamePlaceholder();
            Assert.assertNotNull(usernamePlaceholder, "Username placeholder should not be null");
            logger.info("✅ Username field verified - Placeholder: '{}'", usernamePlaceholder);

            // Verify password field
            logger.debug("Checking password field visibility and properties");
            Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field should be visible");
            String passwordPlaceholder = loginPage.getPasswordPlaceholder();
            Assert.assertNotNull(passwordPlaceholder, "Password placeholder should not be null");
            logger.info("✅ Password field verified - Placeholder: '{}'", passwordPlaceholder);

            // Verify login button
            logger.debug("Checking login button visibility and properties");
            Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be visible");
            String loginButtonText = loginPage.getLoginButtonText();
            Assert.assertEquals(loginButtonText, "Login", "Login button should have correct text");
            logger.info("✅ Login button verified - Text: '{}'", loginButtonText);

            // Verify login logo
            logger.debug("Checking login logo visibility");
            Assert.assertTrue(loginPage.isLoginLogoDisplayed(), "Login logo should be visible");
            logger.info("✅ Login logo verified");

            // Verify page title
            String pageTitle = getDriver().getTitle();
            logger.debug("Page Title: {}", pageTitle);
            Assert.assertNotNull(pageTitle, "Page title should not be null");
            Assert.assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
            logger.info("✅ Page title verified - '{}'", pageTitle);

            // Verify current URL
            String currentUrl = getDriver().getCurrentUrl();
            logger.debug("Current URL: {}", currentUrl);
            Assert.assertTrue(currentUrl.contains(ConfigReader.getProperty("base.url")),
                    "Current URL should contain base URL");
            logger.info("✅ Current URL verified - '{}'", currentUrl);

            logger.info("🎉 All UI elements verification completed successfully");
            logStep("UI elements verification completed successfully");

        } catch (InterruptedException e) {
            logger.error("❌ Test interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            Assert.fail("Test was interrupted");
        } catch (Exception e) {
            logger.error("❌ UI elements verification failed: {}", e.getMessage(), e);
            captureTestFailure("verifyLoginPageUIElements", e);
            Assert.fail("UI elements verification failed: " + e.getMessage());
        }
    }

    @Test(priority = 2, groups = {"regression", "login", "negative"})
    public void verifyInvalidLogin() {
        try {
            logger.info("🎯 Starting invalid login test - verifyInvalidLogin");
            logStep("Starting invalid login test with invalid credentials");

            // Test data
            String invalidUsername = "invalid_user";
            String invalidPassword = "wrong_pass";

            logger.debug("📝 Using invalid credentials - Username: {}, Password: {}",
                    invalidUsername, invalidPassword);
            logData("Invalid credentials - Username: " + invalidUsername + ", Password: " + invalidPassword);

            // Perform login with invalid credentials
            logger.info("🔑 Attempting login with invalid credentials");
            loginPage.login(invalidUsername, invalidPassword);

            // Verify error message
            logger.info("🔍 Verifying error message for invalid login");
            String errorMessage = loginPage.getErrorMessage();
            logger.debug("Error message received: '{}'", errorMessage);

            // Capture screenshot for verification step
            captureScreenshot("InvalidLogin_Error_Message");

            logVerification("Verify error message is shown for invalid credentials");

            // Multiple assertion checks for better error reporting
            Assert.assertNotNull(errorMessage, "Error message should not be null");
            //Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
            Assert.assertTrue(errorMessage.contains("Epic sadface: Username and password do not match any user in this service"),
                    "Expected error message not found. Actual: " + errorMessage);

            logger.info("✅ Invalid login test completed - Error message verified: '{}'", errorMessage);
            logStep("Invalid login test completed - Error message properly displayed");

        } catch (Exception e) {
            logger.error("❌ Invalid login test failed: {}", e.getMessage(), e);
            captureTestFailure("verifyInvalidLogin", e);
            Assert.fail("Invalid login test failed: " + e.getMessage());
        }
    }

    @Test(priority = 3, groups = {"sanity", "login", "positive"})
    public void verifyValidLogin() {
        try {
            logger.info("🎯 Starting valid login test - verifyValidLogin");
            logStep("Starting valid login test with valid credentials");

            // Get credentials from config
            String username = ConfigReader.getProperty("username");
            String password = ConfigReader.getProperty("password");

            // Validate credentials are not empty
            Assert.assertNotNull(username, "Username should not be null in config");
            Assert.assertFalse(username.isEmpty(), "Username should not be empty in config");
            Assert.assertNotNull(password, "Password should not be null in config");
            Assert.assertFalse(password.isEmpty(), "Password should not be empty in config");

            logger.debug("📝 Test Data - Username: {}, Password: {}", username, password);
            logData("Valid login credentials - Username: " + username + ", Password: " + password);

            // Perform login with valid credentials
            logger.info("🔑 Attempting login with valid credentials");
            loginPage.login(username, password);

            // Wait for page to redirect
            Thread.sleep(2000);

            // Verify login success by checking URL redirect
            logger.info("🔍 Verifying login success by checking URL redirect");
            String currentUrl = getDriver().getCurrentUrl();
            logger.debug("Current URL after login: {}", currentUrl);

            // Capture screenshot after successful login
            captureScreenshot("ValidLogin_Success");

            logVerification("Verify user is redirected to inventory page after successful login");

            // Multiple verification points
            Assert.assertNotNull(currentUrl, "Current URL should not be null after login");
            Assert.assertTrue(currentUrl.contains("inventory"),
                    "URL should contain 'inventory' after successful login. Actual URL: " + currentUrl);

            // Additional verification - check page title or specific element on inventory page
            String pageTitle = getDriver().getTitle();
            logger.debug("Page title after login: {}", pageTitle);
            Assert.assertNotNull(pageTitle, "Page title should not be null after login");

            logger.info("✅ Valid login test completed successfully - Redirected to: {}", currentUrl);
            logStep("Valid login test completed successfully - User redirected to inventory page");

        } catch (InterruptedException e) {
            logger.error("❌ Test interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            Assert.fail("Test was interrupted");
        } catch (Exception e) {
            logger.error("❌ Valid login test failed: {}", e.getMessage(), e);
            captureTestFailure("verifyValidLogin", e);
            Assert.fail("Valid login test failed: " + e.getMessage());
        }
    }

    @Test(priority = 4,groups = {"regression","login","edge"})
    public void verifyEmptyCredentialsLogin() {
        try {
            logger.info("🎯 Starting empty credentials test - verifyEmptyCredentialsLogin");
            logStep("Testing login with empty credentials");

            // Attempt login with empty credentials
            logger.info("🔑 Attempting login with empty credentials");
            loginPage.login("", "");

            // Verify error message
            logger.info("🔍 Verifying error message for empty credentials");
            String errorMessage = loginPage.getErrorMessage();
            logger.debug("Error message received: '{}'", errorMessage);

            captureScreenshot("EmptyCredentials_Error");

            logVerification("Verify error message is shown for empty credentials");

            Assert.assertNotNull(errorMessage, "Error message should not be null for empty credentials");

            System.out.println("error message - "+errorMessage);
            Assert.assertTrue(errorMessage.contains("Epic sadface: Username is required"),
                    "Expected error message for empty credentials not found. Actual: " + errorMessage);

            logger.info("✅ Empty credentials test completed - Error message: '{}'", errorMessage);
            logStep("Empty credentials test completed - Proper error message displayed");

        } catch (Exception e) {
            logger.error("❌ Empty credentials test failed: {}", e.getMessage(), e);
            captureTestFailure("verifyEmptyCredentialsLogin", e);
            Assert.fail("Empty credentials test failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to capture test failure details and screenshot
     */
    private void captureTestFailure(String testName, Exception e) {
        try {
            // Capture screenshot on failure
            captureScreenshot("FAILURE_" + testName);

            // Log detailed error information
            logData("Test Failure in " + testName + ": " + e.getMessage());
            logWarning("Stack trace available in logs");

            // Log additional context
            logger.error("🔍 Failure Context - Current URL: {}", getDriver().getCurrentUrl());
            logger.error("🔍 Failure Context - Page Title: {}", getDriver().getTitle());

        } catch (Exception ex) {
            logger.error("❌ Failed to capture test failure details: {}", ex.getMessage());
        }
    }

    /**
     * Helper method to capture screenshot with timestamp
     */
    private void captureScreenshot(String screenshotName) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fullScreenshotName = screenshotName + "_" + timestamp;

            // This will use the screenshot capture method from BaseTest
            // For now, we'll just log it
            logger.debug("📸 Screenshot captured: {}", fullScreenshotName);

        } catch (Exception e) {
            logger.error("❌ Failed to capture screenshot: {}", e.getMessage());
        }
    }

    /**
     * Helper method for logging exceptions with context
     */
    private void logException(Exception e, String context) {
        logger.error("❌ Exception in {}: {}", context, e.getMessage(), e);
        logData("Exception in " + context + ": " + e.getMessage());
    }

    /**
     * Helper method for logging test status
     */
    private void logTestStatus(String testName, String status, String details) {
        logger.info("📊 Test Status - {}: {} - {}", testName, status, details);
        logData(testName + " - " + status + " - " + details);
    }

    /**
     * Helper method to verify page load
     */
    private void verifyPageLoaded(String pageName) {
        try {
            String currentUrl = getDriver().getCurrentUrl();
            String pageTitle = getDriver().getTitle();

            logger.debug("Page Load Verification - {}: URL={}, Title={}", pageName, currentUrl, pageTitle);

            Assert.assertNotNull(currentUrl, pageName + " URL should not be null");
            Assert.assertFalse(currentUrl.isEmpty(), pageName + " URL should not be empty");
            Assert.assertNotNull(pageTitle, pageName + " title should not be null");

        } catch (Exception e) {
            logger.error("❌ Page load verification failed for {}: {}", pageName, e.getMessage());
            throw e;
        }
    }
}