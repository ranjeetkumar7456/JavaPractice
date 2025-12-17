// ===================================================
// Pipeline Constants Configuration
// ===================================================

// Project Information
PROJECT_INFO = [
    name: "Java8Feature",
    description: "Java 8 Features Automation Project",
    version: "2.0.0",
    owner: "Automation Team",
    repository: "JavaPractice"
]

// Test Configuration
TEST_CONFIG = [
    passThreshold: 70,
    maxRetryCount: 5,
    minTestCount: 1,
    timeoutMinutes: 45,
    reportFormats: ["junit", "html", "json"]
]

// Job Configuration
JOB_CONFIG = [
    phase1: [
        name: "JavaPractice-Phase1",
        description: "Environment Setup",
        timeout: 15
    ],
    phase2: [
        name: "JavaPractice-Phase2",
        description: "Code Validation",
        timeout: 10
    ],
    phase3: [
        name: "JavaPractice-Phase3",
        description: "Build & Compilation",
        timeout: 20
    ],
    phase4: [
        name: "JavaPractice-Phase4",
        description: "Test Execution",
        timeout: 45
    ],
    phase5: [
        name: "JavaPractice-Phase5",
        description: "Deployment",
        timeout: 30
    ]
]

// Path Configuration
PATH_CONFIG = [
    source: "src/main/java",
    tests: "src/test/java",
    resources: "src/main/resources",
    testResources: "src/test/resources",
    reports: [
        test: "target/surefire-reports",
        coverage: "target/site/jacoco",
        checkstyle: "target/checkstyle-result.xml"
    ],
    artifacts: [
        build: "target/*.jar",
        libs: "target/libs/*.jar",
        docs: "target/site/*"
    ]
]

// Git Configuration
GIT_CONFIG = [
    repository: "https://github.com/ranjeetkumar7456/JavaPractice.git",
    branch: "main",
    credentialsId: "github-credentials",
    userName: "Jenkins",
    userEmail: "jenkins@example.com"
]

// Notification Configuration
NOTIFICATION_CONFIG = [
    email: [
        recipients: "team@example.com",
        cc: "manager@example.com",
        onSuccess: true,
        onFailure: true,
        onUnstable: true
    ],
    slack: [
        channel: "#jenkins-notifications",
        enabled: true,
        onSuccess: true,
        onFailure: true
    ]
]

// Deployment Configuration
DEPLOYMENT_CONFIG = [
    environments: [
        DEV: [
            url: "http://dev.example.com",
            credentialsId: "dev-credentials"
        ],
        QA: [
            url: "http://qa.example.com",
            credentialsId: "qa-credentials"
        ],
        STAGING: [
            url: "http://staging.example.com",
            credentialsId: "staging-credentials"
        ],
        PROD: [
            url: "http://prod.example.com",
            credentialsId: "prod-credentials"
        ]
    ],
    strategies: [
        BLUE_GREEN: "blue-green",
        CANARY: "canary",
        ROLLING: "rolling"
    ]
]

// Return all constants
def getAllConstants() {
    return [
        project: PROJECT_INFO,
        test: TEST_CONFIG,
        jobs: JOB_CONFIG,
        paths: PATH_CONFIG,
        git: GIT_CONFIG,
        notifications: NOTIFICATION_CONFIG,
        deployment: DEPLOYMENT_CONFIG
    ]
}

// Get specific constant
def getConstant(category, subcategory = null, key = null) {
    def allConstants = getAllConstants()
    
    if (!subcategory && !key) {
        return allConstants[category]
    }
    
    if (subcategory && !key) {
        return allConstants[category][subcategory]
    }
    
    if (subcategory && key) {
        return allConstants[category][subcategory][key]
    }
    
    return null
}

return this
