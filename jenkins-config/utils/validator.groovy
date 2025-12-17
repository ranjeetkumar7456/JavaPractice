
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
        return
