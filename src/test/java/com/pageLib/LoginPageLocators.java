package com.pageLib;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.WebDriver;

public class LoginPageLocators {

    public LoginPageLocators(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    // ✅ Private fields with @FindBy annotations
    @FindBy(xpath = "//input[@id='user-name']")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "login-button")
    private WebElement loginButton;

    @FindBy(xpath = "//h3[@data-test='error']")
    private WebElement errorMessage;

    @FindBy(className = "login_logo")
    private WebElement loginLogo;



    @FindBy(css = ".login_wrapper")
    private WebElement loginWrapper;

    // ✅ Public getter methods
    public WebElement getUsernameField() {
        return usernameField;
    }

    public WebElement getPasswordField() {
        return passwordField;
    }

    public WebElement getLoginButton() {
        return loginButton;
    }

    public WebElement getErrorMessage() {
        return errorMessage;
    }

    public WebElement getLoginLogo() {
        return loginLogo;
    }

    public WebElement getLoginWrapper() {
        return loginWrapper;
    }
}