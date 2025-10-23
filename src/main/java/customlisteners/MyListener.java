package customlisteners;

import ConfigUtil.ExtentReportManager;  // ✅ Correct import
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.AdvancedLogger;

/**
 * Advanced Test Listener for comprehensive test lifecycle management
 */
public class MyListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        AdvancedLogger.info("🎯 Test Suite Started: " + context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = result.getMethod().getMethodName();

        String[] groups = result.getMethod().getGroups();
        String groupsList = groups.length > 0 ? String.join(",", groups) : "Default";

        // ✅ Now this method exists in ExtentReportManager
        ExtentReportManager.createTest(simpleClassName, methodName, groupsList, "AutomationTeam");
        AdvancedLogger.info("🧪 Test Started: " + simpleClassName + " => " + methodName);

        // Log test description if available
        if (result.getMethod().getDescription() != null) {
            ExtentReportManager.info("📝 Test Description: " + result.getMethod().getDescription());
        }
    }

    // ... rest of your methods remain same
}