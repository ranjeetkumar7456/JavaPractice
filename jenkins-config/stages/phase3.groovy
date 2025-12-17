// ===================================================
// Phase 3: Build & Compilation
// ===================================================

def execute(Map config, Map params) {
    def logger = config.utils.logger
    def validator = config.utils.validator
    def constants = config.constants
    
    logger.logPhaseStart("BUILD")
    
    try {
        // Step 1: Clean build
        logger.step("Cleaning Previous Build")
        cleanBuild()
        
        // Step 2: Compile source code
        logger.step("Compiling Source Code")
        compileSource()
        
        // Step 3: Run unit tests
        logger.step("Running Unit Tests")
        runUnitTests()
        
        // Step 4: Package application
        logger.step("Packaging Application")
        packageApplication()
        
        // Step 5: Execute build job
        logger.step("Executing Build Job")
        executeBuildJob(constants.jobs.phase3)
        
        // Step 6: Generate build artifacts
        logger.step("Generating Build Artifacts")
        generateBuildArtifacts()
        
        // Step 7: Validate build output
        logger.step("Validating Build Output")
        validateBuildOutput()
        
        logger.success("Phase 3: Build completed successfully")
        config.env.updatePhaseStatus("BUILD", "SUCCESS", "Build completed successfully")
        
    } catch (Exception e) {
        logger.error("Phase 3: Build failed", e)
        config.env.updatePhaseStatus("BUILD", "FAILED", e.getMessage())
        throw e
    } finally {
        logger.logPhaseEnd("BUILD", env.PHASE_BUILD_STATUS)
    }
}

// Private helper methods
private void cleanBuild() {
    echo "Cleaning previous build artifacts..."
    
    sh 'mvn clean -q'
    
    // Clean additional directories
    sh '''
        rm -rf target/*
        rm -rf out/*
        rm -rf dist/*
    '''
    
    echo "Build cleaned successfully"
}

private void compileSource() {
    echo "Compiling source code..."
    
    def compileCommand = 'mvn compile -q'
    
    if (params.SKIP_TESTS == true) {
        compileCommand += ' -DskipTests'
    }
    
    if (params.SKIP_INTEGRATION_TESTS == true) {
        compileCommand += ' -DskipITs'
    }
    
    def result = sh(script: compileCommand, returnStatus: true)
    
    if (result != 0) {
        // Try to get compilation errors
        def errors = sh(script: 'mvn compile 2>&1 | grep "ERROR\\|FAILURE"', returnStdout: true).trim()
        throw new Exception("Compilation failed:\n${errors}")
    }
    
    echo "✅ Source code compiled successfully"
}

private void runUnitTests() {
    if (params.SKIP_TESTS == true) {
        echo "Skipping unit tests as requested"
        return
    }
    
    echo "Running unit tests..."
    
    try {
        def testCommand = 'mvn test -q'
        
        if (params.TEST_THREAD_COUNT) {
            testCommand += " -DforkCount=${params.TEST_THREAD_COUNT}"
        }
        
        if (params.TEST_CATEGORY) {
            testCommand += " -Dtest=${params.TEST_CATEGORY}"
        }
        
        sh testCommand
        
        // Generate test report
        sh 'mvn surefire-report:report-only'
        
        echo "✅ Unit tests executed successfully"
        
    } catch (Exception e) {
        // Generate failure report
        sh 'mvn surefire-report:report-only'
        
        // Archive test results
        archiveArtifacts artifacts: 'target/surefire-reports/*', fingerprint: true
        
        throw new Exception("Unit tests failed. Check test reports for details.")
    }
}

private void packageApplication() {
    echo "Packaging application..."
    
    def packageCommand = 'mvn package -q -DskipTests'
    
    if (params.BUILD_PROFILE) {
        packageCommand += " -P${params.BUILD_PROFILE}"
    }
    
    if (params.BUILD_ENVIRONMENT) {
        packageCommand += " -Denv=${params.BUILD_ENVIRONMENT}"
    }
    
    sh packageCommand
    
    // Verify package creation
    def jarFile = findFiles(glob: 'target/*.jar')[0]
    if (!jarFile) {
        throw new Exception("No JAR file created during packaging")
    }
    
    env.BUILD_ARTIFACT = jarFile.path
    env.BUILD_VERSION = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
    
    echo "✅ Application packaged: ${env.BUILD_ARTIFACT}"
    echo "   Version: ${env.BUILD_VERSION}"
}

private void executeBuildJob(Map jobConfig) {
    echo "Executing build job: ${jobConfig.name}"
    
    try {
        def buildParams = [
            string(name: 'BRANCH', value: env.GIT_BRANCH),
            string(name: 'COMMIT', value: env.GIT_COMMIT),
            string(name: 'BUILD_PROFILE', value: params.BUILD_PROFILE ?: ''),
            string(name: 'BUILD_ENVIRONMENT', value: params.BUILD_ENVIRONMENT ?: ''),
            booleanParam(name: 'SKIP_TESTS', value: params.SKIP_TESTS ?: false),
            booleanParam(name: 'SKIP_INTEGRATION_TESTS', value: params.SKIP_INTEGRATION_TESTS ?: false)
        ]
        
        def result = build job: jobConfig.name,
                         parameters: buildParams,
                         wait: true,
                         propagate: false
        
        if (result.result == 'SUCCESS') {
            echo "Build job completed successfully"
            
            // Copy artifacts from build job if needed
            copyArtifacts(
                projectName: jobConfig.name,
                selector: specific("${result.number}"),
                filter: 'target/*.jar',
                target: 'artifacts/',
                flatten: true
            )
            
        } else {
            throw new Exception("Build job failed with status: ${result.result}")
        }
        
    } catch (Exception e) {
        echo "Build job failed: ${e.getMessage()}"
        // Continue with local build
    }
}

private void generateBuildArtifacts() {
    echo "Generating build artifacts..."
    
    // Create artifacts directory
    sh 'mkdir -p artifacts'
    
    // Copy JAR file
    if (env.BUILD_ARTIFACT && fileExists(env.BUILD_ARTIFACT)) {
        sh "cp ${env.BUILD_ARTIFACT} artifacts/"
    }
    
    // Copy dependency JARs if any
    if (fileExists('target/libs')) {
        sh 'cp -r target/libs/* artifacts/ 2>/dev/null || true'
    }
    
    // Generate manifest file
    def manifestContent = """
BUILD MANIFEST
==============
Project: Java8Feature
Build Number: ${env.BUILD_NUMBER}
Build Version: ${env.BUILD_VERSION}
Build Timestamp: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
Git Branch: ${env.GIT_BRANCH}
Git Commit: ${env.GIT_COMMIT}
Git Author: ${env.GIT_AUTHOR}
Build Node: ${env.NODE_NAME}

ARTIFACTS:
----------
"""
    
    // List all artifacts
    dir('artifacts') {
        def artifacts = findFiles(glob: '*')
        artifacts.each { artifact ->
            manifestContent += "- ${artifact.name}\n"
        }
    }
    
    writeFile file: 'artifacts/build-manifest.txt', text: manifestContent
    
    echo "✅ Build artifacts generated in artifacts/ directory"
}

private void validateBuildOutput() {
    echo "Validating build output..."
    
    def validationErrors = []
    
    // Check if JAR file exists
    def jarFiles = findFiles(glob: 'target/*.jar')
    if (jarFiles.size() == 0) {
        validationErrors << "No JAR file created in target directory"
    }
    
    // Check JAR file size (minimum 1KB)
    jarFiles.each { jarFile ->
        def size = sh(script: "stat -c%s ${jarFile.path}", returnStdout: true).trim().toInteger()
        if (size < 1024) {
            validationErrors << "JAR file ${jarFile.name} is too small (${size} bytes)"
        }
    }
    
    // Check if JAR is executable
    try {
        sh 'java -jar target/*.jar --version 2>&1 | head -1'
    } catch (Exception e) {
        validationErrors << "JAR file is not executable or version check failed"
    }
    
    // Validate dependencies
    try {
        sh 'mvn dependency:analyze -DignoreNonCompile=true'
    } catch (Exception e) {
        validationErrors << "Dependency analysis found issues"
    }
    
    if (validationErrors.size() > 0) {
        throw new Exception("Build validation failed:\n${validationErrors.join('\n')}")
    }
    
    echo "✅ Build output validated successfully"
}

return this
