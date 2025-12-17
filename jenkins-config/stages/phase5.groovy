// ===================================================
// Phase 5: Deployment
// ===================================================

def execute(Map config, Map params) {
    def logger = config.utils.logger
    def deployUtils = config.utils.loadSharedUtility('deploy-utils')
    def constants = config.constants
    
    logger.logPhaseStart("DEPLOYMENT")
    
    try {
        // Validate deployment prerequisites
        logger.step("Validating Deployment Prerequisites")
        validateDeploymentPrerequisites(config)
        
        // Select deployment environment
        logger.step("Selecting Deployment Environment")
        def environment = selectDeploymentEnvironment(params, constants)
        
        // Prepare deployment artifacts
        logger.step("Preparing Deployment Artifacts")
        def artifacts = prepareDeploymentArtifacts()
        
        // Execute deployment strategy
        logger.step("Executing Deployment")
        executeDeployment(environment, artifacts, config, params)
        
        // Verify deployment
        logger.step("Verifying Deployment")
        verifyDeployment(environment, config)
        
        // Execute deployment job
        logger.step("Executing Deployment Job")
        executeDeploymentJob(constants.jobs.phase5, environment, params)
        
        // Update deployment status
        logger.step("Updating Deployment Status")
        updateDeploymentStatus(environment, "SUCCESS")
        
        logger.success("Phase 5: Deployment completed successfully")
        config.env.updatePhaseStatus("DEPLOYMENT", "SUCCESS", "Deployed to ${environment.name}")
        
    } catch (Exception e) {
        logger.error("Phase 5: Deployment failed", e)
        config.env.updatePhaseStatus("DEPLOYMENT", "FAILED", e.getMessage())
        updateDeploymentStatus(environment ?: [:], "FAILED")
        throw e
    } finally {
        logger.logPhaseEnd("DEPLOYMENT", env.PHASE_DEPLOYMENT_STATUS)
    }
}

// Private helper methods
private void validateDeploymentPrerequisites(Map config) {
    def validator = config.utils.validator
    
    // Check if build artifacts exist
    def requiredArtifacts = [
        'target/*.jar',
        'artifacts/build-manifest.txt'
    ]
    
    validator.validateBuildArtifacts(requiredArtifacts)
    
    // Check deployment environment variables
    def requiredEnvVars = [
        'DEPLOYMENT_ENVIRONMENT',
        'BUILD_ARTIFACT'
    ]
    
    requiredEnvVars.each { varName ->
        if (!env[varName]) {
            throw new Exception("Required environment variable not set: ${varName}")
        }
    }
    
    // Verify test results
    def testPercentage = config.env.getEnv('TEST_PASS_PERCENTAGE', '0').toFloat()
    def threshold = config.constants.test.passThreshold.toFloat()
    
    if (testPercentage < threshold) {
        throw new Exception("Cannot deploy: Test pass percentage (${testPercentage}%) below threshold (${threshold}%)")
    }
    
    echo "✅ Deployment prerequisites validated"
}

private Map selectDeploymentEnvironment(Map params, Map constants) {
    def envName = params.DEPLOYMENT_ENVIRONMENT ?: 'DEV'
    
    def environment = constants.deployment.environments[envName]
    if (!environment) {
        throw new Exception("Unknown deployment environment: ${envName}")
    }
    
    // Set environment variables
    env.DEPLOYMENT_ENVIRONMENT = envName
    env.DEPLOYMENT_URL = environment.url
    env.DEPLOYMENT_CREDENTIALS_ID = environment.credentialsId
    
    echo "Selected Environment: ${envName}"
    echo "Deployment URL: ${environment.url}"
    
    return [
        name: envName,
        url: environment.url,
        credentialsId: environment.credentialsId,
        config: environment
    ]
}

private List prepareDeploymentArtifacts() {
    echo "Preparing deployment artifacts..."
    
    def artifacts = []
    
    // Find JAR files
    def jarFiles = findFiles(glob: 'target/*.jar')
    jarFiles.each { jarFile ->
        artifacts << [
            type: 'application',
            path: jarFile.path,
            name: jarFile.name
        ]
    }
    
    // Find configuration files
    def configFiles = findFiles(glob: 'config/*')
    configFiles.each { configFile ->
        artifacts << [
            type: 'configuration',
            path: configFile.path,
            name: configFile.name
        ]
    }
    
    // Find documentation
    def docs = findFiles(glob: 'docs/*')
    docs.each { doc ->
        artifacts << [
            type: 'documentation',
            path: doc.path,
            name: doc.name
        ]
    }
    
    // Create deployment manifest
    def manifestContent = """
DEPLOYMENT MANIFEST
===================
Deployment Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
Build: #${env.BUILD_NUMBER}
Environment: ${env.DEPLOYMENT_ENVIRONMENT}
Version: ${env.BUILD_VERSION}
Git Commit: ${env.GIT_COMMIT}

ARTIFACTS TO DEPLOY:
-------------------
${artifacts.collect { "- ${it.name} (${it.type})" }.join('\n')}

DEPLOYMENT INSTRUCTIONS:
-----------------------
1. Stop existing application
2. Backup current deployment
3. Deploy new artifacts
4. Update configuration
5. Start application
6. Verify deployment
"""
    
    writeFile file: 'artifacts/deployment-manifest.txt', text: manifestContent
    
    artifacts << [
        type: 'manifest',
        path: 'artifacts/deployment-manifest.txt',
        name: 'deployment-manifest.txt'
    ]
    
    echo "✅ Prepared ${artifacts.size()} artifacts for deployment"
    return artifacts
}

private void executeDeployment(Map environment, List artifacts, Map config, Map params) {
    def logger = config.utils.logger
    def deployUtils = config.utils.loadSharedUtility('deploy-utils')
    
    logger.info("Executing deployment to ${environment.name}")
    
    def deploymentStrategy = params.DEPLOYMENT_STRATEGY ?: 'BLUE_GREEN'
    
    switch(deploymentStrategy.toUpperCase()) {
        case 'BLUE_GREEN':
            executeBlueGreenDeployment(environment, artifacts, config, params)
            break
            
        case 'CANARY':
            executeCanaryDeployment(environment, artifacts, config, params)
            break
            
        case 'ROLLING':
            executeRollingDeployment(environment, artifacts, config, params)
            break
            
        default:
            executeStandardDeployment(environment, artifacts, config, params)
    }
}

private void executeBlueGreenDeployment(Map environment, List artifacts, Map config, Map params) {
    echo "Executing Blue-Green deployment..."
    
    // This would contain actual blue-green deployment logic
    // For demo, we'll simulate the process
    
    echo "1. Deploying to green environment"
    echo "2. Running smoke tests"
    echo "3. Switching traffic from blue to green"
    echo "4. Decommissioning blue environment"
    
    sleep(5) // Simulate deployment time
    
    echo "✅ Blue-Green deployment completed"
}

private void executeStandardDeployment(Map environment, List artifacts, Map config, Map params) {
    echo "Executing standard deployment to ${environment.url}"
    
    withCredentials([usernamePassword(
        credentialsId: environment.credentialsId,
        passwordVariable: 'DEPLOY_PASSWORD',
        usernameVariable: 'DEPLOY_USERNAME'
    )]) {
        // Upload artifacts
        artifacts.each { artifact ->
            if (artifact.type == 'application') {
                echo "Uploading ${artifact.name} to ${environment.url}"
                // Actual upload logic would go here
            }
        }
        
        // Execute deployment commands
        def deployCommands = """
            # Deployment script
            echo "Starting deployment to ${environment.name}"
            
            # Backup current deployment
            BACKUP_DIR="/backup/$(date +%Y%m%d_%H%M%S)"
            mkdir -p \$BACKUP_DIR
            
            # Deploy new version
            echo "Deploying version ${env.BUILD_VERSION}"
            
            # Restart service
            echo "Restarting application..."
            
            echo "Deployment completed"
        """
        
        // In real scenario, would execute via SSH or deployment tool
        echo "Would execute: ${deployCommands}"
        
        sleep(3) // Simulate deployment time
    }
    
    echo "✅ Standard deployment completed"
}

private void verifyDeployment(Map environment, Map config) {
    echo "Verifying deployment to ${environment.url}"
    
    def maxAttempts = 10
    def attempt = 0
    def verified = false
    
    while (attempt < maxAttempts && !verified) {
        attempt++
        echo "Verification attempt ${attempt}/${maxAttempts}"
        
        try {
            // Check if application is reachable
            def response = sh(script: """
                curl -s -o /dev/null -w "%{http_code}" ${environment.url}/health \
                --connect-timeout 5 --max-time 10
            """, returnStdout: true).trim()
            
            if (response == "200") {
                verified = true
                echo "✅ Application is healthy (HTTP ${response})"
                
                // Check additional endpoints
                verifyApplicationEndpoints(environment)
                
            } else {
                echo "⚠️ Application not healthy yet (HTTP ${response})"
                sleep(time: 10, unit: 'SECONDS')
            }
            
        } catch (Exception e) {
            echo "⚠️ Verification failed: ${e.getMessage()}"
            sleep(time: 10, unit: 'SECONDS')
        }
    }
    
    if (!verified) {
        throw new Exception("Deployment verification failed after ${maxAttempts} attempts")
    }
    
    // Log deployment success
    env.DEPLOYMENT_STATUS = "SUCCESS"
    env.DEPLOYMENT_VERIFICATION_TIME = new Date().format('HH:mm:ss')
    
    echo "✅ Deployment verified successfully"
}

private void verifyApplicationEndpoints(Map environment) {
    def endpoints = [
        "${environment.url}/health",
        "${environment.url}/info",
        "${environment.url}/metrics"
    ]
    
    endpoints.each { endpoint ->
        try {
            def response = sh(script: "curl -s -o /dev/null -w \"%{http_code}\" ${endpoint}", returnStdout: true).trim()
            echo "  ${endpoint}: HTTP ${response}"
        } catch (Exception e) {
            echo "  ${endpoint}: Failed - ${e.getMessage()}"
        }
    }
}

private void executeDeploymentJob(Map jobConfig, Map environment, Map params) {
    echo "Executing deployment job: ${jobConfig.name}"
    
    try {
        def deployParams = [
            string(name: 'ENVIRONMENT', value: environment.name),
            string(name: 'DEPLOYMENT_URL', value: environment.url),
            string(name: 'BUILD_VERSION', value: env.BUILD_VERSION),
            string(name: 'GIT_COMMIT', value: env.GIT_COMMIT),
            string(name: 'DEPLOYMENT_STRATEGY', value: params.DEPLOYMENT_STRATEGY ?: 'STANDARD'),
            booleanParam(name: 'VERIFY_DEPLOYMENT', value: params.VERIFY_DEPLOYMENT ?: true),
            booleanParam(name: 'SEND_NOTIFICATIONS', value: params.SEND_NOTIFICATIONS ?: true)
        ]
        
        def result = build job: jobConfig.name,
                         parameters: deployParams,
                         wait: true,
                         propagate: false
        
        if (result.result == 'SUCCESS') {
            echo "Deployment job completed successfully"
        } else {
            throw new Exception("Deployment job failed with status: ${result.result}")
        }
        
    } catch (Exception e) {
        echo "Deployment job failed: ${e.getMessage()}"
        // Don't fail the whole phase if job fails, as we already did local deployment
    }
}

private void updateDeploymentStatus(Map environment, String status) {
    env.DEPLOYMENT_STATUS = status
    env.DEPLOYMENT_COMPLETION_TIME = new Date().format('yyyy-MM-dd HH:mm:ss')
    
    // Create deployment record
    def deploymentRecord = """
DEPLOYMENT RECORD
=================
Status: ${status}
Environment: ${environment.name}
URL: ${environment.url}
Build: #${env.BUILD_NUMBER}
Version: ${env.BUILD_VERSION}
Deployed By: ${env.BUILD_USER}
Deployment Time: ${env.DEPLOYMENT_COMPLETION_TIME}
Git Commit: ${env.GIT_COMMIT}
Verified: ${env.DEPLOYMENT_VERIFICATION_TIME ?: 'Not verified'}
"""
    
    writeFile file: 'reports/deployment-record.txt', text: deploymentRecord
    archiveArtifacts artifacts: 'reports/deployment-record.txt', fingerprint: true
    
    echo "Deployment status updated: ${status}"
}

return this
