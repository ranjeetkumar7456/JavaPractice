package com.businessLib;

import com.pageLib.LoginPageLocators;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPageActions {

    private WebDriver driver;
    private LoginPageLocators loginPage;
    private WebDriverWait wait;

    public LoginPageActions(WebDriver driver) {
        // ✅ FIX: Check if driver is not null before creating WebDriverWait
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }

        this.driver = driver;
        loginPage = new LoginPageLocators(driver);

        // ✅ FIX: Create WebDriverWait only when driver is available
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String username, String password) {
        // ✅ Using getter methods
        loginPage.getUsernameField().clear();
        loginPage.getUsernameField().sendKeys(username);
        loginPage.getPasswordField().clear();
        loginPage.getPasswordField().sendKeys(password);
        loginPage.getLoginButton().click();
    }

    public String getErrorMessage() {
        try {
            return loginPage.getErrorMessage().getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return loginPage.getErrorMessage().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if username field is displayed
     */
    public boolean isUsernameFieldDisplayed() {
        try {
            return loginPage.getUsernameField().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if password field is displayed
     */
    public boolean isPasswordFieldDisplayed() {
        try {
            return loginPage.getPasswordField().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if login button is displayed
     */
    public boolean isLoginButtonDisplayed() {
        try {
            return loginPage.getLoginButton().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if login button is enabled
     */
    public boolean isLoginButtonEnabled() {
        try {
            return loginPage.getLoginButton().isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for error message to be visible
     */
    public void waitForErrorMessage() {
        try {
            // ✅ FIX: Check if wait is initialized
            if (wait != null) {
                wait.until(ExpectedConditions.visibilityOf(loginPage.getErrorMessage()));
            }
        } catch (Exception e) {
            // Ignore if error message doesn't appear
        }
    }

    /**
     * Clear both username and password fields
     */
    public void clearFields() {
        try {
            loginPage.getUsernameField().clear();
            loginPage.getPasswordField().clear();
        } catch (Exception e) {
            // Ignore clear exceptions
        }
    }

    /**
     * Get placeholder text of username field
     */
    public String getUsernamePlaceholder() {
        try {
            return loginPage.getUsernameField().getAttribute("placeholder");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get placeholder text of password field
     */
    public String getPasswordPlaceholder() {
        try {
            return loginPage.getPasswordField().getAttribute("placeholder");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get the type attribute of password field (should be "password")
     */
    public String getPasswordFieldType() {
        try {
            return loginPage.getPasswordField().getAttribute("type");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if login logo is displayed
     */
    public boolean isLoginLogoDisplayed() {
        try {
            return loginPage.getLoginLogo().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if bot image is displayed
     */
    public boolean isBotImageDisplayed() {
        try {
            return loginPage.getBotImage().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user is on login page by verifying URL or title
     */
    public boolean isOnLoginPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();
            return currentUrl.contains("login") ||
                    pageTitle.toLowerCase().contains("login") ||
                    isUsernameFieldDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current page title
     */
    public String getPageTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get current page URL
     */
    public String getPageUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if login was successful by verifying URL change
     */
    public boolean isLoginSuccessful() {
        try {
            String currentUrl = driver.getCurrentUrl();
            return !currentUrl.contains("login") &&
                    (currentUrl.contains("inventory") ||
                            currentUrl.contains("dashboard") ||
                            currentUrl.contains("home"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for page to load completely
     */
    public void waitForPageToLoad() {
        try {
            // ✅ FIX: Check if wait is initialized
            if (wait != null) {
                wait.until(ExpectedConditions.elementToBeClickable(loginPage.getLoginButton()));
            }
        } catch (Exception e) {
            // Ignore timeout exceptions
        }
    }

    /**
     * Get text of login button
     */
    public String getLoginButtonText() {
        try {
            return loginPage.getLoginButton().getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get value of username field
     */
    public String getUsernameFieldValue() {
        try {
            return loginPage.getUsernameField().getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get value of password field
     */
    public String getPasswordFieldValue() {
        try {
            return loginPage.getPasswordField().getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }
}