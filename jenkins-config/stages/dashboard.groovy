// ===================================================
// Comprehensive Dashboard Generator
// ===================================================

def generate(Map config, Map params) {
    def logger = config.utils.logger
    
    logger.section("📊 GENERATING COMPREHENSIVE DASHBOARD")
    
    try {
        // Step 1: Collect all metrics
        logger.step("Collecting Pipeline Metrics")
        def metrics = collectAllMetrics(config, params)
        
        // Step 2: Generate HTML dashboard
        logger.step("Generating HTML Dashboard")
        generateHTMLDashboard(metrics, config)
        
        // Step 3: Generate JSON report
        logger.step("Generating JSON Report")
        generateJSONReport(metrics, config)
        
        // Step 4: Generate summary report
        logger.step("Generating Summary Report")
        generateSummaryReport(metrics, config)
        
        // Step 5: Archive all reports
        logger.step("Archiving Dashboard Reports")
        archiveDashboardReports()
        
        // Step 6: Display dashboard in console
        logger.step("Displaying Console Dashboard")
        displayConsoleDashboard(metrics)
        
        logger.success("✅ Dashboard generation completed successfully")
        
    } catch (Exception e) {
        logger.error("Dashboard generation failed", e)
        // Generate basic dashboard even if full generation fails
        generateBasicDashboard(config)
    }
}

// Private helper methods
private Map collectAllMetrics(Map config, Map params) {
    def metrics = [
        timestamp: new Date().format('yyyy-MM-dd HH:mm:ss'),
        pipeline: collectPipelineMetrics(config),
        phases: collectPhaseMetrics(),
        tests: collectTestMetrics(),
        git: collectGitMetrics(),
        deployment: collectDeploymentMetrics(),
        performance: collectPerformanceMetrics(),
        environment: collectEnvironmentMetrics(),
        rollback: collectRollbackMetrics()
    ]
    
    // Calculate overall status
    metrics.pipeline.overallStatus = calculateOverallStatus(metrics)
    
    return metrics
}

private Map collectPipelineMetrics(Map config) {
    return [
        projectName: config.constants.project.name,
        projectVersion: config.constants.project.version,
        buildNumber: env.BUILD_NUMBER,
        jobName: env.JOB_NAME,
        buildUrl: env.BUILD_URL,
        startTime: env.START_TIME ?: 'N/A',
        endTime: env.END_TIME ?: 'N/A',
        duration: env.DURATION ?: 'N/A',
        pipelineStatus: env.PIPELINE_STATUS ?: 'UNKNOWN',
        pipelineResult: currentBuild.currentResult ?: 'UNKNOWN',
        node: env.NODE_NAME ?: 'N/A',
        workspace: env.WORKSPACE ?: 'N/A'
    ]
}

private Map collectPhaseMetrics() {
    def phases = [
        'SETUP': [
            status: env.PHASE_SETUP_STATUS ?: 'PENDING',
            message: env.PHASE_SETUP_MESSAGE ?: '',
            startTime: env.SETUP_START_TIME ?: 'N/A',
            endTime: env.SETUP_END_TIME ?: 'N/A'
        ],
        'VALIDATION': [
            status: env.PHASE_VALIDATION_STATUS ?: 'PENDING',
            message: env.PHASE_VALIDATION_MESSAGE ?: '',
            startTime: env.VALIDATION_START_TIME ?: 'N/A',
            endTime: env.VALIDATION_END_TIME ?: 'N/A'
        ],
        'BUILD': [
            status: env.PHASE_BUILD_STATUS ?: 'PENDING',
            message: env.PHASE_BUILD_MESSAGE ?: '',
            startTime: env.BUILD_START_TIME ?: 'N/A',
            endTime: env.BUILD_END_TIME ?: 'N/A'
        ],
        'TESTING': [
            status: env.TEST_PHASE_STATUS ?: 'PENDING',
            message: env.TEST_PHASE_MESSAGE ?: '',
            startTime: env.TESTING_START_TIME ?: 'N/A',
            endTime: env.TESTING_END_TIME ?: 'N/A'
        ],
        'DEPLOYMENT': [
            status: env.PHASE_DEPLOYMENT_STATUS ?: 'PENDING',
            message: env.PHASE_DEPLOYMENT_MESSAGE ?: '',
            startTime: env.DEPLOYMENT_START_TIME ?: 'N/A',
            endTime: env.DEPLOYMENT_END_TIME ?: 'N/A'
        ]
    ]
    
    // Calculate phase durations
    phases.each { phaseName, phaseData ->
        if (phaseData.startTime != 'N/A' && phaseData.endTime != 'N/A') {
            try {
                def start = Date.parse('HH:mm:ss', phaseData.startTime)
                def end = Date.parse('HH:mm:ss', phaseData.endTime)
                def duration = end.getTime() - start.getTime()
                phaseData.duration = "${(duration / 1000).toInteger()}s"
            } catch (Exception e) {
                phaseData.duration = 'N/A'
            }
        } else {
            phaseData.duration = 'N/A'
        }
    }
    
    return phases
}

private Map collectTestMetrics() {
    return [
        passPercentage: env.TEST_PASS_PERCENTAGE ?: '0',
        totalTests: env.TEST_TOTAL_COUNT ?: '0',
        passedTests: env.TEST_PASSED_COUNT ?: '0',
        failedTests: env.TEST_FAILED_COUNT ?: '0',
        skippedTests: env.TEST_SKIPPED_COUNT ?: '0',
        retryCount: env.TEST_RETRY_COUNT ?: '0',
        threshold: env.PASS_THRESHOLD ?: '70',
        status: calculateTestStatus()
    ]
}

private String calculateTestStatus() {
    def percentage = env.TEST_PASS_PERCENTAGE ?: '0'
    def threshold = env.PASS_THRESHOLD ?: '70'
    
    if (percentage.toFloat() >= threshold.toFloat()) {
        return 'PASSED'
    } else {
        return 'FAILED'
    }
}

private Map collectGitMetrics() {
    return [
        branch: env.GIT_BRANCH ?: 'N/A',
        commit: env.GIT_COMMIT ?: 'N/A',
        shortCommit: env.GIT_COMMIT ? env.GIT_COMMIT.take(8) : 'N/A',
        author: env.GIT_AUTHOR ?: 'N/A',
        commitMessage: env.GIT_COMMIT_MESSAGE ?: 'N/A',
        repoUrl: env.GIT_URL ?: 'N/A',
        preRollbackCommit: env.PRE_ROLLBACK_COMMIT ?: 'N/A',
        postRollbackCommit: env.POST_ROLLBACK_COMMIT ?: 'N/A',
        hasChanges: env.GIT_HAS_CHANGES ?: 'false'
    ]
}

private Map collectDeploymentMetrics() {
    return [
        environment: env.DEPLOYMENT_ENVIRONMENT ?: 'N/A',
        status: env.DEPLOYMENT_STATUS ?: 'NOT_DEPLOYED',
        url: env.DEPLOYMENT_URL ?: 'N/A',
        version: env.BUILD_VERSION ?: 'N/A',
        timestamp: env.DEPLOYMENT_COMPLETION_TIME ?: 'N/A',
        rollbackTime: env.DEPLOYMENT_ROLLBACK_TIME ?: 'N/A',
        verificationTime: env.DEPLOYMENT_VERIFICATION_TIME ?: 'N/A',
        artifact: env.BUILD_ARTIFACT ?: 'N/A'
    ]
}

private Map collectPerformanceMetrics() {
    return [
        buildDuration: env.BUILD_DURATION_MS ?: '0',
        memoryUsage: env.MEMORY_USAGE_MB ?: 'N/A',
        diskUsage: env.DISK_USAGE_GB ?: 'N/A',
        cpuUsage: env.CPU_USAGE_PERCENT ?: 'N/A',
        testExecutionTime: env.TEST_EXECUTION_TIME ?: 'N/A',
        deploymentTime: env.DEPLOYMENT_TIME ?: 'N/A'
    ]
}

private Map collectEnvironmentMetrics() {
    return [
        javaVersion: getJavaVersion(),
        mavenVersion: getMavenVersion(),
        os: getOSInfo(),
        diskSpace: getDiskSpace(),
        nodeLabel: env.NODE_LABELS ?: 'N/A',
        executorCount: env.EXECUTOR_NUMBER ?: 'N/A'
    ]
}

private String getJavaVersion() {
    try {
        return sh(script: 'java -version 2>&1 | head -1', returnStdout: true).trim()
    } catch (Exception e) {
        return 'N/A'
    }
}

private String getMavenVersion() {
    try {
        return sh(script: 'mvn -v | head -1', returnStdout: true).trim()
    } catch (Exception e) {
        return 'N/A'
    }
}

private String getOSInfo() {
    try {
        return sh(script: 'uname -a', returnStdout: true).trim()
    } catch (Exception e) {
        return 'N/A'
    }
}

private String getDiskSpace() {
    try {
        return sh(script: 'df -h . | tail -1', returnStdout: true).trim()
    } catch (Exception e) {
        return 'N/A'
    }
}

private Map collectRollbackMetrics() {
    return [
        required: env.ROLLBACK_REQUIRED ?: 'false',
        reason: env.ROLLBACK_REASON ?: 'N/A',
        status: env.ROLLBACK_STATUS ?: 'N/A',
        strategy: env.ROLLBACK_STRATEGY ?: 'N/A',
        executionTime: env.ROLLBACK_EXECUTION_TIME ?: 'N/A',
        targetCommit: env.ROLLBACK_TARGET_COMMIT ?: 'N/A',
        verificationStatus: env.ROLLBACK_VERIFICATION_STATUS ?: 'N/A'
    ]
}

private String calculateOverallStatus(Map metrics) {
    // Check for rollback
    if (metrics.rollback.required == 'true' && metrics.rollback.status == 'EXECUTED') {
        return 'UNSTABLE_WITH_ROLLBACK'
    }
    
    // Check pipeline result
    if (metrics.pipeline.pipelineResult == 'FAILURE') {
        return 'FAILED'
    } else if (metrics.pipeline.pipelineResult == 'UNSTABLE') {
        return 'UNSTABLE'
    }
    
    // Check all phases
    def allPhasesSuccess = metrics.phases.every { phaseName, phaseData ->
        phaseData.status == 'SUCCESS' || phaseData.status == 'SKIPPED'
    }
    
    if (allPhasesSuccess) {
        return 'SUCCESS'
    } else {
        return 'PARTIAL_SUCCESS'
    }
}

private void generateHTMLDashboard(Map metrics, Map config) {
    def htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pipeline Dashboard - Build #${metrics.pipeline.buildNumber}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .dashboard-container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 15px;
        }
        
        .header .status-badge {
            display: inline-block;
            padding: 8px 20px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 0.8em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .status-success { background: #27ae60; color: white; }
        .status-failed { background: #e74c3c; color: white; }
        .status-unstable { background: #f39c12; color: white; }
        .status-running { background: #3498db; color: white; }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            padding: 30px;
            background: #f8f9fa;
        }
        
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
        }
        
        .stat-card h3 {
            color: #2c3e50;
            font-size: 1.1em;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .stat-value {
            font-size: 2.2em;
            font-weight: bold;
            color: #34495e;
            margin: 10px 0;
        }
        
        .phase-timeline {
            padding: 30px;
            background: white;
        }
        
        .timeline {
            position: relative;
            max-width: 1000px;
            margin: 30px auto;
        }
        
        .timeline::after {
            content: '';
            position: absolute;
            width: 6px;
            background: #3498db;
            top: 0;
            bottom: 0;
            left: 50%;
            margin-left: -3px;
        }
        
        .phase-item {
            padding: 20px 40px;
            position: relative;
            background: white;
            width: 50%;
            box-sizing: border-box;
            margin: 20px 0;
            border-radius: 10px;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
        }
        
        .phase-item:nth-child(odd) {
            left: 0;
        }
        
        .phase-item:nth-child(even) {
            left: 50%;
        }
        
        .phase-status {
            position: absolute;
            width: 25px;
            height: 25px;
            right: -17px;
            background: #3498db;
            border: 4px solid white;
            border-radius: 50%;
            z-index: 1;
            top: 20px;
        }
        
        .phase-item:nth-child(even) .phase-status {
            left: -17px;
        }
        
        .test-results {
            padding: 30px;
            background: #f8f9fa;
        }
        
        .test-chart {
            display: flex;
            height: 100px;
            background: #ecf0f1;
            border-radius: 10px;
            overflow: hidden;
            margin: 20px 0;
        }
        
        .test-passed { background: #27ae60; }
        .test-failed { background: #e74c3c; }
        .test-skipped { background: #95a5a6; }
        
        .footer {
            background: #2c3e50;
            color: white;
            padding: 20px;
            text-align: center;
            font-size: 0.9em;
        }
        
        .footer a {
            color: #3498db;
            text-decoration: none;
        }
        
        @media (max-width: 768px) {
            .timeline::after {
                left: 31px;
            }
            
            .phase-item {
                width: 100%;
                padding-left: 70px;
                padding-right: 25px;
            }
            
            .phase-item:nth-child(even) {
                left: 0;
            }
            
            .phase-status {
                left: 15px !important;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <div class="header">
            <h1>
                <span>🚀 Pipeline Dashboard</span>
                <span class="status-badge ${getStatusClass(metrics.pipeline.overallStatus)}">
                    ${metrics.pipeline.overallStatus}
                </span>
            </h1>
            <p>Build #${metrics.pipeline.buildNumber} | ${metrics.pipeline.projectName} v${metrics.pipeline.projectVersion}</p>
            <p>${metrics.timestamp}</p>
        </div>
        
        <div class="stats-grid">
            <div class="stat-card">
                <h3>📊 Overall Status</h3>
                <div class="stat-value">${metrics.pipeline.overallStatus}</div>
                <p>Duration: ${metrics.pipeline.duration}</p>
            </div>
            
            <div class="stat-card">
                <h3>🧪 Test Results</h3>
                <div class="stat-value">${metrics.tests.passPercentage}%</div>
                <p>Passed: ${metrics.tests.passedTests}/${metrics.tests.totalTests}</p>
                <p>Threshold: ${metrics.tests.threshold}%</p>
            </div>
            
            <div class="stat-card">
                <h3>🔧 Build Info</h3>
                <div class="stat-value">#${metrics.pipeline.buildNumber}</div>
                <p>Node: ${metrics.pipeline.node}</p>
                <p>Job: ${metrics.pipeline.jobName}</p>
            </div>
            
            <div class="stat-card">
                <h3>📦 Deployment</h3>
                <div class="stat-value">${metrics.deployment.status}</div>
                <p>Env: ${metrics.deployment.environment}</p>
                <p>Version: ${metrics.deployment.version}</p>
            </div>
        </div>
        
        <div class="phase-timeline">
            <h2 style="text-align: center; color: #2c3e50; margin-bottom: 30px;">📋 Phase Timeline</h2>
            <div class="timeline">
                ${generateTimelineHTML(metrics.phases)}
            </div>
        </div>
        
        <div class="test-results">
            <h2 style="color: #2c3e50; margin-bottom: 20px;">🧪 Test Results Visualization</h2>
            <div class="test-chart">
                ${generateTestChartHTML(metrics.tests)}
            </div>
            <div style="display: flex; gap: 20px; margin-top: 20px;">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 20px; height: 20px; background: #27ae60;"></div>
                    <span>Passed: ${metrics.tests.passedTests}</span>
                </div>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 20px; height: 20px; background: #e74c3c;"></div>
                    <span>Failed: ${metrics.tests.failedTests}</span>
                </div>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 20px; height: 20px; background: #95a5a6;"></div>
                    <span>Skipped: ${metrics.tests.skippedTests}</span>
                </div>
            </div>
        </div>
        
        ${generateRollbackSectionHTML(metrics.rollback)}
        
        <div class="footer">
            <p>Generated by Jenkins Pipeline Dashboard | ${metrics.timestamp}</p>
            <p><a href="${metrics.pipeline.buildUrl}" target="_blank">View Build Details</a></p>
            <p>Project: ${metrics.pipeline.projectName} v${metrics.pipeline.projectVersion}</p>
        </div>
    </div>
    
    <script>
        // Simple animation for phase items
        document.addEventListener('DOMContentLoaded', function() {
            const phaseItems = document.querySelectorAll('.phase-item');
            phaseItems.forEach((item, index) => {
                setTimeout(() => {
                    item.style.opacity = '1';
                    item.style.transform = 'translateX(0)';
                }, index * 200);
            });
            
            // Add click handlers for phase items
            phaseItems.forEach(item => {
                item.addEventListener('click', function() {
                    const phaseName = this.querySelector('h3').textContent;
                    alert('Phase: ' + phaseName + '\\nStatus: ' + this.querySelector('.phase-status').dataset.status);
                });
            });
        });
    </script>
</body>
</html>
"""
    
    writeFile file: 'reports/dashboard.html', text: htmlContent
    echo "✅ HTML dashboard generated: reports/dashboard.html"
}

private String getStatusClass(String status) {
    switch(status.toLowerCase()) {
        case 'success': return 'status-success'
        case 'failed': return 'status-failed'
        case 'unstable':
        case 'unstable_with_rollback': return 'status-unstable'
        case 'running': return 'status-running'
        default: return 'status-running'
    }
}

private String generateTimelineHTML(Map phases) {
    def timelineHTML = ''
    def phaseOrder = ['SETUP', 'VALIDATION', 'BUILD', 'TESTING', 'DEPLOYMENT']
    
    phaseOrder.eachWithIndex { phaseName, index ->
        def phase = phases[phaseName]
        if (phase) {
            def statusClass = getStatusClass(phase.status.toLowerCase())
            def icon = getPhaseIcon(phaseName)
            
            timelineHTML += """
                <div class="phase-item" style="opacity: 0; transform: translateX(${index % 2 == 0 ? '-20px' : '20px'}); transition: all 0.5s ease;">
                    <div class="phase-status ${statusClass}" data-status="${phase.status}"></div>
                    <h3>${icon} ${phaseName}</h3>
                    <p><strong>Status:</strong> ${phase.status}</p>
                    <p><strong>Duration:</strong> ${phase.duration}</p>
                    <p><strong>Time:</strong> ${phase.startTime} - ${phase.endTime}</p>
                    ${phase.message ? "<p><strong>Message:</strong> ${phase.message}</p>" : ""}
                </div>
            """
        }
    }
    
    return timelineHTML
}

private String getPhaseIcon(String phaseName) {
    switch(phaseName) {
        case 'SETUP': return '🔧'
        case 'VALIDATION': return '🔍'
        case 'BUILD': return '⚙️'
        case 'TESTING': return '🧪'
        case 'DEPLOYMENT': return '📦'
        default: return '📋'
    }
}

private String generateTestChartHTML(Map testMetrics) {
    def total = testMetrics.totalTests.toInteger()
    if (total == 0) return '<div style="width: 100%; text-align: center; padding: 40px;">No tests executed</div>'
    
    def passed = testMetrics.passedTests.toInteger()
    def failed = testMetrics.failedTests.toInteger()
    def skipped = testMetrics.skippedTests.toInteger()
    
    def passedWidth = (passed / total * 100)
    def failedWidth = (failed / total * 100)
    def skippedWidth = (skipped / total * 100)
    
    return """
        <div class="test-passed" style="width: ${passedWidth}%;" title="Passed: ${passed}"></div>
        <div class="test-failed" style="width: ${failedWidth}%;" title="Failed: ${failed}"></div>
        <div class="test-skipped" style="width: ${skippedWidth}%;" title="Skipped: ${skipped}"></div>
    """
}

private String generateRollbackSectionHTML(Map rollbackMetrics) {
    if (rollbackMetrics.required != 'true') {
        return ''
    }
    
    return """
        <div style="padding: 30px; background: #fff3cd; border-top: 3px solid #ffc107;">
            <h2 style="color: #856404; margin-bottom: 20px;">🔄 Rollback Information</h2>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px;">
                <div style="background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #dc3545;">
                    <h3 style="color: #dc3545;">Reason</h3>
                    <p>${rollbackMetrics.reason}</p>
                </div>
                <div style="background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #fd7e14;">
                    <h3 style="color: #fd7e14;">Strategy</h3>
                    <p>${rollbackMetrics.strategy}</p>
                </div>
                <div style="background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #20c997;">
                    <h3 style="color: #20c997;">Status</h3>
                    <p>${rollbackMetrics.status}</p>
                </div>
                <div style="background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #6f42c1;">
                    <h3 style="color: #6f42c1;">Target Commit</h3>
                    <p>${rollbackMetrics.targetCommit?.take(8) ?: 'N/A'}</p>
                </div>
            </div>
        </div>
    """
}

private void generateJSONReport(Map metrics, Map config) {
    def jsonContent = new groovy.json.JsonBuilder(metrics).toPrettyString()
    
    writeFile file: 'reports/dashboard-data.json', text: jsonContent
    echo "✅ JSON report generated: reports/dashboard-data.json"
}

private void generateSummaryReport(Map metrics, Map config) {
    def summaryContent = """
PIPELINE EXECUTION SUMMARY
==========================

EXECUTION DETAILS:
------------------
Project: ${metrics.pipeline.projectName}
Version: ${metrics.pipeline.projectVersion}
Build: #${metrics.pipeline.buildNumber}
Status: ${metrics.pipeline.overallStatus}
Duration: ${metrics.pipeline.duration}
Timestamp: ${metrics.timestamp}
Build URL: ${metrics.pipeline.buildUrl}

PHASE STATUSES:
---------------
${generatePhaseSummary(metrics.phases)}

TEST RESULTS:
-------------
Pass Percentage: ${metrics.tests.passPercentage}%
Total Tests: ${metrics.tests.totalTests}
Passed: ${metrics.tests.passedTests}
Failed: ${metrics.tests.failedTests}
Skipped: ${metrics.tests.skippedTests}
Retry Attempts: ${metrics.tests.retryCount}
Status: ${metrics.tests.status}

DEPLOYMENT:
-----------
Environment: ${metrics.deployment.environment}
Status: ${metrics.deployment.status}
Version: ${metrics.deployment.version}
URL: ${metrics.deployment.url}
Timestamp: ${metrics.deployment.timestamp}

GIT INFORMATION:
----------------
Branch: ${metrics.git.branch}
Commit: ${metrics.git.shortCommit}
Author: ${metrics.git.author}

${metrics.rollback.required == 'true' ? generateRollbackSummary(metrics.rollback) : ''}

PERFORMANCE METRICS:
--------------------
Build Duration: ${metrics.performance.buildDuration}ms
Memory Usage: ${metrics.performance.memoryUsage}
Disk Usage: ${metrics.performance.diskUsage}

ENVIRONMENT:
------------
Java: ${metrics.environment.javaVersion}
Maven: ${metrics.environment.mavenVersion}
OS: ${metrics.environment.os}
Node: ${metrics.pipeline.node}

RECOMMENDATIONS:
---------------
${generateRecommendations(metrics)}

---
Generated by Jenkins Pipeline Dashboard
${metrics.timestamp}
"""
    
    writeFile file: 'reports/pipeline-summary.txt', text: summaryContent
    archiveArtifacts artifacts: 'reports/pipeline-summary.txt', fingerprint: true
    
    echo "✅ Summary report generated: reports/pipeline-summary.txt"
}

private String generatePhaseSummary(Map phases) {
    def summary = []
    phases.each { phaseName, phaseData ->
        summary << "${phaseName}: ${phaseData.status} (${phaseData.duration})"
    }
    return summary.join('\n')
}

private String generateRollbackSummary(Map rollbackMetrics) {
    return """
ROLLBACK EXECUTED:
------------------
Reason: ${rollbackMetrics.reason}
Strategy: ${rollbackMetrics.strategy}
Status: ${rollbackMetrics.status}
Execution Time: ${rollbackMetrics.executionTime}
Target Commit: ${rollbackMetrics.targetCommit?.take(8) ?: 'N/A'}
Verification: ${rollbackMetrics.verificationStatus}

"""
}

private String generateRecommendations(Map metrics) {
    def recommendations = []
    
    if (metrics.pipeline.overallStatus == 'FAILED') {
        recommendations << "- Investigate pipeline failure"
        recommendations << "- Check phase logs for errors"
        recommendations << "- Review test results"
    }
    
    if (metrics.tests.status == 'FAILED') {
        recommendations << "- Fix failing tests"
        recommendations << "- Review test coverage"
        recommendations << "- Consider adjusting test threshold"
    }
    
    if (metrics.rollback.required == 'true') {
        recommendations << "- Address the issues that caused rollback"
        recommendations << "- Verify fixes before next deployment"
        recommendations << "- Review rollback procedure effectiveness"
    }
    
    if (metrics.deployment.status == 'SUCCESS') {
        recommendations << "- Monitor deployment performance"
        recommendations << "- Set up application monitoring"
        recommendations << "- Plan next release cycle"
    }
    
    return recommendations.join('\n')
}

private void archiveDashboardReports() {
    archiveArtifacts artifacts: 'reports/**/*', fingerprint: true
    
    // Also archive specific important files
    def importantFiles = [
        'reports/dashboard.html',
        'reports/dashboard-data.json',
        'reports/pipeline-summary.txt'
    ]
    
    importantFiles.each { file ->
        if (fileExists(file)) {
            archiveArtifacts artifacts: file, fingerprint: true
        }
    }
    
    echo "✅ Dashboard reports archived"
}

private void displayConsoleDashboard(Map metrics) {
    def logger = new PipelineLogger()
    
    logger.section("📊 CONSOLE DASHBOARD")
    
    // Display overall status
    def statusIcon = getStatusIcon(metrics.pipeline.overallStatus)
    echo "${statusIcon} OVERALL STATUS: ${metrics.pipeline.overallStatus}"
    echo "   Build: #${metrics.pipeline.buildNumber}"
    echo "   Duration: ${metrics.pipeline.duration}"
    echo ""
    
    // Display phase statuses
    echo "PHASE STATUSES:"
    echo "---------------"
    metrics.phases.each { phaseName, phaseData ->
        def phaseIcon = getPhaseIcon(phaseName)
        def phaseStatusIcon = getStatusIcon(phaseData.status)
        echo "   ${phaseIcon} ${phaseName}: ${phaseStatusIcon} ${phaseData.status} (${phaseData.duration})"
    }
    echo ""
    
    // Display test results
    echo "TEST RESULTS:"
    echo "-------------"
    def testStatusIcon = getStatusIcon(metrics.tests.status)
    echo "   ${testStatusIcon} Status: ${metrics.tests.status}"
    echo "   📈 Pass Percentage: ${metrics.tests.passPercentage}%"
    echo "   📊 Total Tests: ${metrics.tests.totalTests}"
    echo "   ✅ Passed: ${metrics.tests.passedTests}"
    echo "   ❌ Failed: ${metrics.tests.failedTests}"
    echo "   ⏭️ Skipped: ${metrics.tests.skippedTests}"
    echo ""
    
    // Display deployment info
    echo "DEPLOYMENT:"
    echo "-----------"
    if (metrics.deployment.status != 'NOT_DEPLOYED') {
        def deployStatusIcon = getStatusIcon(metrics.deployment.status)
        echo "   ${deployStatusIcon} Status: ${metrics.deployment.status}"
        echo "   🌍 Environment: ${metrics.deployment.environment}"
        echo "   🏷️ Version: ${metrics.deployment.version}"
        echo "   🔗 URL: ${metrics.deployment.url}"
    } else {
        echo "   ⏭️ Not deployed"
    }
    echo ""
    
    // Display rollback info if applicable
    if (metrics.rollback.required == 'true') {
        echo "ROLLBACK EXECUTED:"
        echo "-----------------"
        echo "   🚨 Reason: ${metrics.rollback.reason}"
        echo "   🔄 Strategy: ${metrics.rollback.strategy}"
        echo "   ✅ Status: ${metrics.rollback.status}"
        echo ""
    }
    
    echo "📄 Reports available in workspace:"
    echo "   - HTML Dashboard: reports/dashboard.html"
    echo "   - JSON Data: reports/dashboard-data.json"
    echo "   - Summary: reports/pipeline-summary.txt"
    echo ""
    echo "🔗 Build URL: ${metrics.pipeline.buildUrl}"
}

private String getStatusIcon(String status) {
    switch(status.toLowerCase()) {
        case 'success': return '✅'
        case 'failed': return '❌'
        case 'unstable':
        case 'unstable_with_rollback': return '⚠️'
        case 'running': return '🔄'
        case 'passed': return '✅'
        case 'skipped': return '⏭️'
        default: return '📝'
    }
}

private void generateBasicDashboard(Map config) {
    def basicContent = """
BASIC PIPELINE DASHBOARD
========================

Build: #${env.BUILD_NUMBER}
Status: ${env.PIPELINE_STATUS ?: 'UNKNOWN'}
Timestamp: ${new Date().format('yyyy-MM-dd HH:mm:ss')}

Note: Full dashboard generation failed. Basic information shown.

For details, check:
- Console output
- Build artifacts
- Phase logs
"""
    
    writeFile file: 'reports/basic-dashboard.txt', text: basicContent
    archiveArtifacts artifacts: 'reports/basic-dashboard.txt', fingerprint: true
    
    echo "⚠️ Basic dashboard generated due to generation failure"
}

return this
