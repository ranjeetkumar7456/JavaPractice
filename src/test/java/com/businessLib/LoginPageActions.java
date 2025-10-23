package com.businessLib;


import com.pageLib.LoginPageLocators;
import org.openqa.selenium.WebDriver;

public class LoginPageActions {

    private WebDriver driver;
    private LoginPageLocators loginPage;

    public LoginPageActions(WebDriver driver) {
        this.driver = driver;
        loginPage = new LoginPageLocators(driver);
    }

    public void login(String username, String password) {
        loginPage.usernameField.clear();
        loginPage.usernameField.sendKeys(username);
        loginPage.passwordField.clear();
        loginPage.passwordField.sendKeys(password);
        loginPage.loginButton.click();
    }

    public String getErrorMessage() {
        try {
            return loginPage.errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
}

