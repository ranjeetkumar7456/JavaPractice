// ========== PIPELINE OPTIONS ==========

def getPipelineOptions() {
    return {
        // Time and Build Management
        timestamps()
        timeout(time: 120, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(
            numToKeepStr: '20',
            artifactNumToKeepStr: '10'
        ))
        
        // Visual Enhancements
        ansiColor('xterm')
        durabilityHint('PERFORMANCE_OPTIMIZED')
        
        // Retry Configuration
        retry(3)
        
        // Parallel Execution
        parallelsAlwaysFailFast()
    }
}

// Get options for specific phase
def getPhaseOptions(String phaseType) {
    def optionsMap = [
        'SETUP': {
            timeout(time: 15, unit: 'MINUTES')
        },
        'TEST': {
            timeout(time: 45, unit: 'MINUTES')
            retry(2)
        },
        'DEPLOY': {
            timeout(time: 30, unit: 'MINUTES')
        }
    ]
    
    return optionsMap[phaseType] ?: {}
}

return this
