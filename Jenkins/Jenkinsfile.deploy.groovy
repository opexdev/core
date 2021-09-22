pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                withMaven(
                        maven: 'maven-latest'
                ) {
                    dir("Utility") {
                        sh 'mvn -B  clean install'
                    }

                    dir("MatchingEngine") {
                        sh 'mvn -B  clean install'
                    }

                    dir("MatchingGateway") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Accountant") {
                        sh 'mvn -B  clean install'
                    }

                    dir("EventLog") {
                        sh 'mvn -B  clean install'
                    }

                    dir("UserManagement") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Wallet") {
                        sh 'mvn -B  clean install'
                    }

                    dir("Api") {
                        sh 'mvn -B  clean install'
                    }

                    dir("BlockchainGateway") {
                        sh 'mvn -B  clean install'
                    }
                }

            }
        }
        stage('Deliver') {
           environment {
              DATA = '/var/opex/runtime'
           }
           steps {
              dir("Deployment") {
                sh 'docker-compose build'
                sh 'docker-compose up -d'
              }
           }
        }
    }
}
