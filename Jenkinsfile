pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DEV_IP  = "13.205.120.107"
        DEV_URL = "http://${DEV_IP}:8000"

        NEXUS_URL = "http://13.200.74.102:8081"
        REPO = "maven-snapshots"

        APP_NAME = "student-dashboard"
        GROUP = "com/aneeshravikumar2002-group"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                url: 'https://gitlab.com/aneeshravikumar2002-group/student.git'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                    ./mvnw clean verify sonar:sonar \
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
                sh """
                ./mvnw clean package deploy \
                -DskipTests \
                -Drevision=${BUILD_NUMBER}-SNAPSHOT
                """
            }
        }

        stage('Deploy to DEV') {
            steps {
                sh '''
                scp -o StrictHostKeyChecking=no \
                target/*.jar \
                ubuntu@${DEV_IP}:/opt/student-dashboard/student-dashboard.jar

                ssh -o StrictHostKeyChecking=no \
                ubuntu@${DEV_IP} '
                sudo systemctl restart student-dashboard
                sudo systemctl status student-dashboard --no-pager
                '
                '''
            }
        }

        stage('Verify DEV Deployment') {
            steps {
                sh '''
                echo "Verifying DEV deployment..."
                sleep 10

                curl -f ${DEV_URL} > /dev/null || {
                    echo "DEV deployment failed"
                    exit 1
                }
                '''
            }
        }

        stage('Selenium and Cypress Tests') {
            steps {
                sh """
                npm install

                CYPRESS_BASE_URL=${DEV_URL} npx cypress run

                ./mvnw test -Dtest=LoginTest
                """
            }
        }
    }

    post {

        success {
            echo 'Pipeline Success'
        }

        failure {

            echo 'Pipeline Failed - Starting Auto Rollback'

            sh '''
            CURRENT=${BUILD_NUMBER}-SNAPSHOT

            PREVIOUS=$(curl -s \
            ${NEXUS_URL}/service/rest/v1/search?repository=${REPO}\&name=${APP_NAME} \
            | jq -r '.items[].version' \
            | grep SNAPSHOT \
            | sort -V \
            | grep -v ${CURRENT} \
            | tail -1)

            echo "Rollback Version: $PREVIOUS"

            wget -O rollback.jar \
            ${NEXUS_URL}/repository/${REPO}/${GROUP}/${APP_NAME}/$PREVIOUS/${APP_NAME}-$PREVIOUS.jar

            scp -o StrictHostKeyChecking=no \
            rollback.jar \
            ubuntu@${DEV_IP}:/opt/student-dashboard/student-dashboard.jar

            ssh -o StrictHostKeyChecking=no \
            ubuntu@${DEV_IP} '
            sudo systemctl restart student-dashboard
            sudo systemctl status student-dashboard --no-pager
            '

            echo "Rollback completed"
            '''
        }
    }
}