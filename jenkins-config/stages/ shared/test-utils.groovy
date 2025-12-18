// ===================================================
// Test Utilities
// ===================================================

def parseTestResults(String testResultPath) {
    echo "Parsing test results from: ${testResultPath}"
    
    try {
        // Use Jenkins JUnit plugin to parse test results
        def testResults = junit testResults: testResultPath, allowEmptyResults: true
        
        return [
            totalCount: testResults.totalCount,
            passCount: testResults.passCount,
            failCount: testResults.failCount,
            skipCount: testResults.skipCount,
            duration: testResults.duration,
            suites: testResults.suites
        ]
        
    } catch (Exception e) {
        echo "⚠️ Failed to parse test results: ${e.getMessage()}"
        
        // Return empty results
        return [
            totalCount: 0,
            passCount: 0,
            failCount: 0,
            skipCount: 0,
            duration: 0,
            suites: []
        ]
    }
}

def calculateTestMetrics(Map testResults) {
    def total = testResults.totalCount ?: 0
    def passed = testResults.passCount ?: 0
    
    return [
        passPercentage: total > 0 ? (passed / total * 100) : 0,
        totalTests: total,
        passedTests: passed,
        failedTests: testResults.failCount ?: 0,
        skippedTests: testResults.skipCount ?: 0
    ]
}

def generateTestReport(Map testResults, String reportPath) {
    def metrics = calculateTestMetrics(testResults)
    
    def reportContent = """
TEST EXECUTION REPORT
=====================

SUMMARY:
--------
Total Tests: ${metrics.totalTests}
Passed: ${metrics.passedTests}
Failed: ${metrics.failedTests}
Skipped: ${metrics.skippedTests}
Pass Percentage: ${String.format("%.2f", metrics.passPercentage)}%

DETAILED RESULTS:
-----------------
${getDetailedTestInfo(testResults)}

TIMING INFORMATION:
------------------
Total Duration: ${testResults.duration ?: 0} seconds

RECOMMENDATIONS:
---------------
${getTestRecommendations(metrics)}
"""
    
    writeFile file: reportPath, text: reportContent
    return reportPath
}

private String getDetailedTestInfo(Map testResults) {
    if (!testResults.suites || testResults.suites.size() == 0) {
        return "No detailed test information available"
    }
    
    def details = []
    testResults.suites.each { suite ->
        details << "Suite: ${suite.name}"
        suite.cases.each { testCase ->
            def status = testCase.status == 'PASSED' ? '✅' : 
                        testCase.status == 'FAILED' ? '❌' : '⏭️'
            details << "  ${status} ${testCase.name} (${testCase.duration}s)"
        }
    }
    
    return details.join('\n')
}

private String getTestRecommendations(Map metrics) {
    def recommendations = []
    
    if (metrics.failedTests > 0) {
        recommendations << "- Investigate failing tests"
        recommendations << "- Check test environment setup"
        recommendations << "- Review test data"
    }
    
    if (metrics.skippedTests > 0) {
        recommendations << "- Review why tests were skipped"
        recommendations << "- Consider running skipped tests"
    }
    
    if (metrics.totalTests == 0) {
        recommendations << "- No tests executed, check test configuration"
        recommendations << "- Verify test source locations"
    }
    
    return recommendations.join('\n')
}

return this
