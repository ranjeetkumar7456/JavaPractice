pipeline {

    agent any

    /***********************
     * AUTOMATIC TRIGGER
     ***********************/
    triggers {
        // Runs every 1 minute (optional)
        cron('* * * * *')
    }

    /***********************
     * GLOBAL OPTIONS
     ***********************/
    options {
        timestamps()
        disableConcurrentBuilds()
        ansiColor('xterm')
    }

    /***********************
     * ALL PIPELINE STAGES
     ***********************/
    stages {

        /****************************
         * 1. CHECKOUT SOURCE CODE
         ****************************/
        stage('Checkout Code') {
            steps {
                echo '=== CHECKOUT CODE FROM GITHUB USING PERSONAL ACCESS TOKEN ==='
                withCredentials([string(credentialsId: 'github-pat', variable: 'GIT_PAT')]) {
                    bat '''
                        echo Deleting any old repo folder...
                        if exist repo rmdir /s /q repo

                        echo Cloning latest code from GitHub...
                        "C:\\Program Files\\Git\\bin\\git.exe" clone -b Java8Feature https://ranjeetkumar7456:%GIT_PAT%@github.com/ranjeetkumar7456/JavaPractice.git repo

                        if %ERRORLEVEL% neq 0 (
                            echo Git Clone Failed!
                            exit /b 1
                        )
                    '''
                }
            }
        }

        /****************************
         * 2. COMPILE JAVA CLASSES
         ****************************/
        stage('Compile Java Classes') {
            steps {
                echo '=== COMPILING ALL JAVA CLASSES ==='
                bat '''
                    if not exist repo\\bin mkdir repo\\bin

                    echo Compiling Java source files...
                    javac -d repo\\bin repo\\src\\main\\java\\Java8Examples\\*.java

                    if %ERRORLEVEL% neq 0 (
                        echo Java Compilation Failed!
                        exit /b 1
                    )
                '''
            }
        }

        /****************************
         * 3. RUN ALL JAVA MODULES
         ****************************/
        stage('Execute Java Modules') {
            steps {
                echo '=== EXECUTING ALL JAVA PROGRAMS ==='

                bat 'java -cp repo\\bin Java8Examples.EmployeeDataProcessor'
                bat 'java -cp repo\\bin Java8Examples.FinancialCalculator'
                bat 'java -cp repo\\bin Java8Examples.InventoryManagementSystem'
                bat 'java -cp repo\\bin Java8Examples.MainExecutor'
                bat 'java -cp repo\\bin Java8Examples.OrderProcessingSystem'
            }
        }

        /****************************
         * 4. EXTENT REPORT PLACEHOLDER
         ****************************/
        stage('Generate Report') {
            steps {
                echo '=== GENERATING EXTENT REPORT (CUSTOM LOGIC CAN BE ADDED HERE) ==='
            }
        }
    }

    /***********************
     * POST BUILD ACTIONS
     ***********************/
    post {

        /****************************
         * ALWAYS RUN
         ****************************/
        always {
            echo '=== CLEANING UP WORKSPACE & FINAL LOGS ==='
        }

        /****************************
         * SUCCESS EMAIL
         ****************************/
        success {
            echo '=== BUILD SUCCESS → SENDING EMAIL ==='

            emailext (
                subject: "✅ Walmart Automation Execution Report - Build #${BUILD_NUMBER}",
                to: "ranjeetkumar7456@gmail.com, qa-team@walmart.com",
                attachmentsPattern: "repo/bin/*.class",
                body: """
                <html>
                <body style="font-family: Arial; background:#f4f6f7; padding:15px;">
                    <h2 style="color:#0071ce;">Walmart Automation Build Success</h2>
                    <p>Hello Team,</p>
                    <p>The automation pipeline executed successfully.</p>

                    <table border="1" cellpadding="6" style="border-collapse: collapse;">
                        <tr><td><b>Project</b></td><td>Walmart Digital Platform</td></tr>
                        <tr><td><b>Branch</b></td><td>Java8Feature</td></tr>
                        <tr><td><b>Build Number</b></td><td>${BUILD_NUMBER}</td></tr>
                        <tr><td><b>Build URL</b></td><td><a href="${BUILD_URL}">${BUILD_URL}</a></td></tr>
                        <tr><td><b>Status</b></td><td><b style="color:green;">SUCCESS</b></td></tr>
                        <tr><td><b>Date/Time</b></td><td>${new Date()}</td></tr>
                    </table>

                    <p>Regards,<br><b>Walmart QA Automation Team</b></p>
                </body>
                </html>
                """
            )
        }

        /****************************
         * FAILURE EMAIL
         ****************************/
        failure {
            echo '=== BUILD FAILED → SENDING EMAIL ==='

            emailext (
                subject: "❌ Walmart Automation Test Execution Failed - Build #${BUILD_NUMBER}",
                to: "ranjeetkumar7456@gmail.com, qa-team@walmart.com",
                attachmentsPattern: "repo/bin/*.class",
                body: """
                <html>
                <body style="font-family: Arial; background:#fff2f2; padding:15px;">
                    <h2 style="color:red;">Walmart Automation Build Failure</h2>
                    <p>Hello Team,</p>
                    <p>The Jenkins build has failed. Please check logs.</p>

                    <table border="1" cellpadding="6" style="border-collapse: collapse;">
                        <tr><td><b>Project</b></td><td>Walmart Digital Platform</td></tr>
                        <tr><td><b>Branch</b></td><td>Java8Feature</td></tr>
                        <tr><td><b>Build Number</b></td><td>${BUILD_NUMBER}</td></tr>
                        <tr><td><b>Build URL</b></td><td><a href="${BUILD_URL}">${BUILD_URL}</a></td></tr>
                        <tr><td><b>Status</b></td><td><b style="color:red;">FAILED</b></td></tr>
                        <tr><td><b>Date/Time</b></td><td>${new Date()}</td></tr>
                    </table>

                    <p>Regards,<br><b>Walmart QA Automation Team</b></p>
                </body>
                </html>
                """
            )
        }
    }
}
