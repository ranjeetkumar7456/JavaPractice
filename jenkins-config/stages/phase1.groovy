// ===================================================
// Phase 1: Environment Setup & Initialization
// ===================================================

def execute(Map config, Map params) {
    def logger = config.utils.logger
    def validator = config.utils.validator
    def constants = config.constants
    
    logger.logPhaseStart("SETUP")
    
    try {
        // Step 1: Validate environment
        logger.step("Validating Environment")
        validateEnvironment(config, validator)
        
        // Step 2: Setup workspace
        logger.step("Setting Up Workspace")
        setupWorkspace()
        
        // Step 3: Clone Git repository
        logger.step("Cloning Git Repository")
        cloneGitRepository(constants.git)
        
        // Step 4: Checkout specified branch
        logger.step("Checking Out Branch")
        checkoutBranch(params.BRANCH ?: constants.git.branch)
        
        // Step 5: Install dependencies
        logger.step("Installing Dependencies")
        installDependencies()
        
        // Step 6: Execute setup job
        logger.step("Executing Setup Job")
        executeSetupJob(constants.jobs.phase1)
        
        // Step 7: Log environment information
        logger.step("Logging Environment Info")
        logEnvironmentInfo()
        
        logger.success("Phase 1: Setup completed successfully")
        config.env.updatePhaseStatus("SETUP", "SUCCESS", "Environment initialized successfully")
        
    } catch (Exception e) {
        logger.error("Phase 1: Setup failed", e)
        config.env.updatePhaseStatus("SETUP", "FAILED", e.getMessage())
        throw e
    } finally {
        logger.logPhaseEnd("SETUP", env.PHASE_SETUP_STATUS)
    }
}

// Private helper methods
private void validateEnvironment(Map config, def validator) {
    // Check required environment variables
    def requiredEnvVars = [
        'JAVA_HOME',
        'MAVEN_HOME', 
        'PATH',
        'WORKSPACE'
    ]
    
    validator.validateEnvironmentVariables(requiredEnvVars)
    
    // Check disk space (minimum 1GB)
    validator.validateDiskSpace(1024)
    
    // Check network connectivity
    def requiredHosts = [
        'github.com',
        'repo.maven.apache.org'
    ]
    
    validator.validateNetworkConnectivity(requiredHosts)
    
    // Log environment details
    echo "Environment Details:"
    echo "  JAVA_HOME: ${env.JAVA_HOME}"
    echo "  MAVEN_HOME: ${env.MAVEN_HOME}"
    echo "  Workspace: ${env.WORKSPACE}"
}

private void setupWorkspace() {
    // Clean workspace if needed
    if (params.CLEAN_WORKSPACE == true) {
        echo "Cleaning workspace..."
        cleanWs()
    }
    
    // Create required directories
    dir(env.WORKSPACE) {
        sh '''
            mkdir -p logs
            mkdir -p reports
            mkdir -p artifacts
            mkdir -p temp
        '''
    }
    
    echo "Workspace structure created"
}

private void cloneGitRepository(Map gitConfig) {
    withCredentials([usernamePassword(
        credentialsId: gitConfig.credentialsId,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        def repoUrl = gitConfig.repository.replace('https://', "https://${GIT_USERNAME}:${GIT_PASSWORD}@")
        
        sh """
            # Clone repository if not already cloned
            if [ ! -d ".git" ]; then
                echo "Cloning repository..."
                git clone ${repoUrl} .
            else
                echo "Repository already exists, fetching updates..."
                git fetch --all
            fi
        """
    }
    
    // Store Git information
    env.GIT_URL = gitConfig.repository
    env.GIT_BRANCH = sh(script: 'git branch --show-current', returnStdout: true).trim()
    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    env.GIT_AUTHOR = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()
    env.GIT_COMMIT_MESSAGE = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
}

private void checkoutBranch(String branchName) {
    sh """
        # Checkout specified branch
        git checkout ${branchName}
        
        # Pull latest changes
        git pull origin ${branchName}
        
        # Verify checkout
        CURRENT_BRANCH=\$(git branch --show-current)
        echo "Currently on branch: \${CURRENT_BRANCH}"
    """
}

private void installDependencies() {
    echo "Installing project dependencies..."
    
    // Check Maven installation
    def mavenVersion = sh(script: 'mvn -v | head -1', returnStdout: true).trim()
    echo "Maven Version: ${mavenVersion}"
    
    // Install dependencies (skip tests)
    sh 'mvn clean install -DskipTests -DskipITs -q'
    
    echo "Dependencies installed successfully"
}

private void executeSetupJob(Map jobConfig) {
    echo "Executing setup job: ${jobConfig.name}"
    
    try {
        // Build the job with parameters
        def buildParams = [
            string(name: 'BRANCH', value: env.GIT_BRANCH),
            string(name: 'COMMIT', value: env.GIT_COMMIT),
            booleanParam(name: 'CLEAN_BUILD', value: params.CLEAN_BUILD ?: false)
        ]
        
        def result = build job: jobConfig.name,
                         parameters: buildParams,
                         wait: true,
                         propagate: false
        
        if (result.result == 'SUCCESS') {
            echo "Setup job completed successfully"
        } else {
            throw new Exception("Setup job failed with status: ${result.result}")
        }
        
    } catch (Exception e) {
        echo "Setup job execution failed, continuing with local setup..."
        // Continue with local setup if job fails
        performLocalSetup()
    }
}

private void performLocalSetup() {
    echo "Performing local setup..."
    
    // Create configuration files
    sh '''
        # Create configuration directory
        mkdir -p config
        
        # Create default properties file
        cat > config/application.properties << 'EOF'
        # Application Properties
        app.name=Java8Feature
        app.version=1.0.0
        app.env=${DEPLOYMENT_ENVIRONMENT}
        
        # Database Configuration
        db.url=jdbc:mysql://localhost:3306/testdb
        db.username=testuser
        db.password=testpass
        
        # Logging Configuration
        logging.level.root=INFO
        logging.level.com.example=DEBUG
        EOF
        
        # Create log directory
        mkdir -p logs/app
    '''
    
    echo "Local setup completed"
}

private void logEnvironmentInfo() {
    echo "=== ENVIRONMENT INFORMATION ==="
    
    // System information
    sh '''
        echo "System: $(uname -a)"
        echo "Java: $(java -version 2>&1 | head -1)"
        echo "Maven: $(mvn -v | head -1)"
        echo "Git: $(git --version)"
        echo "Disk: $(df -h . | tail -1)"
        echo "Memory: $(free -h | head -2 | tail -1)"
    '''
    
    // Git information
    echo "Git Branch: ${env.GIT_BRANCH}"
    echo "Git Commit: ${env.GIT_COMMIT}"
    echo "Git Author: ${env.GIT_AUTHOR}"
    echo "Commit Message: ${env.GIT_COMMIT_MESSAGE}"
    
    // Workspace information
    echo "Workspace: ${env.WORKSPACE}"
    echo "Build Number: ${env.BUILD_NUMBER}"
    echo "Node: ${env.NODE_NAME}"
}

return this
