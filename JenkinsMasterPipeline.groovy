// Jenkins Master Pipeline - Generic and Reusable
def execute() {
    pipeline {
        agent any
        
        // Load options from config
        options loadOptions()
        
        // Set environment variables
        environment loadEnvironment()
        
        stages {
            // Load and execute all stages dynamically
            stage('🚀 Load Pipeline Configuration') {
                steps {
                    script {
                        loadPipelineConfig()
                    }
                }
            }
            
            // Dynamic phase execution
            stage('🔧 Execute Phase 1: Setup') {
                steps {
                    script {
                        executePhase('phase1', 'PHASE_1')
                    }
                }
            }
            
            stage('🔍 Execute Phase 2: Validation') {
                steps {
                    script {
                        executePhase('phase2', 'PHASE_2')
                    }
                }
            }
            
            stage('⚙️ Execute Phase 3: Compilation') {
                steps {
                    script {
                        executePhase('phase3', 'PHASE_3')
                    }
                }
            }
            
            stage('🧪 Execute Phase 4: Testing') {
                steps {
                    script {
                        executeTestingPhase()
                    }
                }
            }
            
            stage('🔄 Rollback Decision') {
                when {
                    expression { return env.ROLLBACK_REQUIRED == 'true' }
                }
                steps {
                    script {
                        executeRollback()
                    }
                }
            }
            
            stage('📦 Execute Phase 5: Deployment') {
                when {
                    expression { 
                        return env.ROLLBACK_REQUIRED != 'true' && 
                               env.TEST_PHASE_STATUS == 'SUCCESS'
                    }
                }
                steps {
                    script {
                        executePhase('phase5', 'PHASE_5')
                    }
                }
            }
            
            stage('📊 Generate Dashboard Report') {
                steps {
                    script {
                        generateDashboard()
                    }
                }
            }
        }
        
        post {
            always {
                script {
                    loadUtils().logger.logPipelineCompletion()
                }
            }
            success {
                script {
                    loadUtils().notifier.sendSuccessNotification()
                }
            }
            failure {
                script {
                    loadUtils().notifier.sendFailureNotification()
                }
            }
            unstable {
                script {
                    loadUtils().notifier.sendUnstableNotification()
                }
            }
        }
    }
}

// ========== HELPER METHODS ==========

def loadOptions() {
    def optionsConfig = load('jenkins-config/options.groovy')
    return optionsConfig.getPipelineOptions()
}

def loadEnvironment() {
    def envConfig = load('jenkins-config/environment.groovy')
    return envConfig.getEnvironmentVariables()
}

def loadPipelineConfig() {
    echo "📁 Loading Pipeline Configuration..."
    
    // Load all configuration
    def constants = load('jenkins-config/constants.groovy').getAllConstants()
    def envVars = load('jenkins-config/environment.groovy').getEnvironmentVariables()
    
    // Set initial status
    env.PIPELINE_STATUS = "INITIALIZED"
    env.START_TIME = new Date().format('yyyy-MM-dd HH:mm:ss')
    
    echo "✅ Pipeline Configuration Loaded"
    echo "Project: ${constants.PROJECT_NAME}"
    echo "Version: ${constants.PIPELINE_VERSION}"
}

def loadUtils() {
    return load('jenkins-config/utils/loader.groovy')
}

def executePhase(phaseFile, phaseKey) {
    echo "▶️ Starting ${phaseKey}..."
    
    try {
        // Update phase status
        env["${phaseKey}_STATUS"] = "RUNNING"
        
        // Load and execute phase
        def phaseScript = load("jenkins-config/stages/${phaseFile}.groovy")
        phaseScript.execute(loadConfig())
        
        // Mark as success
        env["${phaseKey}_STATUS"] = "SUCCESS"
        echo "✅ ${phaseKey} completed successfully"
        
    } catch (Exception e) {
        env["${phaseKey}_STATUS"] = "FAILED"
        env.PIPELINE_STATUS = "FAILED"
        echo "❌ ${phaseKey} failed: ${e.getMessage()}"
        error("Phase execution failed")
    }
}

def executeTestingPhase() {
    echo "🧪 Starting Testing Phase..."
    
    try {
        env.TEST_PHASE_STATUS = "RUNNING"
        
        // Load testing phase
        def testPhase = load('jenkins-config/stages/phase4.groovy')
        def config = loadConfig()
        
        // Execute with retry logic
        testPhase.executeWithRetry(config)
        
        // Check if rollback required
        if (env.TEST_PASS_PERCENTAGE.toFloat() < config.constants.PASS_THRESHOLD.toFloat() &&
            env.TEST_RETRY_COUNT.toInteger() >= config.constants.MAX_RETRY_COUNT.toInteger()) {
            env.ROLLBACK_REQUIRED = "true"
            env.TEST_PHASE_STATUS = "FAILED"
        } else {
            env.TEST_PHASE_STATUS = "SUCCESS"
        }
        
    } catch (Exception e) {
        env.TEST_PHASE_STATUS = "FAILED"
        error("Testing phase failed: ${e.getMessage()}")
    }
}

def executeRollback() {
    echo "🔄 Executing Rollback Procedure..."
    
    try {
        def rollbackScript = load('jenkins-config/stages/rollback.groovy')
        def config = loadConfig()
        
        rollbackScript.execute(config)
        
        env.PIPELINE_STATUS = "ROLLBACK_EXECUTED"
        echo "✅ Rollback completed"
        
    } catch (Exception e) {
        env.PIPELINE_STATUS = "ROLLBACK_FAILED"
        error("Rollback failed: ${e.getMessage()}")
    }
}

def generateDashboard() {
    echo "📊 Generating Dashboard Report..."
    
    try {
        def dashboardScript = load('jenkins-config/stages/dashboard.groovy')
        def config = loadConfig()
        
        dashboardScript.generate(config)
        
        echo "✅ Dashboard generated successfully"
        
    } catch (Exception e) {
        echo "⚠️ Dashboard generation failed: ${e.getMessage()}"
    }
}

def loadConfig() {
    return [
        constants: load('jenkins-config/constants.groovy').getAllConstants(),
        environment: load('jenkins-config/environment.groovy').getEnvironmentVariables(),
        utils: loadUtils()
    ]
}

return this
