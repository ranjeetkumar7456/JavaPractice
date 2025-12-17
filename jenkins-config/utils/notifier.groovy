// ===================================================
// Notification Utility
// ===================================================

class PipelineNotifier {
    
    def logger
    
    PipelineNotifier(logger) {
        this.logger = logger
    }
    
    // Send success notification
    def sendSuccessNotification(Map config, Map params) {
        logger.info("Sending success notification")
        
        try {
            def notificationConfig = config.constants.notifications
            
            if (notificationConfig.email.onSuccess) {
                sendEmailNotification("success", config, params)
            }
            
            if (notificationConfig.slack.enabled && notificationConfig.slack.onSuccess) {
                sendSlackNotification("success", config, params)
            }
            
            logger.success("Success notifications sent")
            
        } catch (Exception e) {
            logger.warning("Failed to send success notification: ${e.getMessage()}")
        }
    }
    
    // Send failure notification
    def sendFailureNotification(Map config, Map params) {
        logger.info("Sending failure notification")
        
        try {
            def notificationConfig = config.constants.notifications
            
            if (notificationConfig.email.onFailure) {
                sendEmailNotification("failure", config, params)
            }
            
            if (notificationConfig.slack.enabled && notificationConfig.slack.onFailure) {
                sendSlackNotification("failure", config, params)
            }
            
            logger.info("Failure notifications sent")
            
        } catch (Exception e) {
            logger.warning("Failed to send failure notification: ${e.getMessage()}")
        }
    }
    
    // Send unstable notification
    def sendUnstableNotification(Map config, Map params) {
        logger.info("Sending unstable notification")
        
        try {
            def notificationConfig = config.constants.notifications
            
            if (notificationConfig.email.onUnstable) {
                sendEmailNotification("unstable", config, params)
            }
            
            if (notificationConfig.slack.enabled) {
                sendSlackNotification("unstable", config, params)
            }
            
            logger.info("Unstable notifications sent")
            
        } catch (Exception e) {
            logger.warning("Failed to send unstable notification: ${e.getMessage()}")
        }
    }
    
    // Send rollback notification
    def sendRollbackNotification(Map config, Map params, String reason) {
        logger.section("Sending Rollback Notification")
        
        try {
            def subject = "🚨 ROLLBACK INITIATED - ${config.constants.project.name} Build #${env.BUILD_NUMBER}"
            
            def body = """
ROLLBACK EXECUTED

Project: ${config.constants.project.name}
Build: #${env.BUILD_NUMBER}
Status: ROLLBACK_INITIATED

REASON FOR ROLLBACK:
${reason}

TEST RESULTS:
- Pass Percentage: ${env.TEST_PASS_PERCENTAGE}%
- Threshold Required: ${config.constants.test.passThreshold}%
- Retry Attempts: ${env.TEST_RETRY_COUNT}/${config.constants.test.maxRetryCount}

GIT INFORMATION:
- Previous Commit: ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: 'N/A'}
- Current Commit: ${env.GIT_COMMIT ?: 'N/A'}
- Branch: ${env.GIT_BRANCH ?: 'N/A'}

BUILD INFORMATION:
- Build URL: ${env.BUILD_URL}
- Node: ${env.NODE_NAME}
- Duration: ${env.DURATION ?: 'N/A'}

ACTION TAKEN:
- Git revert executed
- Repository rolled back to previous stable state
- Deployment cancelled

Please investigate the test failures and fix the issues before the next deployment.
"""
            
            // Send email
            emailext(
                subject: subject,
                body: body,
                to: config.constants.notifications.email.recipients,
                cc: config.constants.notifications.email.cc
            )
            
            logger.success("Rollback notification sent")
            
        } catch (Exception e) {
            logger.error("Failed to send rollback notification: ${e.getMessage()}")
        }
    }
    
    // Private email notification method
    private void sendEmailNotification(String type, Map config, Map params) {
        def notificationConfig = config.constants.notifications.email
        
        def subject = getEmailSubject(type, config)
        def body = getEmailBody(type, config, params)
        
        emailext(
            subject: subject,
            body: body,
            to: notificationConfig.recipients,
            cc: notificationConfig.cc,
            attachLog: (type == "failure"),
            compressLog: true
        )
    }
    
    // Private slack notification method
    private void sendSlackNotification(String type, Map config, Map params) {
        def slackConfig = config.constants.notifications.slack
        
        def message = getSlackMessage(type, config, params)
        def color = getSlackColor(type)
        
        // In real implementation, use Slack plugin
        // slackSend(channel: slackConfig.channel, color: color, message: message)
        
        logger.debug("Slack notification prepared: ${message}")
    }
    
    // Helper methods for email
    private String getEmailSubject(String type, Map config) {
        def statusIcon = type == "success" ? "✅" : type == "failure" ? "❌" : "⚠️"
        def statusText = type.toUpperCase()
        
        return "${statusIcon} ${statusText} - ${config.constants.project.name} Build #${env.BUILD_NUMBER}"
    }
    
    private String getEmailBody(String type, Map config, Map params) {
        def project = config.constants.project
        
        return """
PIPELINE EXECUTION REPORT
=========================

PROJECT INFORMATION:
-------------------
Name: ${project.name}
Description: ${project.description}
Version: ${project.version}

BUILD INFORMATION:
------------------
Build Number: #${env.BUILD_NUMBER}
Build Status: ${type.toUpperCase()}
Start Time: ${env.START_TIME ?: 'N/A'}
End Time: ${env.END_TIME ?: 'N/A'}
Duration: ${env.DURATION ?: 'N/A'}
Build URL: ${env.BUILD_URL}

PHASE STATUSES:
---------------
Setup: ${env.PHASE_SETUP_STATUS ?: 'N/A'}
Validation: ${env.PHASE_VALIDATION_STATUS ?: 'N/A'}
Build: ${env.PHASE_BUILD_STATUS ?: 'N/A'}
Testing: ${env.TEST_PHASE_STATUS ?: 'N/A'}
Deployment: ${env.PHASE_DEPLOYMENT_STATUS ?: 'N/A'}

TEST RESULTS:
-------------
Pass Percentage: ${env.TEST_PASS_PERCENTAGE ?: '0'}%
Total Tests: ${env.TEST_TOTAL_COUNT ?: '0'}
Passed: ${env.TEST_PASSED_COUNT ?: '0'}
Failed: ${env.TEST_FAILED_COUNT ?: '0'}
Retry Count: ${env.TEST_RETRY_COUNT ?: '0'}

PARAMETERS:
-----------
${getParametersTable(params)}

GIT INFORMATION:
----------------
Commit: ${env.GIT_COMMIT ?: 'N/A'}
Branch: ${env.GIT_BRANCH ?: 'N/A'}
Author: ${env.GIT_AUTHOR ?: 'N/A'}

${getAdditionalInfo(type)}
"""
    }
    
    // Helper methods for slack
    private String getSlackMessage(String type, Map config, Map params) {
        def project = config.constants.project
        
        return """
*${type.toUpperCase()}* - ${project.name} Build #${env.BUILD_NUMBER}
• Status: ${type.toUpperCase()}
• Project: ${project.name}
• Branch: ${env.GIT_BRANCH ?: 'N/A'}
• Tests: ${env.TEST_PASS_PERCENTAGE ?: '0'}% passed
• Duration: ${env.DURATION ?: 'N/A'}
• Build URL: ${env.BUILD_URL}
"""
    }
    
    private String getSlackColor(String type) {
        switch(type) {
            case "success": return "good"
            case "failure": return "danger"
            case "unstable": return "warning"
            default: return "#808080"
        }
    }
    
    // Utility methods
    private String getParametersTable(Map params) {
        if (!params) return "No parameters"
        
        def table = ""
        params.each { key, value ->
            table += "${key}: ${value}\n"
        }
        return table
    }
    
    private String getAdditionalInfo(String type) {
        switch(type) {
            case "success":
                return "\n✅ All phases completed successfully. The application has been deployed."
            case "failure":
                return "\n❌ Pipeline failed. Please check the build logs for details."
            case "unstable":
                return "\n⚠️ Pipeline completed with warnings. Some phases may have been skipped."
            default:
                return ""
        }
    }
}

// Create instance with logger
def logger = new PipelineLogger()
def notifier = new PipelineNotifier(logger)

return [
    sendSuccessNotification: { config, params -> notifier.sendSuccessNotification(config, params) },
    sendFailureNotification: { config, params -> notifier.sendFailureNotification(config, params) },
    sendUnstableNotification: { config, params -> notifier.sendUnstableNotification(config, params) },
    sendRollbackNotification: { config, params, reason -> notifier.sendRollbackNotification(config, params, reason) }
]
