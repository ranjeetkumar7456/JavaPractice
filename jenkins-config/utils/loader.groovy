// ===================================================
// Dynamic Script Loader
// ===================================================

class ScriptLoader {
    
    def logger
    def cache = [:]
    
    ScriptLoader() {
        logger = loadLogger()
    }
    
    // Load any Groovy script
    def loadScript(String scriptPath) {
        if (cache.containsKey(scriptPath)) {
            logger.debug("Loading from cache: ${scriptPath}")
            return cache[scriptPath]
        }
        
        try {
            logger.info("Loading script: ${scriptPath}")
            def script = load(scriptPath)
            cache[scriptPath] = script
            return script
            
        } catch (FileNotFoundException e) {
            logger.error("Script not found: ${scriptPath}", e)
            throw new Exception("Script not found: ${scriptPath}")
            
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
    
    // Load stage
    def loadStage(String stageName) {
        def stagePath = "jenkins-config/stages/${stageName}.groovy"
        return loadScript(stagePath)
    }
    
    // Load shared utility
    def loadSharedUtility(String utilName) {
        def utilPath = "jenkins-config/stages/shared/${utilName}.groovy"
        return loadScript(utilPath)
    }
    
    // Load logger
    def loadLogger() {
        return loadScript('jenkins-config/utils/logger.groovy')
    }
    
    // Load validator
    def loadValidator() {
        return loadScript('jenkins-config/utils/validator.groovy')
    }
    
    // Load notifier
    def loadNotifier() {
        return loadScript('jenkins-config/utils/notifier.groovy')
    }
    
    // Clear cache
    def clearCache() {
        cache.clear()
        logger.info("Script cache cleared")
    }
    
    // List loaded scripts
    def listLoadedScripts() {
        return cache.keySet().toList()
    }
    
    // Check if script exists
    def scriptExists(String scriptPath) {
        try {
            loadScript(scriptPath)
            return true
        } catch (Exception e) {
            return false
        }
    }
}

// Create and return instance
def loader = new ScriptLoader()

// Add convenience methods
def loadConfig = { loader.loadConfig() }
def loadStage = { stageName -> loader.loadStage(stageName) }
def loadSharedUtility = { utilName -> loader.loadSharedUtility(utilName) }
def logger = loader.logger
def validator = loader.loadValidator()
def notifier = loader.loadNotifier()

return [
    loadConfig: loadConfig,
    loadStage: loadStage,
    loadSharedUtility: loadSharedUtility,
    logger: logger,
    validator: validator,
    notifier: notifier,
    loader: loader
]
