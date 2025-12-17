// ========== PIPELINE CONSTANTS ==========

// Pipeline Metadata
PROJECT_NAME = "Java8Feature"
PIPELINE_VERSION = "2.0.0"
PIPELINE_AUTHOR = "Jenkins Automation Team"

// Test Configuration
PASS_THRESHOLD = 70
MAX_RETRY_COUNT = 5
MIN_TEST_COUNT = 1

// Job Configuration
JOBS = [
    PHASE1: "JavaPractice-Phase1",
    PHASE2: "JavaPractice-Phase2", 
    PHASE3: "JavaPractice-Phase3",
    PHASE4: "JavaPractice-Phase4",
    PHASE5: "JavaPractice-Phase5"
]

// Path Configuration
PATHS = [
    TEST_RESULTS: "target/surefire-reports/*.xml",
    BUILD_ARTIFACTS: "target/*.jar",
    REPORTS: "test-output/"
]

// Git Configuration
GIT_CONFIG = [
    REPO_URL: "https://github.com/ranjeetkumar7456/JavaPractice.git",
    BRANCH: "main",
    CREDENTIALS_ID: "github-credentials"
]

// Notification Configuration
NOTIFICATIONS = [
    EMAIL_TO: "team@example.com",
    EMAIL_CC: "manager@example.com",
    SLACK_CHANNEL: "#jenkins-notifications",
    ON_SUCCESS: true,
    ON_FAILURE: true,
    ON_UNSTABLE: true
]

// Timeout Configuration
TIMEOUTS = [
    PHASE: 30,    // minutes
    TEST: 45,     // minutes
    DEPLOY: 60    // minutes
]

// Return all constants
def getAllConstants() {
    return [
        project: PROJECT_NAME,
        version: PIPELINE_VERSION,
        author: PIPELINE_AUTHOR,
        
        testConfig: [
            passThreshold: PASS_THRESHOLD,
            maxRetryCount: MAX_RETRY_COUNT,
            minTestCount: MIN_TEST_COUNT
        ],
        
        jobs: JOBS,
        paths: PATHS,
        git: GIT_CONFIG,
        notifications: NOTIFICATIONS,
        timeouts: TIMEOUTS
    ]
}

// Get specific constant category
def getConstant(category, key = null) {
    def allConstants = getAllConstants()
    if (key) {
        return allConstants[category][key]
    }
    return allConstants[category]
}

return this
