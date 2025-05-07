pipeline {
    agent any

    stages {
        stage('Build Project') {
            steps {
                sh 'chmod +x ./gradlew' // gradlew 파일에 실행 권한 부여
                sh './gradlew clean build'
            }
        }

        stage('Docker Hub Login and build image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                            docker build -t \$repository:\$BUILD_NUMBER .
                            """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push \$repository:\$BUILD_NUMBER"
            }
        }
    }
}
