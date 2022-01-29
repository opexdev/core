pipeline {
    agent any

    stages('Deploy - Stage') {
        stage('Build') {
            steps {
                setBuildStatus("?", "PENDING")
                withMaven(
                        maven: 'maven-latest'
                ) {
                    sh 'mvn -B clean install'
                }
            }
        }
        stage('Deliver') {
            environment {
                DATA = '/var/opex/runtime-dev'
                COMPOSE_PROJECT_NAME = 'dev'
                DEFAULT_NETWORK_NAME = 'dev-opex'
            }
            steps {
                sh 'docker-compose up -f docker-compose.yml -f docker-compose.dev.yml -d --build --remove-orphans'
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
