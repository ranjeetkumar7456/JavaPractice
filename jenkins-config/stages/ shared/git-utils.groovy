// ========== GIT UTILITIES ==========

def cloneRepository(String repoUrl, String branch) {
    echo "Cloning repository: ${repoUrl}"
    sh """
        git clone -b ${branch} ${repoUrl} .
    """
}

def revertToCommit(String commitHash, gitConfig) {
    withCredentials([usernamePassword(
        credentialsId: gitConfig.CREDENTIALS_ID,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        sh """
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins"
            
            # Revert to specified commit
            git revert ${commitHash}..HEAD --no-edit
            
            # If revert fails, try reset
            if [ \$? -ne 0 ]; then
                echo "Revert failed, trying reset..."
                git reset --hard ${commitHash}
            fi
        """
    }
}

def pushChanges(gitConfig) {
    withCredentials([usernamePassword(
        credentialsId: gitConfig.CREDENTIALS_ID,
        passwordVariable: 'GIT_PASSWORD',
        usernameVariable: 'GIT_USERNAME'
    )]) {
        sh """
            git push origin HEAD:${gitConfig.BRANCH} --force
        """
    }
}

def getCommitInfo() {
    return [
        hash: sh(script: 'git rev-parse HEAD', returnStdout: true).trim(),
        shortHash: sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim(),
        author: sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim(),
        message: sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim(),
        date: sh(script: 'git log -1 --pretty=format:"%cd"', returnStdout: true).trim()
    ]
}

return this
