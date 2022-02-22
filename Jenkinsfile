pipeline {
    agent any

    stages('Deploy') {
        stage('Build') {
            steps {
                setBuildStatus("?", "PENDING")
                withMaven(
                        maven: 'maven-3.6.3'
                ) {
                    sh 'mvn -B clean install'
                }
            }
        }
        stage('Deliver') {
            environment {
                DATA = '/var/opex/demo-core'
                PANEL_PASS = credentials("v-panel-secret")
                BACKEND_USER = credentials("v-backend-secret")
                SMTP_PASS = credentials("smtp-secret")
                DB_USER = 'opex'
                DB_PASS = credentials("db-secret")
                DB_BACKUP_USER = 'opex_backup'
                DB_BACKUP_PASSWORD = credentials("db-backup-secret")
                KEYCLOAK_ADMIN_URL = 'https://demo.opex.dev/auth'
                KEYCLOAK_FRONTEND_URL = 'https://demo.opex.dev/auth'
                COMPOSE_PROJECT_NAME = 'demo-core'
                DEFAULT_NETWORK_NAME = 'demo-opex'
            }
            steps {
                sh 'docker-compose up -d --build --remove-orphans'
                sh 'docker image prune -f'
                sh 'docker network prune -f'
            }
        }
    }

    post {
        always {
            echo 'One way or another, I have finished'
        }
        success {
            echo ':)'
            setBuildStatus(":)", "SUCCESS")
        }
        unstable {
            echo ':/'
            setBuildStatus(":/", "UNSTABLE")
        }
        failure {
            echo ':('
            setBuildStatus(":(", "FAILURE")
        }
        changed {
            echo 'Things were different before...'
        }
    }
}

void setBuildStatus(String message, String state) {
    step([
            $class            : "GitHubCommitStatusSetter",
            reposSource       : [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/opexdev/OPEX-Core"],
            contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
            errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ])
}
