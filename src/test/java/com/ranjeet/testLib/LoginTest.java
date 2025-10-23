package com.ranjeet.testLib;

import base.BaseTest;
import com.businessLib.LoginPageActions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;  // ✅ Use ConfigReader instead of PropertyUtils

public class LoginTest extends BaseTest {

    @BeforeClass
    public void loadConfig() {
        // ✅ ConfigReader automatically loads properties, no need to call again
        // Just log that we're using config
        logData("Using configuration: " + ConfigReader.getProperty("browser"));
    }

    @Test(groups = {"sanity", "login"})
    public void verifyValidLogin() {
        logStep("Starting valid login test");

        LoginPageActions loginPage = new LoginPageActions(driver);
        String username = ConfigReader.getProperty("username");  // ✅ Use ConfigReader
        String password = ConfigReader.getProperty("password");  // ✅ Use ConfigReader

        logData("Login credentials - Username: " + username + ", Password: " + password);

        loginPage.login(username, password);

        logVerification("Verify user is redirected to inventory page after login");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"), "Login failed!");

        logStep("Valid login test completed successfully");
    }

    @Test(groups = {"regression", "login"})
    public void verifyInvalidLogin() {
        logStep("Starting invalid login test");

        LoginPageActions loginPage = new LoginPageActions(driver);
        loginPage.login("invalid_user", "wrong_pass");

        logVerification("Verify error message is shown for invalid credentials");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"),
                "Error message not shown for invalid login!");

        logStep("Invalid login test completed");
    }
}