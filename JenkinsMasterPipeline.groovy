// ===================================================
// Jenkins Master Pipeline - Generic Controller
// ===================================================
/*
 * This is the main controller that orchestrates the entire pipeline.
 * It loads configurations and executes phases dynamically.
 */

class JenkinsMasterPipeline {
    
    def config
    def utils
    
    def executePipeline() {
        pipeline {
            agent any
            
            options {
                loadOptions()
            }
            
            environment {
                loadEnvironment()
            }
            
            parameters {
                choice(
                    name: 'DEPLOY_ENVIRONMENT',
                    choices: ['DEV', 'QA', 'STAGING', 'PROD'],
                    description: 'Select deployment environment'
                )
                booleanParam(
                    name: 'RUN_TESTS',
                    defaultValue: true,
                    description: 'Run automated tests'
                )
                booleanParam(
                    name: 'PERFORM_DEPLOYMENT',
                    defaultValue: true,
                    description: 'Perform deployment'
                )
                string(
                    name: 'CUSTOM_TAG',
                    defaultValue: '',
                    description: 'Custom tag for deployment'
                )
            }
            
            stages {
                stage('🚀 Initialize Pipeline') {
                    steps {
                        script {
                            initializePipeline()
                        }
                    }
                }
                
                stage('🔧 Phase 1: Environment Setup') {
                    steps {
                        script {
                            executePhase('phase1', 'Setup')
                        }
                    }
                }
                
                stage('🔍 Phase 2: Code Validation') {
                    steps {
                        script {
                            executePhase('phase2', 'Validation')
                        }
                    }
                }
                
                stage('⚙️ Phase 3: Build & Compile') {
                    steps {
                        script {
                            executePhase('phase3', 'Build')
                        }
                    }
                }
                
                stage('🧪 Phase 4: Testing') {
                    when {
                        expression { return params.RUN_TESTS == true }
                    }
                    steps {
                        script {
                            executeTestingPhase()
                        }
                    }
                }
                
                stage('🔄 Rollback Assessment') {
                    when {
                        expression { 
                            return env.TEST_PHASE_STATUS == 'FAILED' || 
                                   env.ROLLBACK_REQUIRED == 'true'
                        }
                    }
                    steps {
                        script {
                            assessRollback()
                        }
                    }
                }
                
                stage('📦 Phase 5: Deployment') {
                    when {
                        expression { 
                            return params.PERFORM_DEPLOYMENT == true &&
                                   env.ROLLBACK_REQUIRED != 'true' &&
                                   env.TEST_PHASE_STATUS == 'SUCCESS'
                        }
                    }
                    steps {
                        script {
                            executePhase('phase5', 'Deployment')
                        }
                    }
                }
                
                stage('📊 Generate Reports') {
                    steps {
                        script {
                            generateReports()
                        }
                    }
                }
            }
            
            post {
                always {
                    script {
                        finalizePipeline()
                    }
                }
                success {
                    script {
                        handleSuccess()
                    }
                }
                failure {
                    script {
                        handleFailure()
                    }
                }
                unstable {
                    script {
                        handleUnstable()
                    }
                }
                changed {
                    script {
                        handleChanged()
                    }
                }
            }
        }
    }
    
    // ========== PRIVATE METHODS ==========
    
    private void initializePipeline() {
        echo "🔧 Initializing Jenkins Master Pipeline..."
        
        // Load utilities
        utils = load('jenkins-config/utils/loader.groovy')
        utils.logger.info("Pipeline initialization started")
        
        // Load configuration
        config = [
            constants: load('jenkins-config/constants.groovy').getAllConstants(),
            env: load('jenkins-config/environment.groovy'),
            opts: load('jenkins-config/options.groovy')
        ]
        
        // Set initial environment
        config.env.setEnv('PIPELINE_STATUS', 'INITIALIZED')
        config.env.setEnv('START_TIME', new Date().format('yyyy-MM-dd HH:mm:ss'))
        config.env.setEnv('BUILD_NODE', env.NODE_NAME)
        config.env.setEnv('BUILD_USER', currentBuild.getBuildCauses()[0].userId ?: 'SYSTEM')
        
        utils.logger.success("Pipeline initialized successfully")
        utils.logger.info("Project: ${config.constants.project.name}")
        utils.logger.info("Version: ${config.constants.project.version}")
    }
    
    private void loadOptions() {
        def options = config?.opts?.getPipelineOptions()
        if (options) {
            options()
        }
    }
    
    private void loadEnvironment() {
        def envVars = config?.env?.getEnvironmentVariables()
        if (envVars) {
            return envVars
        }
        return [:]
    }
    
    private void executePhase(String phaseFile, String phaseName) {
        utils.logger.section("Starting Phase: ${phaseName}")
        
        try {
            // Update phase status
            config.env.updatePhaseStatus(phaseName.toUpperCase(), 'RUNNING')
            
            // Load and execute phase
            def phaseScript = utils.loadStage(phaseFile)
            phaseScript.execute(config, params)
            
            // Mark as success
            config.env.updatePhaseStatus(phaseName.toUpperCase(), 'SUCCESS')
            utils.logger.success("Phase ${phaseName} completed successfully")
            
        } catch (Exception e) {
            config.env.updatePhaseStatus(phaseName.toUpperCase(), 'FAILED', e.getMessage())
            utils.logger.error("Phase ${phaseName} failed", e)
            
            // Update pipeline status
            config.env.setEnv('PIPELINE_STATUS', 'FAILED')
            error("Phase ${phaseName} execution failed")
        }
    }
    
    private void executeTestingPhase() {
        utils.logger.section("Starting Testing Phase")
        
        try {
            config.env.setEnv('TEST_PHASE_STATUS', 'RUNNING')
            
            // Load testing phase
            def testPhase = utils.loadStage('phase4')
            testPhase.execute(config, params)
            
            // Check test results
            def testPercentage = config.env.getEnv('TEST_PASS_PERCENTAGE', '0').toFloat()
            def threshold = config.constants.test.passThreshold.toFloat()
            
            if (testPercentage >= threshold) {
                config.env.setEnv('TEST_PHASE_STATUS', 'SUCCESS')
                utils.logger.success("Tests passed with ${testPercentage}% (Threshold: ${threshold}%)")
            } else {
                config.env.setEnv('TEST_PHASE_STATUS', 'FAILED')
                utils.logger.warning("Tests failed with ${testPercentage}% (Threshold: ${threshold}%)")
                
                // Check retry count
                def retryCount = config.env.getEnv('TEST_RETRY_COUNT', '0').toInteger()
                def maxRetries = config.constants.test.maxRetryCount.toInteger()
                
                if (retryCount >= maxRetries) {
                    config.env.setEnv('ROLLBACK_REQUIRED', 'true')
                    config.env.setEnv('ROLLBACK_REASON', 
                        "Tests consistently below threshold (${retryCount}/${maxRetries} retries)")
                }
            }
            
        } catch (Exception e) {
            config.env.setEnv('TEST_PHASE_STATUS', 'FAILED')
            utils.logger.error("Testing phase failed", e)
            error("Testing phase execution failed")
        }
    }
    
    private void assessRollback() {
        utils.logger.section("Assessing Rollback Requirement")
        
        if (env.ROLLBACK_REQUIRED == 'true') {
            utils.logger.warning("Rollback required: ${env.ROLLBACK_REASON}")
            
            try {
                def rollbackScript = utils.loadStage('rollback')
                rollbackScript.execute(config, params)
                
                config.env.setEnv('PIPELINE_STATUS', 'ROLLBACK_EXECUTED')
                currentBuild.result = 'UNSTABLE'
                
            } catch (Exception e) {
                utils.logger.error("Rollback assessment failed", e)
                config.env.setEnv('PIPELINE_STATUS', 'ROLLBACK_FAILED')
                error("Rollback assessment failed")
            }
        }
    }
    
    private void generateReports() {
        utils.logger.section("Generating Reports")
        
        try {
            def dashboardScript = utils.loadStage('dashboard')
            dashboardScript.generate(config, params)
            
            utils.logger.success("Reports generated successfully")
            
        } catch (Exception e) {
            utils.logger.error("Report generation failed", e)
        }
    }
    
    private void finalizePipeline() {
        utils.logger.section("Finalizing Pipeline")
        
        // Calculate duration
        def startTime = config.env.getEnv('START_TIME')
        def endTime = new Date().format('yyyy-MM-dd HH:mm:ss')
        config.env.setEnv('END_TIME', endTime)
        
        // Update final status if not already set
        if (!env.PIPELINE_STATUS || env.PIPELINE_STATUS == 'INITIALIZED') {
            def allPhasesSuccess = [
                env.PHASE_SETUP_STATUS,
                env.PHASE_VALIDATION_STATUS,
                env.PHASE_BUILD_STATUS,
                env.TEST_PHASE_STATUS,
                env.PHASE_DEPLOYMENT_STATUS
            ].every { it == 'SUCCESS' || it == 'SKIPPED' }
            
            config.env.setEnv('PIPELINE_STATUS', allPhasesSuccess ? 'SUCCESS' : 'FAILED')
        }
        
        utils.logger.info("Pipeline Status: ${env.PIPELINE_STATUS}")
        utils.logger.info("Start Time: ${startTime}")
        utils.logger.info("End Time: ${endTime}")
    }
    
    private void handleSuccess() {
        utils.logger.success("🎉 Pipeline completed successfully!")
        utils.notifier.sendSuccessNotification(config, params)
    }
    
    private void handleFailure() {
        utils.logger.error("❌ Pipeline failed!")
        utils.notifier.sendFailureNotification(config, params)
    }
    
    private void handleUnstable() {
        utils.logger.warning("⚠️ Pipeline completed with warnings")
        utils.notifier.sendUnstableNotification(config, params)
    }
    
    private void handleChanged() {
        utils.logger.info("Pipeline status changed from previous build")
    }
}

// Create instance and return
return new JenkinsMasterPipeline()
