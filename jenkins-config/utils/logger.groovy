// ===================================================
// Enhanced Logging Utility
// ===================================================

class PipelineLogger {
    
    def colors = [
        reset: "\u001B[0m",
        red: "\u001B[31m",
        green: "\u001B[32m",
        yellow: "\u001B[33m",
        blue: "\u001B[34m",
        magenta: "\u001B[35m",
        cyan: "\u001B[36m",
        white: "\u001B[37m"
    ]
    
    def logLevels = [
        DEBUG: 0,
        INFO: 1,
        WARN: 2,
        ERROR: 3
    ]
    
    def currentLogLevel = logLevels.INFO
    
    // Set log level
    def setLogLevel(String level) {
        if (logLevels.containsKey(level)) {
            currentLogLevel = logLevels[level]
            echo "${colors.cyan}Log level set to: ${level}${colors.reset}"
        }
    }
    
    // Log methods
    def debug(String message) {
        if (currentLogLevel <= logLevels.DEBUG) {
            echo "${colors.blue}🔍 DEBUG: ${message}${colors.reset}"
        }
    }
    
    def info(String message) {
        if (currentLogLevel <= logLevels.INFO) {
            echo "${colors.cyan}📘 INFO: ${message}${colors.reset}"
        }
    }
    
    def success(String message) {
        if (currentLogLevel <= logLevels.INFO) {
            echo "${colors.green}✅ SUCCESS: ${message}${colors.reset}"
        }
    }
    
    def warning(String message) {
        if (currentLogLevel <= logLevels.WARN) {
            echo "${colors.yellow}⚠️ WARNING: ${message}${colors.reset}"
        }
    }
    
    def error(String message, Exception e = null) {
        if (currentLogLevel <= logLevels.ERROR) {
            echo "${colors.red}❌ ERROR: ${message}${colors.reset}"
            if (e) {
                echo "${colors.red}   Exception: ${e.getClass().getName()} - ${e.getMessage()}${colors.reset}"
                if (env.DEBUG_MODE == 'true') {
                    echo "${colors.red}   Stack trace: ${e.getStackTrace().join('\n   ')}${colors.reset}"
                }
            }
        }
    }
    
    // Section logging
    def section(String title) {
        echo ""
        echo "${colors.magenta}========================================${colors.reset}"
        echo "${colors.magenta}📋 ${title}${colors.reset}"
        echo "${colors.magenta}========================================${colors.reset}"
        echo ""
    }
    
    def step(String stepName) {
        echo "${colors.cyan}▶️ STEP: ${stepName}${colors.reset}"
    }
    
    // Phase logging
    def logPhaseStart(String phaseName) {
        section("STARTING PHASE: ${phaseName}")
        env["${phaseName}_START_TIME"] = new Date().format('HH:mm:ss')
    }
    
    def logPhaseEnd(String phaseName, String status) {
        def endTime = new Date().format('HH:mm:ss')
        def startTime = env["${phaseName}_START_TIME"] ?: "N/A"
        
        echo "${colors.cyan}📊 PHASE SUMMARY: ${phaseName}${colors.reset}"
        echo "   Status: ${getStatusIcon(status)} ${status}"
        echo "   Start: ${startTime}"
        echo "   End: ${endTime}"
        echo ""
        
        env["${phaseName}_END_TIME"] = endTime
    }
    
    def logPipelineStart() {
        section("🚀 JENKINS PIPELINE STARTED")
        echo "Job: ${env.JOB_NAME}"
        echo "Build: #${env.BUILD_NUMBER}"
        echo "Node: ${env.NODE_NAME}"
        echo "Workspace: ${env.WORKSPACE}"
        echo ""
    }
    
    def logPipelineCompletion() {
        section("🏁 PIPELINE EXECUTION COMPLETED")
        echo "Overall Status: ${env.PIPELINE_STATUS ?: 'UNKNOWN'}"
        echo "Start Time: ${env.START_TIME ?: 'N/A'}"
        echo "End Time: ${env.END_TIME ?: 'N/A'}"
        echo "Duration: ${env.DURATION ?: 'N/A'}"
        echo ""
        
        // Log phase statuses
        echo "PHASE STATUSES:"
        def phases = ['SETUP', 'VALIDATION', 'BUILD', 'TESTING', 'DEPLOYMENT']
        phases.each { phase ->
            def status = env["PHASE_${phase}_STATUS"] ?: 'PENDING'
            echo "  ${phase}: ${getStatusIcon(status)} ${status}"
        }
    }
    
    // Performance logging
    def logPerformance(String metric, String value, String unit = "") {
        if (currentLogLevel <= logLevels.DEBUG) {
            def unitStr = unit ? " ${unit}" : ""
            echo "${colors.blue}📈 PERFORMANCE: ${metric} = ${value}${unitStr}${colors.reset}"
        }
    }
    
    // Helper methods
    private String getStatusIcon(String status) {
        switch(status.toUpperCase()) {
            case 'SUCCESS': return '✅'
            case 'FAILED': return '❌'
            case 'RUNNING': return '🔄'
            case 'PENDING': return '⏳'
            case 'SKIPPED': return '⏭️'
            case 'UNSTABLE': return '⚠️'
            default: return '📝'
        }
    }
    
    // Table logging
    def logTable(String title, Map data) {
        if (currentLogLevel <= logLevels.INFO) {
            echo "${colors.cyan}${title}:${colors.reset}"
            data.each { key, value ->
                echo "  ${key.padRight(20)}: ${value}"
            }
            echo ""
        }
    }
}

// Create and return instance
def logger = new PipelineLogger()

// Export methods
return [
    debug: { message -> logger.debug(message) },
    info: { message -> logger.info(message) },
    success: { message -> logger.success(message) },
    warning: { message -> logger.warning(message) },
    error: { message, e = null -> logger.error(message, e) },
    section: { title -> logger.section(title) },
    step: { stepName -> logger.step(stepName) },
    logPhaseStart: { phaseName -> logger.logPhaseStart(phaseName) },
    logPhaseEnd: { phaseName, status -> logger.logPhaseEnd(phaseName, status) },
    logPipelineStart: { -> logger.logPipelineStart() },
    logPipelineCompletion: { -> logger.logPipelineCompletion() },
    logPerformance: { metric, value, unit = "" -> logger.logPerformance(metric, value, unit) },
    logTable: { title, data -> logger.logTable(title, data) },
    setLogLevel: { level -> logger.setLogLevel(level) }
]
