pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        APP_URL = "http://15.207.10.227:8000"
    }

    stages {

        stage('Build + Sonar') {
            steps {
                git branch: 'main',
                url: 'https://gitlab.com/aneeshravikumar2002-group/student.git'

                withSonarQubeEnv('sonarqube') {
                    sh '''
                    ./mvnw clean test sonar:sonar \
                    -Dtest=!LoginTest \
                    -Dsonar.projectKey=aneeshravikumar2002-group_student_6ce334be-6c38-4c78-9dab-54266f19606b \
                    -Dsonar.projectName=Student
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package + Nexus') {
            steps {
                sh './mvnw clean package deploy -DskipTests'
            }
        }

        stage('Start Application') {
            steps {
                sh '''
                pkill -f student-dashboard || true

                nohup java -jar target/*.jar > app.log 2>&1 &

                echo "Waiting for application..."

                for i in {1..20}; do
                    curl -I ${APP_URL} && exit 0
                    sleep 5
                done

                cat app.log
                exit 1
                '''
            }
        }

        stage('Selenium and Cypress Tests') {
            steps {
                sh '''
                npm install
                npx cypress run
                ./mvnw test -Dtest=LoginTest
                '''
            }
        }
    }

    post {
        always {
            sh 'pkill -f student-dashboard || true'
        }

        success {
            echo 'Pipeline Success'
        }

        failure {
            echo 'Pipeline Failed'
            sh 'cat app.log || true'
        }
    }
}