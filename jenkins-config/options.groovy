// ===================================================
// Pipeline Options Configuration
// ===================================================

// Get pipeline options
def getPipelineOptions() {
    return {
        // Build retention
        buildDiscarder(logRotator(
            numToKeepStr: '50',
            artifactNumToKeepStr: '20',
            daysToKeepStr: '30',
            artifactDaysToKeepStr: '7'
        ))
        
        // Concurrency control
        disableConcurrentBuilds()
        
        // Timeout
        timeout(time: 120, unit: 'MINUTES')
        
        // Timestamps
        timestamps()
        
        // Color output
        ansiColor('xterm')
        
        // Durability
        durabilityHint('PERFORMANCE_OPTIMIZED')
        
        // Retry
        retry(2)
        
        // Skip stages after failure
        skipStagesAfterUnstable()
        
        // GitHub project
        githubProjectProperty(
            projectUrlStr: 'https://github.com/ranjeetkumar7456/JavaPractice'
        )
        
        // Build triggers
        // upstream(upstreamProjects: 'dependency-job', threshold: hudson.model.Result.SUCCESS)
    }
}

// Get options for specific phase
def getPhaseOptions(String phaseType) {
    def phaseOptions = [
        'SETUP': {
            timeout(time: 15, unit: 'MINUTES')
            retry(1)
        },
        'VALIDATION': {
            timeout(time: 10, unit: 'MINUTES')
        },
        'BUILD': {
            timeout(time: 20, unit: 'MINUTES')
            retry(1)
        },
        'TESTING': {
            timeout(time: 45, unit: 'MINUTES')
            retry(2)
        },
        'DEPLOYMENT': {
            timeout(time: 30, unit: 'MINUTES')
        },
        'ROLLBACK': {
            timeout(time: 15, unit: 'MINUTES')
        }
    ]
    
    return phaseOptions[phaseType] ?: {}
}

// Get notification options
def getNotificationOptions() {
    return {
        // Email notifications
        emailext(
            subject: '${DEFAULT_SUBJECT}',
            body: '${DEFAULT_CONTENT}',
            recipientProviders: [
                [$class: 'DevelopersRecipientProvider'],
                [$class: 'RequesterRecipientProvider']
            ],
            to: 'team@example.com'
        )
    }
}

// Get artifact archiving options
def getArchiveOptions() {
    return {
        // Archive artifacts
        archiveArtifacts(
            artifacts: '**/target/*.jar, **/reports/**/*, **/logs/**/*',
            fingerprint: true,
            allowEmptyArchive: false
        )
        
        // Archive test results
        junit(
            testResults: '**/target/surefire-reports/*.xml',
            allowEmptyResults: true,
            healthScaleFactor: 1.0
        )
    }
}

return this
