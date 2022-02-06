pipeline {
    agent any

    stages('Deploy - Stage') {
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
                DATA = '/var/opex/dev-core'
                PANEL_PASS = credentials("v-panel-secret-dev")
                BACKEND_USER = credentials("v-backend-secret-dev")
                SMTP_PASS = credentials("smtp-secret-dev")
                DB_USER = 'opex'
                DB_PASS = credentials("db-secret-dev")
                COMPOSE_PROJECT_NAME = 'dev-core'
                DEFAULT_NETWORK_NAME = 'dev-opex'
            }
            steps {
                sh 'docker-compose up -f docker-compose.yml -f docker-compose.dev.yml -d --build --remove-orphans'
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
