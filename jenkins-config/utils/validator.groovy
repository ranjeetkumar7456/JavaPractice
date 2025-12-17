// ===================================================
// Validation Utility
// ===================================================

class PipelineValidator {
    
    def logger
    
    PipelineValidator(logger) {
        this.logger = logger
    }
    
    // Validate environment variables
    def validateEnvironmentVariables(List requiredVars) {
        logger.step("Validating environment variables")
        
        def missingVars = []
        requiredVars.each { varName ->
            if (!env[varName]) {
                missingVars << varName
            }
        }
        
        if (missingVars) {
            def errorMsg = "Missing required environment variables: ${missingVars.join(', ')}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        
        logger.success("All environment variables validated")
        return true
    }
    
    // Validate disk space
    def validateDiskSpace(long minSpaceMB) {
        logger.step("Validating disk space")
        
        try {
            def diskInfo = sh(script: "df -m ${env.WORKSPACE} | tail -1", returnStdout: true).trim()
            def availableSpace = diskInfo.split("\\s+")[3].toLong()
            
            logger.info("Available disk space: ${availableSpace}MB (Required: ${minSpaceMB}MB)")
            
            if (availableSpace < minSpaceMB) {
                def errorMsg = "Insufficient disk space. Available: ${availableSpace}MB, Required: ${minSpaceMB}MB"
                logger.error(errorMsg)
                throw new Exception(errorMsg)
            }
            
            logger.success("Disk space validation passed")
            return availableSpace
            
        } catch (Exception e) {
            logger.warning("Disk space validation failed: ${e.getMessage()}")
            return -1
        }
    }
    
    // Validate network connectivity
    def validateNetworkConnectivity(List hosts, int timeout = 5) {
        logger.step("Validating network connectivity")
        
        def unreachableHosts = []
        hosts.each { host ->
            try {
                def result = sh(script: "ping -c 1 -W ${timeout} ${host}", returnStdout: true, returnStatus: true)
                if (result != 0) {
                    unreachableHosts << host
                }
            } catch (Exception e) {
                unreachableHosts << host
            }
        }
        
        if (unreachableHosts) {
            def warningMsg = "Cannot reach hosts: ${unreachableHosts.join(', ')}"
            logger.warning(warningMsg)
            return false
        }
        
        logger.success("Network connectivity validation passed")
        return true
    }
    
    // Validate file existence
    def validateFileExists(String filePath, String description = "File") {
        logger.debug("Validating ${description} exists: ${filePath}")
        
        def exists = fileExists(filePath)
        if (!exists) {
            def errorMsg = "${description} not found: ${filePath}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        
        logger.debug("${description} validated: ${filePath}")
        return true
    }
    
    // Validate directory existence
    def validateDirectoryExists(String dirPath, String description = "Directory") {
        logger.debug("Validating ${description} exists: ${dirPath}")
        
        def exists = dirExists(dirPath)
        if (!exists) {
            def errorMsg = "${description} not found: ${dirPath}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        
        logger.debug("${description} validated: ${dirPath}")
        return true
    }
    
    // Validate test results
    def validateTestResults(Map testResults) {
        logger.step("Validating test results")
        
        def totalTests = testResults.totalCount ?: 0
        def passedTests = testResults.passCount ?: 0
        def failedTests = testResults.failCount ?: 0
        
        logger.info("Test Results: Total=${totalTests}, Passed=${passedTests}, Failed=${failedTests}")
        
        if (totalTests == 0) {
            logger.warning("No tests were executed")
            return false
        }
        
        if (failedTests > 0) {
            logger.warning("Some tests failed: ${failedTests} failures")
            return false
        }
        
        logger.success("Test results validation passed")
        return true
    }
    
    // Validate build artifacts
    def validateBuildArtifacts(List artifactPatterns) {
        logger.step("Validating build artifacts")
        
        def missingArtifacts = []
        artifactPatterns.each { pattern ->
            def files = findFiles(glob: pattern)
            if (files.size() == 0) {
                missingArtifacts << pattern
            }
        }
        
        if (missingArtifacts) {
            def errorMsg = "Missing build artifacts: ${missingArtifacts.join(', ')}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        
        logger.success("Build artifacts validation passed")
        return true
    }
    
    // Validate Git repository
    def validateGitRepository() {
        logger.step("Validating Git repository")
        
        try {
            // Check if in git repository
            sh(script: 'git rev-parse --git-dir', returnStatus: true)
            
            // Get current branch
            def currentBranch = sh(script: 'git branch --show-current', returnStdout: true).trim()
            logger.info("Current Git branch: ${currentBranch}")
            
            // Check for uncommitted changes
            def status = sh(script: 'git status --porcelain', returnStdout: true).trim()
            if (status) {
                logger.warning("There are uncommitted changes in the repository")
            }
            
            logger.success("Git repository validation passed")
            return [
                branch: currentBranch,
                hasUncommittedChanges: !!status
            ]
            
        } catch (Exception e) {
            def errorMsg = "Git repository validation failed: ${e.getMessage()}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
    }
    
    // Validate parameters
    def validateParameters(Map params, Map validationRules) {
        logger.step("Validating pipeline parameters")
        
        def validationErrors = []
        
        validationRules.each { paramName, rules ->
            def paramValue = params[paramName]
            
            // Check required
            if (rules.required && !paramValue) {
                validationErrors << "Parameter '${paramName}' is required"
            }
            
            // Check type
            if (paramValue && rules.type) {
                def isValid = false
                switch(rules.type) {
                    case 'string':
                        isValid = paramValue instanceof String
                        break
                    case 'number':
                        isValid = paramValue.toString().isNumber()
                        break
                    case 'boolean':
                        isValid = paramValue.toString().toBoolean() != null
                        break
                }
                
                if (!isValid) {
                    validationErrors << "Parameter '${paramName}' must be of type ${rules.type}"
                }
            }
            
            // Check allowed values
            if (paramValue && rules.allowedValues) {
                if (!rules.allowedValues.contains(paramValue)) {
                    validationErrors << "Parameter '${paramName}' must be one of: ${rules.allowedValues.join(', ')}"
                }
            }
        }
        
        if (validationErrors) {
            def errorMsg = "Parameter validation failed:\n${validationErrors.join('\n')}"
            logger.error(errorMsg)
            throw new Exception(errorMsg)
        }
        
        logger.success("Parameters validation passed")
        return true
    }
}

// Create instance with logger
def validator = new PipelineLogger()
def validatorInstance = new PipelineValidator(validator)

return [
    validateEnvironmentVariables: { vars -> validatorInstance.validateEnvironmentVariables(vars) },
    validateDiskSpace: { minSpace -> validatorInstance.validateDiskSpace(minSpace) },
    validateNetworkConnectivity: { hosts, timeout = 5 -> validatorInstance.validateNetworkConnectivity(hosts, timeout) },
    validateFileExists: { path, desc = "File" -> validatorInstance.validateFileExists(path, desc) },
    validateDirectoryExists: { path, desc = "Directory" -> validatorInstance.validateDirectoryExists(path, desc) },
    validateTestResults: { results -> validatorInstance.validateTestResults(results) },
    validateBuildArtifacts: { patterns -> validatorInstance.validateBuildArtifacts(patterns) },
    validateGitRepository: { -> validatorInstance.validateGitRepository() },
    validateParameters: { params, rules -> validatorInstance.validateParameters(params, rules) }
]
