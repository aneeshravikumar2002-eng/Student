pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        APP_NAME = "student-dashboard"
        APP_PORT = "8080"
        APP_URL = "http://localhost:8080"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                url: 'https://gitlab.com/aneeshravikumar2002-group/student.git'
            }
        }

        stage('Build Project') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def mvn = tool 'maven'

                    withSonarQubeEnv('sonarqube') {
                        sh """
                        ${mvn}/bin/mvn clean verify \
                        org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -Dsonar.projectKey=aneeshravikumar2002-group_student_6ce334be-6c38-4c78-9dab-54266f19606b \
                        -Dsonar.projectName='Student'
                        """
                    }
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

        stage('Package Application') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Upload to Nexus') {
            steps {
                sh './mvnw deploy -DskipTests'
            }
        }

        stage('Start Application') {
            steps {
                sh '''
                echo "Stopping old app..."
                pkill -f student-dashboard || true

                echo "Starting app..."
                nohup java -jar target/student-dashboard-0.0.1.jar \
                > app.log 2>&1 &

                sleep 20
                '''
            }
        }

        stage('Install Node Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Run Cypress Tests') {
            steps {
                sh 'npx cypress run'
            }
        }

        stage('Run Selenium Tests') {
            steps {
                sh './mvnw test'
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
        }
    }
}