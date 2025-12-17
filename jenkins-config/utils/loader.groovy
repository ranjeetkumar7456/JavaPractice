// ========== DYNAMIC LOADER UTILITIES ==========

def logger = load('jenkins-config/utils/logger.groovy')
def validator = load('jenkins-config/utils/validator.groovy')
def notifier = load('jenkins-config/utils/notifier.groovy')

// Load any Groovy script
def loadScript(String scriptPath) {
    try {
        logger.debug("Loading script: ${scriptPath}")
        return load(scriptPath)
    } catch (Exception e) {
        logger.error("Failed to load script: ${scriptPath}", e)
        throw e
    }
}

// Load configuration
def loadConfig() {
    return [
        constants: loadScript('jenkins-config/constants.groovy').getAllConstants(),
        environment: loadScript('jenkins-config/environment.groovy'),
        options: loadScript('jenkins-config/options.groovy')
    ]
}

// Load stage dynamically
def loadStage(String stageName) {
    def stagePath = "jenkins-config/stages/${stageName}.groovy"
    return loadScript(stagePath)
}

// Load shared utilities
def loadSharedUtils(String utilName) {
    def utilPath = "jenkins-config/stages/shared/${utilName}.groovy"
    return loadScript(utilPath)
}

// Check if file exists
def scriptExists(String path) {
    try {
        loadScript(path)
        return true
    } catch (Exception e) {
        return false
    }
}

return this
