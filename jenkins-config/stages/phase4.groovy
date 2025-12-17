// ===================================================
// Phase 4: Testing with Retry Logic
// ===================================================

def execute(Map config, Map params) {
    def logger = config.utils.logger
    def testUtils = config.utils.loadSharedUtility('test-utils')
    def constants = config.constants
    
    logger.logPhaseStart("TESTING")
    
    // Initialize test variables
    config.env.setEnv('TEST_RETRY_COUNT', '0')
    config.env.setEnv('TEST_PASS_PERCENTAGE', '0')
    config.env.setEnv('TEST_TOTAL_COUNT', '0')
    config.env.setEnv('TEST_PASSED_COUNT', '0')
    config.env.setEnv('TEST_FAILED_COUNT', '0')
    config.env.setEnv('TEST_SKIPPED_COUNT', '0')
    
    def maxRetries = constants.test.maxRetryCount.toInteger()
    def passThreshold = constants.test.passThreshold.toFloat()
    def currentRetry = 0
    def testsPassed = false
    
    try {
        while (currentRetry <= maxRetries && !testsPassed) {
            currentRetry++
            config.env.setEnv('TEST_RETRY_COUNT', currentRetry.toString())
            
            logger.info("Test Execution Attempt ${currentRetry}/${maxRetries}")
            
            try {
                // Execute tests
                def testResults = executeTests(config, params, currentRetry)
                
                // Analyze results
                analyzeTestResults(testResults, config)
                
                // Check if passed threshold
                def passPercentage = config.env.getEnv('TEST_PASS_PERCENTAGE', '0').toFloat()
                if (passPercentage >= passThreshold) {
                    testsPassed = true
                    logger.success("✅ Tests passed threshold on attempt ${currentRetry} (${passPercentage}% >= ${passThreshold}%)")
                    config.env.updatePhaseStatus("TESTING", "SUCCESS", 
                        "Passed with ${passPercentage}% on attempt ${currentRetry}")
                } else {
                    logger.warning("⚠️ Tests below threshold: ${passPercentage}% < ${passThreshold}%")
                    
                    if (currentRetry < maxRetries) {
                        logger.info("Preparing for retry ${currentRetry + 1}...")
                        prepareForRetry(config, currentRetry)
                    } else {
                        logger.error("❌ Max retries reached (${maxRetries})")
                        config.env.setEnv('ROLLBACK_REQUIRED', 'true')
                        config.env.setEnv('ROLLBACK_REASON', 
                            "Tests consistently below ${passThreshold}% after ${maxRetries} attempts")
                        config.env.updatePhaseStatus("TESTING", "FAILED", 
                            "Failed to meet threshold after ${maxRetries} attempts")
                    }
                }
                
            } catch (Exception e) {
                logger.error("Test execution failed on attempt ${currentRetry}", e)
                
                if (currentRetry >= maxRetries) {
                    config.env.updatePhaseStatus("TESTING", "FAILED", e.getMessage())
                    config.env.setEnv('ROLLBACK_REQUIRED', 'true')
                    config.env.setEnv('ROLLBACK_REASON', "Test execution failed after ${maxRetries} attempts")
                    throw e
                } else {
                    logger.info("Will retry test execution...")
                }
            }
        }
        
        if (!testsPassed) {
            config.env.updatePhaseStatus("TESTING", "FAILED", 
                "Failed to meet threshold after ${maxRetries} attempts")
            config.env.setEnv('ROLLBACK_REQUIRED', 'true')
            config.env.setEnv('ROLLBACK_REASON', "Tests consistently below ${passThreshold}%")
        }
        
    } catch (Exception e) {
        logger.error("Testing phase failed", e)
        config.env.updatePhaseStatus("TESTING", "FAILED", e.getMessage())
        throw e
    } finally {
        logger.logPhaseEnd("TESTING", env.TEST_PHASE_STATUS)
    }
}

// Private helper methods
private Map executeTests(Map config, Map params, int attemptNumber) {
    def logger = config.utils.logger
    def testUtils = config.utils.loadSharedUtility('test-utils')
    
    logger.step("Executing Tests (Attempt ${attemptNumber})")
    
    try {
        // Execute test job
        def jobResult = build job: config.constants.jobs.phase4.name,
                             parameters: getTestParameters(params, attemptNumber),
                             wait: true,
                             propagate: false
        
        if (jobResult.result != 'SUCCESS') {
            throw new Exception("Test job failed with status: ${jobResult.result}")
        }
        
        // Parse test results
        def testResults = testUtils.parseTestResults(config.constants.paths.reports.test)
        
        return testResults
        
    } catch (Exception e) {
        // If test job fails, try to run tests locally
        logger.warning("Test job failed, attempting local test execution")
        return executeTestsLocally(config, params)
    }
}

private Map getTestParameters(Map params, int attemptNumber) {
    def testParams = [
        string(name: 'BRANCH', value: env.GIT_BRANCH),
        string(name: 'COMMIT', value: env.GIT_COMMIT),
        string(name: 'TEST_CATEGORY', value: params.TEST_CATEGORY ?: ''),
        string(name: 'TEST_ENVIRONMENT', value: params.TEST_ENVIRONMENT ?: 'DEV'),
        booleanParam(name: 'RUN_INTEGRATION_TESTS', value: params.RUN_INTEGRATION_TESTS ?: false),
        booleanParam(name: 'RUN_PERFORMANCE_TESTS', value: params.RUN_PERFORMANCE_TESTS ?: false),
        string(name: 'RETRY_ATTEMPT', value: attemptNumber.toString())
    ]
    
    // Add test tags if specified
    if (params.TEST_TAGS) {
        testParams << string(name: 'TEST_TAGS', value: params.TEST_TAGS)
    }
    
    return testParams
}

private Map executeTestsLocally(Map config, Map params) {
    echo "Executing tests locally..."
    
    def testCommand = 'mvn test'
    
    if (params.TEST_CATEGORY) {
        testCommand += " -Dtest=${params.TEST_CATEGORY}"
    }
    
    if (params.RUN_INTEGRATION_TESTS == true) {
        testCommand += ' verify'
    }
    
    try {
        sh testCommand
        
        // Parse test results
        def testUtils = config.utils.loadSharedUtility('test-utils')
        return testUtils.parseTestResults(config.constants.paths.reports.test)
        
    } catch (Exception e) {
        // Generate test report even if tests fail
        sh 'mvn surefire-report:report-only'
        throw e
    }
}

private void analyzeTestResults(Map testResults, Map config) {
    def logger = config.utils.logger
    
    def total = testResults.totalCount ?: 0
    def passed = testResults.passCount ?: 0
    def failed = testResults.failCount ?: 0
    def skipped = testResults.skipCount ?: 0
    
    // Calculate percentages
    def passPercentage = total > 0 ? (passed / total * 100) : 0
    
    // Update environment
    config.env.setEnv('TEST_TOTAL_COUNT', total.toString())
    config.env.setEnv('TEST_PASSED_COUNT', passed.toString())
    config.env.setEnv('TEST_FAILED_COUNT', failed.toString())
    config.env.setEnv('TEST_SKIPPED_COUNT', skipped.toString())
    config.env.setEnv('TEST_PASS_PERCENTAGE', String.format("%.2f", passPercentage))
    
    // Log results
    logger.info("Test Results Summary:")
    logger.info("  Total Tests: ${total}")
    logger.info("  Passed: ${passed}")
    logger.info("  Failed: ${failed}")
    logger.info("  Skipped: ${skipped}")
    logger.info("  Pass Percentage: ${String.format("%.2f", passPercentage)}%")
    
    // Generate detailed report
    generateTestReport(testResults, config)
}

private void generateTestReport(Map testResults, Map config) {
    def reportContent = """
TEST EXECUTION REPORT
=====================
Generated: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
Build: #${env.BUILD_NUMBER}
Retry Attempt: ${env.TEST_RETRY_COUNT}

SUMMARY:
--------
Total Tests: ${testResults.totalCount ?: 0}
Passed: ${testResults.passCount ?: 0}
Failed: ${testResults.failCount ?: 0}
Skipped: ${testResults.skipCount ?: 0}
Pass Percentage: ${env.TEST_PASS_PERCENTAGE}%
Required Threshold: ${config.constants.test.passThreshold}%

DETAILED RESULTS:
-----------------
${getDetailedTestResults(testResults)}

RECOMMENDATIONS:
---------------
${getTestRecommendations(testResults)}
"""
    
    def reportFile = "reports/test-report-attempt-${env.TEST_RETRY_COUNT}.txt"
    writeFile file: reportFile, text: reportContent
    archiveArtifacts artifacts: reportFile, fingerprint: true
    
    // Also archive test result files
    archiveArtifacts artifacts: 'target/surefire-reports/*', fingerprint: true
}

private String getDetailedTestResults(Map testResults) {
    if (!testResults || testResults.totalCount == 0) {
        return "No test results available"
    }
    
    def details = []
    
    // Add passed tests
    if (testResults.passCount > 0) {
        details << "PASSED TESTS (${testResults.passCount}):"
        // In real implementation, would list actual test names
        details << "  - All tests passed successfully"
    }
    
    // Add failed tests
    if (testResults.failCount > 0) {
        details << "\nFAILED TESTS (${testResults.failCount}):"
        // In real implementation, would list failed test names
        details << "  - Check test reports for detailed failure information"
    }
    
    // Add skipped tests
    if (testResults.skipCount > 0) {
        details << "\nSKIPPED TESTS (${testResults.skipCount}):"
        details << "  - Tests were skipped due to various reasons"
    }
    
    return details.join('\n')
}

private String getTestRecommendations(Map testResults) {
    def recommendations = []
    
    if (testResults.failCount > 0) {
        recommendations << "- Investigate and fix failing tests"
        recommendations << "- Check test environment configuration"
        recommendations << "- Review test dependencies"
    }
    
    if (testResults.skipCount > 0) {
        recommendations << "- Review why tests were skipped"
        recommendations << "- Consider running skipped tests in appropriate environments"
    }
    
    if (testResults.totalCount == 0) {
        recommendations << "- No tests were executed. Check test configuration."
        recommendations << "- Verify test sources are correctly placed"
    }
    
    return recommendations.join('\n')
}

private void prepareForRetry(Map config, int currentAttempt) {
    def logger = config.utils.logger
    
    logger.info("Preparing for retry attempt ${currentAttempt + 1}")
    
    try {
        // Clean test reports
        sh 'rm -rf target/surefire-reports/* 2>/dev/null || true'
        
        // Reset test database if applicable
        if (params.RESET_TEST_DB == true) {
            logger.info("Resetting test database...")
            // Add database reset logic here
        }
        
        // Clear caches
        sh 'mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false'
        
        // Wait before retry
        sleep(time: 5, unit: 'SECONDS')
        
        logger.info("Ready for retry attempt ${currentAttempt + 1}")
        
    } catch (Exception e) {
        logger.warning("Failed to prepare for retry: ${e.getMessage()}")
    }
}

return this
