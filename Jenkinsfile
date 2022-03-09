pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "jdk17"
            }
            steps {
                sh 'gradle publish'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}
