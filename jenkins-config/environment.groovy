// ========== ENVIRONMENT VARIABLES ==========

def getEnvironmentVariables() {
    return [
        // Pipeline Status
        PIPELINE_STATUS: "INITIALIZED",
        START_TIME: "",
        END_TIME: "",
        DURATION: "",
        
        // Phase Status
        PHASE_1_STATUS: "PENDING",
        PHASE_2_STATUS: "PENDING",
        PHASE_3_STATUS: "PENDING", 
        TEST_PHASE_STATUS: "PENDING",
        PHASE_5_STATUS: "PENDING",
        
        // Test Results
        TEST_PASS_PERCENTAGE: "0",
        TEST_TOTAL_COUNT: "0",
        TEST_PASSED_COUNT: "0",
        TEST_FAILED_COUNT: "0",
        TEST_RETRY_COUNT: "0",
        
        // Rollback Control
        ROLLBACK_REQUIRED: "false",
        ROLLBACK_REASON: "",
        PREVIOUS_COMMIT: "",
        
        // Build Information
        BUILD_NODE: "",
        BUILD_USER: "",
        BUILD_PARAMETERS: "",
        
        // Git Information
        GIT_COMMIT: "",
        GIT_BRANCH: "",
        GIT_URL: "",
        
        // Performance Metrics
        MEMORY_USAGE: "",
        DISK_USAGE: "",
        BUILD_DURATION: ""
    ]
}

// Set environment variable with validation
def setEnv(String key, String value) {
    if (key && value) {
        env[key] = value
        return true
    }
    return false
}

// Get environment variable with default
def getEnv(String key, String defaultValue = "") {
    return env[key] ?: defaultValue
}

// Update phase status
def updatePhaseStatus(String phase, String status, String message = "") {
    def statusKey = "${phase}_STATUS"
    env[statusKey] = status
    
    if (message) {
        env["${phase}_MESSAGE"] = message
    }
}

// Get all phase statuses
def getPhaseStatuses() {
    return [
        phase1: env.PHASE_1_STATUS,
        phase2: env.PHASE_2_STATUS,
        phase3: env.PHASE_3_STATUS,
        testing: env.TEST_PHASE_STATUS,
        phase5: env.PHASE_5_STATUS
    ]
}

return this
