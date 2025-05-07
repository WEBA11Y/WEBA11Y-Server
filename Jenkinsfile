pipeline {
    agent any

    stages {
        stage('Build Spring Boot Project') {
            steps {
                sh 'chmod +x ./gradlew' // gradlew 파일에 실행 권한 부여
                sh './gradlew clean build -x test'
            }
        }
        stage('Docker Hub Login and build image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        def imageTag = "${DOCKER_USER}/${repository}:${BUILD_NUMBER}"
                        sh """
                            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                            docker build -t ${imageTag} .
                        """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    def imageTag = "${DOCKER_USER}/${repository}:${BUILD_NUMBER}"
                    sh "docker push ${imageTag}"
                }
            }
        }
    }

    post {
        always {
            sh "docker logout"
        }
        success {
            echo "✅ 배포 성공"
        }
        failure {
            echo "❌ 배포 실패"
        }
    }
}
