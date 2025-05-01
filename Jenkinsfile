pipeline {
    agent any

    environment {
        dockerImage = ''
    }

    stages {
        stage('Build Project') {
            steps {
                bat './gradlew clean build'
            }
        }

        stage('Docker Hub Login and build image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat """
                            echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                            docker build -t $repository:%BUILD_NUMBER% .
                            """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                bat "docker push $repository:$BUILD_NUMBER"
            }
        }
    }
}
