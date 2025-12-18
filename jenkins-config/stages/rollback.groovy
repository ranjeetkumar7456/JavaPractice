// ===================================================
// Rollback Procedure with Full Integration
// ===================================================

def execute(Map config, Map params) {
    def logger = config.utils.logger
    def gitUtils = config.utils.loadSharedUtility('git-utils')
    def constants = config.constants
    
    logger.section("🚨 EXECUTING ROLLBACK PROCEDURE")
    
    try {
        // Step 1: Analyze current state
        logger.step("Analyzing Current State")
        def currentState = analyzeCurrentState(config)
        
        // Step 2: Determine rollback strategy
        logger.step("Determining Rollback Strategy")
        def strategy = determineRollbackStrategy(currentState, params)
        
        // Step 3: Get rollback target
        logger.step("Identifying Rollback Target")
        def rollbackTarget = identifyRollbackTarget(currentState, config)
        
        // Step 4: Execute Git rollback
        logger.step("Executing Git Rollback")
        executeGitRollback(rollbackTarget, config, strategy)
        
        // Step 5: Update deployment status
        logger.step("Updating Deployment Status")
        updateDeploymentStatusAfterRollback(currentState, rollbackTarget)
        
        // Step 6: Send notifications
        logger.step("Sending Rollback Notifications")
        sendRollbackNotifications(config, currentState, rollbackTarget, strategy)
        
        // Step 7: Generate rollback report
        logger.step("Generating Rollback Report")
        generateRollbackReport(currentState, rollbackTarget, strategy)
        
        // Step 8: Mark pipeline as unstable
        logger.step("Updating Pipeline Status")
        updatePipelineStatusAfterRollback()
        
        logger.success("✅ Rollback procedure completed successfully")
        
    } catch (Exception e) {
        logger.error("❌ Rollback procedure failed", e)
        config.env.setEnv("ROLLBACK_STATUS", "FAILED")
        config.env.setEnv("ROLLBACK_ERROR", e.getMessage())
        throw e
    }
}

// Private helper methods
private Map analyzeCurrentState(Map config) {
    def logger = config.utils.logger
    
    logger.info("Analyzing current pipeline state...")
    
    def state = [
        timestamp: new Date().format('yyyy-MM-dd HH:mm:ss'),
        buildInfo: getBuildInfo(),
        testResults: getTestResults(),
        gitState: getGitState(),
        deploymentState: getDeploymentState(),
        rollbackReason: config.env.getEnv('ROLLBACK_REASON', 'Unknown')
    ]
    
    logger.logTable("Current State Analysis", [
        "Build Number": state.buildInfo.buildNumber,
        "Test Percentage": "${state.testResults.passPercentage}%",
        "Retry Attempts": "${state.testResults.retryCount}/${config.constants.test.maxRetryCount}",
        "Current Commit": state.gitState.currentCommit?.take(8) ?: 'N/A',
        "Deployment Status": state.deploymentState.status ?: 'N/A',
        "Rollback Reason": state.rollbackReason
    ])
    
    return state
}

private Map getBuildInfo() {
    return [
        buildNumber: env.BUILD_NUMBER,
        jobName: env.JOB_NAME,
        node: env.NODE_NAME,
        workspace: env.WORKSPACE,
        startTime: env.START_TIME,
        pipelineStatus: env.PIPELINE_STATUS
    ]
}

private Map getTestResults() {
    return [
        passPercentage: env.TEST_PASS_PERCENTAGE ?: '0',
        totalTests: env.TEST_TOTAL_COUNT ?: '0',
        passedTests: env.TEST_PASSED_COUNT ?: '0',
        failedTests: env.TEST_FAILED_COUNT ?: '0',
        retryCount: env.TEST_RETRY_COUNT ?: '0'
    ]
}

private Map getGitState() {
    try {
        def currentCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
        def branch = sh(script: 'git branch --show-current', returnStdout: true).trim()
        def commitMessage = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
        def author = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()
        
        return [
            currentCommit: currentCommit,
            branch: branch,
            commitMessage: commitMessage,
            author: author,
            hasUncommittedChanges: hasUncommittedChanges()
        ]
    } catch (Exception e) {
        return [error: e.getMessage()]
    }
}

private boolean hasUncommittedChanges() {
    def status = sh(script: 'git status --porcelain', returnStdout: true).trim()
    return !status.empty
}

private Map getDeploymentState() {
    return [
        status: env.DEPLOYMENT_STATUS,
        environment: env.DEPLOYMENT_ENVIRONMENT,
        url: env.DEPLOYMENT_URL,
        timestamp: env.DEPLOYMENT_COMPLETION_TIME
    ]
}

private String determineRollbackStrategy(Map currentState, Map params) {
    def strategy = params.ROLLBACK_STRATEGY ?: 'GIT_REVERT'
    
    // Auto-determine strategy based on conditions
    if (!currentState.gitState.currentCommit) {
        strategy = 'FULL_RESET'
    } else if (currentState.deploymentState.status == 'SUCCESS') {
        strategy = 'DEPLOYMENT_ROLLBACK'
    }
    
    echo "Selected rollback strategy: ${strategy}"
    return strategy
}

private Map identifyRollbackTarget(Map currentState, Map config) {
    def logger = config.utils.logger
    logger.info("Identifying rollback target...")
    
    def target = [
        type: 'COMMIT',
        identifier: '',
        reason: '',
        timestamp: ''
    ]
    
    // Try to find previous successful commit
    def previousSuccessCommit = findPreviousSuccessfulCommit()
    
    if (previousSuccessCommit) {
        target.identifier = previousSuccessCommit
        target.reason = "Previous successful build"
        target.timestamp = getCommitTimestamp(previousSuccessCommit)
    } else {
        // Fallback to previous commit
        target.identifier = sh(script: 'git rev-parse HEAD~1', returnStdout: true).trim()
        target.reason = "Previous commit"
        target.timestamp = getCommitTimestamp(target.identifier)
    }
    
    logger.info("Rollback target identified: ${target.identifier.take(8)}")
    logger.info("Reason: ${target.reason}")
    logger.info("Timestamp: ${target.timestamp}")
    
    return target
}

private String findPreviousSuccessfulCommit() {
    try {
        // Look for commit with successful build tag or message
        def commit = sh(script: '''
            git log --oneline --grep="BUILD SUCCESS" --grep="Deployment successful" -n 1 | head -1 | cut -d" " -f1
        ''', returnStdout: true).trim()
        
        if (!commit) {
            // Look for tags
            commit = sh(script: 'git describe --tags --abbrev=0 2>/dev/null || true', returnStdout: true).trim()
        }
        
        return commit ?: ''
    } catch (Exception e) {
        return ''
    }
}

private String getCommitTimestamp(String commitHash) {
    try {
        return sh(script: "git show -s --format=%ci ${commitHash}", returnStdout: true).trim()
    } catch (Exception e) {
        return 'Unknown'
    }
}

private void executeGitRollback(Map rollbackTarget, Map config, String strategy) {
    def logger = config.utils.logger
    def gitUtils = config.utils.loadSharedUtility('git-utils')
    
    logger.info("Executing Git rollback using strategy: ${strategy}")
    
    // Store current commit before rollback
    env.PRE_ROLLBACK_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    env.ROLLBACK_TARGET_COMMIT = rollbackTarget.identifier
    env.ROLLBACK_STRATEGY = strategy
    env.ROLLBACK_EXECUTION_TIME = new Date().format('yyyy-MM-dd HH:mm:ss')
    
    try {
        switch(strategy.toUpperCase()) {
            case 'GIT_REVERT':
                executeGitRevert(rollbackTarget, config)
                break
                
            case 'GIT_RESET':
                executeGitReset(rollbackTarget, config)
                break
                
            case 'FULL_RESET':
                executeFullReset(config)
                break
                
            case 'DEPLOYMENT_ROLLBACK':
                executeDeploymentRollback(config)
                break
                
            default:
                executeGitRevert(rollbackTarget, config)
        }
        
        // Verify rollback
        verifyRollbackExecution(rollbackTarget)
        
        config.env.setEnv("ROLLBACK_STATUS", "EXECUTED")
        logger.success("Git rollback executed successfully")
        
    } catch (Exception e) {
        config.env.setEnv("ROLLBACK_STATUS", "GIT_FAILED")
        throw e
    }
}

private void executeGitRevert(Map rollbackTarget, Map config) {
    def gitConfig = config.constants.git
    
    withCredentials([usernamePassword(
        credentialsId: gitConfig.credentialsId,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        sh """
            # Configure Git
            git config user.email "${gitConfig.userEmail}"
            git config user.name "${gitConfig.userName}"
            
            # Create revert commit
            echo "Creating revert commit..."
            git revert ${rollbackTarget.identifier}..HEAD --no-edit
            
            # Push changes
            echo "Pushing revert commit..."
            git push origin HEAD:${gitConfig.branch}
        """
    }
    
    echo "Git revert completed for commit: ${rollbackTarget.identifier.take(8)}"
}

private void executeGitReset(Map rollbackTarget, Map config) {
    def gitConfig = config.constants.git
    
    withCredentials([usernamePassword(
        credentialsId: gitConfig.credentialsId,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        sh """
            # Configure Git
            git config user.email "${gitConfig.userEmail}"
            git config user.name "${gitConfig.userName}"
            
            # Hard reset to target commit
            echo "Resetting to commit: ${rollbackTarget.identifier}"
            git reset --hard ${rollbackTarget.identifier}
            
            # Force push
            echo "Force pushing changes..."
            git push origin HEAD:${gitConfig.branch} --force
        """
    }
    
    echo "Git reset completed to commit: ${rollbackTarget.identifier.take(8)}"
}

private void executeFullReset(Map config) {
    def gitConfig = config.constants.git
    
    echo "Performing full repository reset..."
    
    withCredentials([usernamePassword(
        credentialsId: gitConfig.credentialsId,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        sh """
            # Remove all local changes
            echo "Cleaning workspace..."
            git clean -fd
            
            # Reset to origin
            echo "Resetting to origin/${gitConfig.branch}..."
            git fetch origin
            git reset --hard origin/${gitConfig.branch}
        """
    }
    
    echo "Full reset completed"
}

private void executeDeploymentRollback(Map config) {
    echo "Executing deployment rollback..."
    
    // This would contain actual deployment rollback logic
    // For demo, we'll log what would happen
    
    echo "1. Identifying current deployment"
    echo "2. Rolling back to previous deployment version"
    echo "3. Verifying rollback deployment"
    echo "4. Updating deployment records"
    
    sleep(3) // Simulate rollback time
    
    echo "Deployment rollback completed"
}

private void verifyRollbackExecution(Map rollbackTarget) {
    echo "Verifying rollback execution..."
    
    def currentCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    
    // Check if we're at the target commit (for reset) or have revert commit
    def logOutput = sh(script: 'git log --oneline -5', returnStdout: true)
    
    if (logOutput.contains("Revert") || logOutput.contains(rollbackTarget.identifier.take(8))) {
        echo "✅ Rollback verified successfully"
        env.POST_ROLLBACK_COMMIT = currentCommit
        env.ROLLBACK_VERIFICATION_STATUS = "SUCCESS"
    } else {
        throw new Exception("Rollback verification failed")
    }
}

private void updateDeploymentStatusAfterRollback(Map currentState, Map rollbackTarget) {
    env.DEPLOYMENT_STATUS = "ROLLED_BACK"
    env.DEPLOYMENT_ROLLBACK_TIME = new Date().format('yyyy-MM-dd HH:mm:ss')
    env.DEPLOYMENT_ROLLBACK_REASON = currentState.rollbackReason
    
    echo "Deployment status updated to: ROLLED_BACK"
}

private void sendRollbackNotifications(Map config, Map currentState, Map rollbackTarget, String strategy) {
    def logger = config.utils.logger
    def notifier = config.utils.notifier
    
    logger.info("Preparing rollback notifications...")
    
    def notificationData = [
        project: config.constants.project.name,
        buildNumber: currentState.buildInfo.buildNumber,
        rollbackReason: currentState.rollbackReason,
        testPercentage: currentState.testResults.passPercentage,
        retryCount: currentState.testResults.retryCount,
        previousCommit: env.PRE_ROLLBACK_COMMIT?.take(8) ?: 'N/A',
        rollbackTarget: rollbackTarget.identifier.take(8),
        strategy: strategy,
        executionTime: env.ROLLBACK_EXECUTION_TIME,
        buildUrl: env.BUILD_URL
    ]
    
    notifier.sendRollbackNotification(config, [:], currentState.rollbackReason)
    
    logger.success("Rollback notifications sent")
}

private void generateRollbackReport(Map currentState, Map rollbackTarget, String strategy) {
    def reportContent = """
ROLLBACK EXECUTION REPORT
=========================
Generated: ${new Date().format('yyyy-MM-dd HH:mm:ss')}

ROLLBACK SUMMARY:
----------------
Status: ${env.ROLLBACK_STATUS ?: 'EXECUTED'}
Strategy: ${strategy}
Execution Time: ${env.ROLLBACK_EXECUTION_TIME}
Verified: ${env.ROLLBACK_VERIFICATION_STATUS ?: 'PENDING'}

REASON FOR ROLLBACK:
-------------------
${currentState.rollbackReason}

TEST RESULTS (Triggering Rollback):
----------------------------------
Pass Percentage: ${currentState.testResults.passPercentage}%
Total Tests: ${currentState.testResults.totalTests}
Passed: ${currentState.testResults.passedTests}
Failed: ${currentState.testResults.failedTests}
Retry Attempts: ${currentState.testResults.retryCount}

GIT OPERATIONS:
--------------
Previous Commit: ${env.PRE_ROLLBACK_COMMIT?.take(8) ?: 'N/A'}
Rollback Target: ${rollbackTarget.identifier.take(8)}
Target Reason: ${rollbackTarget.reason}
Target Timestamp: ${rollbackTarget.timestamp}
Post-Rollback Commit: ${env.POST_ROLLBACK_COMMIT?.take(8) ?: 'N/A'}

DEPLOYMENT STATUS:
-----------------
Status: ${env.DEPLOYMENT_STATUS ?: 'N/A'}
Environment: ${env.DEPLOYMENT_ENVIRONMENT ?: 'N/A'}
Rollback Time: ${env.DEPLOYMENT_ROLLBACK_TIME ?: 'N/A'}

BUILD INFORMATION:
-----------------
Build Number: ${currentState.buildInfo.buildNumber}
Job: ${currentState.buildInfo.jobName}
Node: ${currentState.buildInfo.node}
Build URL: ${env.BUILD_URL}

ACTIONS TAKEN:
--------------
1. ${getActionDescription(strategy)}
2. Deployment status updated
3. Notifications sent
4. Report generated

NEXT STEPS:
----------
1. Investigate test failures
2. Fix identified issues
3. Create new build with fixes
4. Monitor next deployment

ROLLBACK VERIFICATION:
---------------------
${getVerificationDetails()}
"""
    
    writeFile file: 'reports/rollback-execution-report.txt', text: reportContent
    archiveArtifacts artifacts: 'reports/rollback-execution-report.txt', fingerprint: true
    
    echo "✅ Rollback report generated: reports/rollback-execution-report.txt"
}

private String getActionDescription(String strategy) {
    switch(strategy.toUpperCase()) {
        case 'GIT_REVERT': return "Git revert executed to rollback commit"
        case 'GIT_RESET': return "Git hard reset performed"
        case 'FULL_RESET': return "Full repository reset executed"
        case 'DEPLOYMENT_ROLLBACK': return "Deployment rollback performed"
        default: return "Standard rollback executed"
    }
}

private String getVerificationDetails() {
    if (env.ROLLBACK_VERIFICATION_STATUS == 'SUCCESS') {
        return "✅ Rollback verified successfully"
    } else {
        return "⚠️ Rollback verification pending or failed"
    }
}

private void updatePipelineStatusAfterRollback() {
    // Mark pipeline as unstable
    currentBuild.result = 'UNSTABLE'
    
    // Update environment variables
    env.PIPELINE_STATUS = "ROLLBACK_EXECUTED"
    env.PIPELINE_FINAL_STATUS = "UNSTABLE_WITH_ROLLBACK"
    
    // Skip deployment phase
    env.PHASE_DEPLOYMENT_STATUS = "SKIPPED"
    env.PHASE_DEPLOYMENT_MESSAGE = "Skipped due to rollback"
    
    echo "Pipeline marked as UNSTABLE due to rollback"
}

return this
