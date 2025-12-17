// ========== ENHANCED LOGGING UTILITIES ==========

def info(String message) {
    echo "📘 INFO: ${message}"
}

def success(String message) {
    echo "✅ SUCCESS: ${message}"
}

def warning(String message) {
    echo "⚠️ WARNING: ${message}"
}

def error(String message, Exception e = null) {
    echo "❌ ERROR: ${message}"
    if (e) {
        echo "Exception: ${e.getMessage()}"
    }
}

def debug(String message) {
    if (env.DEBUG_MODE == 'true') {
        echo "🔍 DEBUG: ${message}"
    }
}

def section(String title) {
    echo ""
    echo "========================================"
    echo "📋 ${title}"
    echo "========================================"
}

def step(String stepName) {
    echo "▶️ STEP: ${stepName}"
}

def logPhaseStart(String phaseName) {
    section("STARTING PHASE: ${phaseName}")
    env["${phaseName}_START_TIME"] = new Date().format('HH:mm:ss')
}

def logPhaseEnd(String phaseName, String status) {
    env["${phaseName}_END_TIME"] = new Date().format('HH:mm:ss')
    echo "📊 ${phaseName} - Status: ${status}"
    echo "   Start: ${env["${phaseName}_START_TIME"]}"
    echo "   End: ${env["${phaseName}_END_TIME"]}"
}

def logPipelineCompletion() {
    section("PIPELINE EXECUTION COMPLETED")
    echo "Overall Status: ${env.PIPELINE_STATUS}"
    echo "Start Time: ${env.START_TIME}"
    echo "End Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}"
}

return this
