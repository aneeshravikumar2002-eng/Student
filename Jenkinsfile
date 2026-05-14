pipeline {
    agent any

    tools {
        maven 'maven'
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

        stage('Package Application') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Start Application') {
            steps {
                sh '''
                echo "Stopping old application..."
                pkill -f student-dashboard || true

                echo "Starting Spring Boot application..."
                nohup java -jar target/*.jar > app.log 2>&1 &

                echo "Waiting for application startup..."
                sleep 30

                echo "Checking application..."
                curl http://localhost:8080 || true
                '''
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
                sh './mvnw test -Dtest=LoginTest'
            }
        }

        stage('Upload to Nexus') {
            steps {
                sh './mvnw deploy -DskipTests'
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

        always {
            sh '''
            echo "Stopping application..."
            pkill -f student-dashboard || true
            '''
        }
    }
}