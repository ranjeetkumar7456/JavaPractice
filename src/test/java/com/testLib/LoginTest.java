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

    @BeforeClass
    public void loadConfig() {
        try {
            logData("Using configuration: " + ConfigReader.getProperty("browser"));

            // ✅ FIX: Check if driver is initialized before creating LoginPageActions
            if (driver == null) {
                logger.error("❌ Driver is null in @BeforeClass. Initializing driver first...");
                // Driver should be initialized in @BeforeMethod, so we'll create LoginPageActions there
                return;
            }

            loginPage = new LoginPageActions(driver);

            logger.info("🔧 LoginTest configuration loaded successfully");
            logger.debug("Browser: {}, Base URL: {}",
                    ConfigReader.getProperty("browser"),
                    ConfigReader.getProperty("base.url"));
        } catch (Exception e) {
            logger.error("❌ Error in loadConfig: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = {"sanity", "login"})
    public void verifyValidLogin() {
        // ✅ FIX: Initialize loginPage here if not already done
        if (loginPage == null) {
            loginPage = new LoginPageActions(driver);
        }

        logger.info("🎯 Starting valid login test - verifyValidLogin");
        logStep("Starting valid login test");

        String username = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");

        logger.debug("📝 Test Data - Username: {}, Password: {}", username, password);
        logData("Login credentials - Username: " + username + ", Password: " + password);

        logger.info("🔑 Attempting login with valid credentials");
        loginPage.login(username, password);

        logger.info("🔍 Verifying login success by checking URL redirect");
        String currentUrl = driver.getCurrentUrl();
        logger.debug("Current URL: {}", currentUrl);

        logVerification("Verify user is redirected to inventory page after login");
        Assert.assertTrue(currentUrl.contains("inventory"), "Login failed!");

        logger.info("✅ Valid login test completed successfully");
        logStep("Valid login test completed successfully");
    }

    @Test(groups = {"regression", "login"})
    public void verifyInvalidLogin() {
        // ✅ FIX: Initialize loginPage here if not already done
        if (loginPage == null) {
            loginPage = new LoginPageActions(driver);
        }

        logger.info("🎯 Starting invalid login test - verifyInvalidLogin");
        logStep("Starting invalid login test");

        logger.debug("📝 Using invalid credentials - Username: invalid_user, Password: wrong_pass");
        logger.info("🔑 Attempting login with invalid credentials");
        loginPage.login("invalid_user", "wrong_pass");

        logger.info("🔍 Verifying error message for invalid login");
        String errorMessage = loginPage.getErrorMessage();
        logger.debug("Error message received: {}", errorMessage);

        logVerification("Verify error message is shown for invalid credentials");
        Assert.assertTrue(errorMessage.contains("Username and password do not match"),
                "Error message not shown for invalid login!");

        logger.info("✅ Invalid login test completed - Error message verified");
        logStep("Invalid login test completed");
    }

    @Test(groups = {"sanity", "login", "ui"})
    public void verifyLoginPageUIElements() {
        // ✅ FIX: Initialize loginPage here if not already done
        if (loginPage == null) {
            loginPage = new LoginPageActions(driver);
        }

        logger.info("🎯 Verifying all UI elements on login page");
        logStep("Verifying all UI elements on login page");

        logger.debug("🔍 Checking username field visibility");
        Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field should be visible");

        logger.debug("🔍 Checking password field visibility");
        Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field should be visible");

        logger.debug("🔍 Checking login button visibility and state");
        Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be visible");
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be enabled");

        logger.debug("🔍 Checking login logo visibility");
        Assert.assertTrue(loginPage.isLoginLogoDisplayed(), "Login logo should be visible");

        logger.debug("🔍 Checking bot image visibility");
        Assert.assertTrue(loginPage.isBotImageDisplayed(), "Bot image should be visible");

        String pageTitle = driver.getTitle();
        logger.debug("📄 Page Title: {}", pageTitle);
        Assert.assertNotNull(pageTitle, "Page title should not be null");

        String loginButtonText = loginPage.getLoginButtonText();
        logger.debug("🔘 Login Button Text: {}", loginButtonText);
        Assert.assertFalse(loginButtonText.isEmpty(), "Login button should have text");

        String usernamePlaceholder = loginPage.getUsernamePlaceholder();
        String passwordPlaceholder = loginPage.getPasswordPlaceholder();
        logger.debug("📝 Username Placeholder: {}, Password Placeholder: {}",
                usernamePlaceholder, passwordPlaceholder);

        logger.info("✅ UI elements verification completed");
        logStep("UI elements verification completed");
    }

    // ✅ Helper method for logging exceptions
    private void logException(Exception e, String context) {
        logger.error("❌ Exception in {}: {}", context, e.getMessage(), e);
        logData("Exception in " + context + ": " + e.getMessage());
    }

    // ✅ Helper method for logging test status
    private void logTestStatus(String testName, String status) {
        logger.info("📊 Test Status - {}: {}", testName, status);
    }
}