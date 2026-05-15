pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DEV_URL = "http://13.205.120.107:8000"
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

        stage('Deploy to DEV') {
            steps {
                sh '''
                scp -o StrictHostKeyChecking=no \
                target/*.jar \
                ubuntu@DEV_IP:/opt/student-dashboard/student-dashboard.jar

                ssh -o StrictHostKeyChecking=no \
                ubuntu@DEV_IP '
                sudo systemctl restart student-dashboard
                sudo systemctl status student-dashboard --no-pager
                '
                '''
            }
        }

        stage('Verify DEV Deployment') {
            steps {
                sh '''
                sleep 10

                curl -s ${DEV_URL} > /dev/null || {
                    echo "DEV deployment failed"
                    exit 1
                }
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
        success {
            echo 'Pipeline Success'
        }

        failure {
            echo 'Pipeline Failed'
        }
    }
}