// ===================================================
// Environment Variables Configuration
// ===================================================

// Define all environment variables
def getEnvironmentVariables() {
    return [
        // Pipeline Status
        PIPELINE_STATUS: "INITIALIZED",
        PIPELINE_VERSION: "2.0.0",
        
        // Timestamps
        START_TIME: "",
        END_TIME: "",
        DURATION: "",
        
        // Phase Status
        PHASE_SETUP_STATUS: "PENDING",
        PHASE_VALIDATION_STATUS: "PENDING",
        PHASE_BUILD_STATUS: "PENDING",
        TEST_PHASE_STATUS: "PENDING",
        PHASE_DEPLOYMENT_STATUS: "PENDING",
        
        // Phase Messages
        PHASE_SETUP_MESSAGE: "",
        PHASE_VALIDATION_MESSAGE: "",
        PHASE_BUILD_MESSAGE: "",
        TEST_PHASE_MESSAGE: "",
        PHASE_DEPLOYMENT_MESSAGE: "",
        
        // Test Results
        TEST_PASS_PERCENTAGE: "0",
        TEST_TOTAL_COUNT: "0",
        TEST_PASSED_COUNT: "0",
        TEST_FAILED_COUNT: "0",
        TEST_SKIPPED_COUNT: "0",
        TEST_RETRY_COUNT: "0",
        
        // Build Information
        BUILD_NUMBER: "${env.BUILD_NUMBER}",
        BUILD_ID: "${env.BUILD_ID}",
        BUILD_TAG: "${env.BUILD_TAG}",
        BUILD_URL: "${env.BUILD_URL}",
        JOB_NAME: "${env.JOB_NAME}",
        JOB_BASE_NAME: "${env.JOB_BASE_NAME}",
        BUILD_DISPLAY_NAME: "${env.BUILD_DISPLAY_NAME}",
        
        // Node Information
        NODE_NAME: "${env.NODE_NAME}",
        WORKSPACE: "${env.WORKSPACE}",
        
        // Git Information
        GIT_COMMIT: "",
        GIT_PREVIOUS_COMMIT: "",
        GIT_PREVIOUS_SUCCESSFUL_COMMIT: "",
        GIT_BRANCH: "",
        GIT_URL: "",
        GIT_AUTHOR: "",
        GIT_COMMIT_MESSAGE: "",
        
        // Rollback Information
        ROLLBACK_REQUIRED: "false",
        ROLLBACK_REASON: "",
        ROLLBACK_STATUS: "",
        ROLLBACK_TIMESTAMP: "",
        
        // Deployment Information
        DEPLOYMENT_ENVIRONMENT: "",
        DEPLOYMENT_STATUS: "",
        DEPLOYMENT_URL: "",
        
        // Performance Metrics
        BUILD_DURATION_MS: "",
        MEMORY_USAGE_MB: "",
        DISK_USAGE_GB: "",
        CPU_USAGE_PERCENT: ""
    ]
}

// Set environment variable with validation
def setEnv(String key, String value) {
    if (!key || !value) {
        throw new IllegalArgumentException("Key and value cannot be null or empty")
    }
    
    // Validate key exists in schema
    def envSchema = getEnvironmentVariables()
    if (!envSchema.containsKey(key)) {
        echo "⚠️ Warning: Setting non-standard environment variable: ${key}"
    }
    
    env[key] = value
    return true
}

// Get environment variable with default
def getEnv(String key, String defaultValue = "") {
    return env[key] ?: defaultValue
}

// Update phase status
def updatePhaseStatus(String phase, String status, String message = "") {
    def statusKey = "PHASE_${phase.toUpperCase()}_STATUS"
    def messageKey = "PHASE_${phase.toUpperCase()}_MESSAGE"
    
    setEnv(statusKey, status)
    if (message) {
        setEnv(messageKey, message)
    }
    
    echo "📝 Phase ${phase} status updated: ${status}"
    if (message) {
        echo "   Message: ${message}"
    }
}

// Get all phase statuses
def getPhaseStatuses() {
    return [
        setup: getEnv("PHASE_SETUP_STATUS"),
        validation: getEnv("PHASE_VALIDATION_STATUS"),
        build: getEnv("PHASE_BUILD_STATUS"),
        testing: getEnv("TEST_PHASE_STATUS"),
        deployment: getEnv("PHASE_DEPLOYMENT_STATUS")
    ]
}

// Calculate and set pipeline duration
def calculateDuration() {
    def startTime = getEnv("START_TIME")
    if (!startTime) return
    
    def startDate = Date.parse("yyyy-MM-dd HH:mm:ss", startTime)
    def endDate = new Date()
    def duration = endDate.getTime() - startDate.getTime()
    
    // Convert to readable format
    def minutes = (duration / (1000 * 60)).toInteger()
    def seconds = ((duration % (1000 * 60)) / 1000).toInteger()
    
    setEnv("END_TIME", endDate.format("yyyy-MM-dd HH:mm:ss"))
    setEnv("DURATION", "${minutes}m ${seconds}s")
    setEnv("BUILD_DURATION_MS", duration.toString())
}

// Reset environment for retry
def resetForRetry() {
    echo "🔄 Resetting environment for retry..."
    
    // Reset test-related variables
    setEnv("TEST_PASS_PERCENTAGE", "0")
    setEnv("TEST_TOTAL_COUNT", "0")
    setEnv("TEST_PASSED_COUNT", "0")
    setEnv("TEST_FAILED_COUNT", "0")
    setEnv("TEST_SKIPPED_COUNT", "0")
    
    // Increment retry count
    def retryCount = getEnv("TEST_RETRY_COUNT", "0").toInteger() + 1
    setEnv("TEST_RETRY_COUNT", retryCount.toString())
    
    echo "Retry count: ${retryCount}"
}

return this
