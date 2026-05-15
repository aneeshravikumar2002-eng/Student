pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        APP_URL = "http://15.207.10.227:8080"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://gitlab.com/aneeshravikumar2002-group/student.git'
            }
        }

        stage('Build + SonarQube') {
            steps {
                script {
                    def mvn = tool 'maven'

                    withSonarQubeEnv('sonarqube') {
                        sh """
                        ${mvn}/bin/mvn clean verify \
                        org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -DskipUITests=true \
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

        stage('Package + Upload Nexus') {
            steps {
                sh '''
                ./mvnw clean package -DskipTests
                ./mvnw deploy -DskipTests
                '''
            }
        }

        stage('Start Application') {
            steps {
                sh '''
                echo "Stopping old app..."
                pkill -f student-dashboard || true

                echo "Starting application..."
                nohup java -jar target/student-dashboard-0.0.1-SNAPSHOT.jar \
                > app.log 2>&1 &

                echo "Waiting for application..."

                for i in {1..20}
                do
                    if curl -I ${APP_URL} > /dev/null 2>&1; then
                        echo "Application is UP"
                        exit 0
                    fi

                    sleep 5
                done

                echo "Application failed to start"
                cat app.log
                exit 1
                '''
            }
        }

        stage('Selenium and cypress Tests') {
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