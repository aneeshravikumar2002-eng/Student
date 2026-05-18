# Student Dashboard DevOps Project

## Project Overview

This project demonstrates a complete end-to-end DevOps CI/CD pipeline for a Spring Boot application (`student-dashboard`) using industry-standard DevOps tools.

The goal of this project is to automate:

* Source Code Management
* Continuous Integration
* Code Quality Analysis
* Artifact Management
* Automated UI Testing
* Application Deployment
* Monitoring

### CI/CD Flow

```text
GitLab
↓
Webhook Trigger
↓
Jenkins Pipeline
↓
Build
↓
SonarQube Analysis
↓
Quality Gate
↓
Package JAR
↓
Upload Artifact to Nexus
↓
Deploy to App Server
↓
Start Application
↓
Health Check
↓
Cypress Testing
↓
Selenium Testing
↓
Monitoring
```

---

# Architecture

## EC2 Infrastructure

### EC2-1 — Jenkins Server

Services:

* Jenkins
* Maven
* Java 21
* Git
* Node.js
* Cypress
* Chrome
* ChromeDriver

Purpose:

* Pipeline execution
* Build automation
* SonarQube trigger
* Nexus upload
* Cypress testing
* Selenium testing
* Application deployment to App Server

---

### EC2-2 — SonarQube + Nexus Server

Services:

* SonarQube
* Nexus Repository Manager

Purpose:

* Code Quality Analysis
* Quality Gate Validation
* Artifact Repository

---

### EC2-3 — Application Server

Services:

* Java 21
* Spring Boot Application

Purpose:

* Hosts deployed Spring Boot application

---

### EC2-4 — Monitoring Server

Services:

* Prometheus
* Grafana
* Node Exporter

Purpose:

* Infrastructure Monitoring
* Server Metrics Visualization

---

# Technology Stack

| Category               | Tool        |
| ---------------------- | ----------- |
| Source Code Management | GitLab      |
| CI/CD                  | Jenkins     |
| Build Tool             | Maven       |
| Code Quality           | SonarQube   |
| Artifact Repository    | Nexus       |
| Backend                | Spring Boot |
| UI Automation          | Cypress     |
| Browser Automation     | Selenium    |
| Monitoring             | Prometheus  |
| Dashboard              | Grafana     |
| Cloud Platform         | AWS EC2     |
| Language               | Java 21     |

---

# Project Repository

GitLab Repository:

`https://gitlab.com/aneeshravikumar2002-group/student.git`

---

# Step 1 — Create EC2 Instances

Create 4 Ubuntu EC2 instances.

Recommended Configuration:

| Server | Purpose           | RAM    |
| ------ | ----------------- | ------ |
| EC2-1  | Jenkins           | 4–8 GB |
| EC2-2  | SonarQube + Nexus | 8 GB   |
| EC2-3  | App Server        | 2–4 GB |
| EC2-4  | Monitoring        | 4 GB   |

Assign Elastic IPs to avoid public IP changes.

---

# Step 2 — Jenkins Setup

Install Java:

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
java -version
```

Install Jenkins:

```bash
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
/usr/share/keyrings/jenkins-keyring.asc > /dev/null

sudo echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
/etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt update
sudo apt install jenkins -y
```

Start Jenkins:

```bash
sudo systemctl enable jenkins
sudo systemctl start jenkins
```

Access:

```text
http://<JENKINS-IP>:8080
```

---

# Step 3 — SonarQube Setup

Install Docker:

```bash
sudo apt update
sudo apt install docker.io -y
sudo systemctl start docker
```

Run SonarQube:

```bash
docker run -d \
--name sonarqube \
-p 9000:9000 sonarqube:lts-community
```

Access:

```text
http://<SONAR-IP>:9000
```

Default Credentials:

```text
Username: admin
Password: admin
```

Generate Sonar Token.

Configure in Jenkins:

```text
Manage Jenkins
→ System
→ SonarQube Servers
```

---

# Step 4 — Nexus Setup

Run Nexus:

```bash
docker run -d \
--name nexus \
-p 8081:8081 sonatype/nexus3
```

Access:

```text
http://<NEXUS-IP>:8081
```

Create:

* maven-releases
* maven-snapshots

Jenkins Credentials:

```text
ID: nexus-cred
Username: <nexus-username>
Password: <nexus-password>
```

Maven settings.xml:

```xml
<settings>
    <servers>
        <server>
            <id>nexus-cred</id>
            <username>username</username>
            <password>password</password>
        </server>
    </servers>
</settings>
```

---

# Step 5 — App Server Setup

Install Java:

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
```

Deploy application:

```bash
scp target/*.jar ubuntu@<APP-IP>:/opt/student
```

Start Application:

```bash
java -jar student-dashboard.jar
```

Check:

```bash
curl http://localhost:8080
```

---

# Step 6 — Nexus Configuration (Detailed)

## Why Nexus?

Nexus Repository Manager is used to store build artifacts.

In this project:

* Maven JAR files are stored in Nexus
* Jenkins uploads artifacts automatically
* Snapshots are used for repeated CI/CD deployments

---

## Install Nexus

Run Nexus using Docker:

```bash
sudo docker run -d \
--name nexus \
-p 8081:8081 \
sonatype/nexus3
```

Check container:

```bash
docker ps
```

View logs:

```bash
docker logs -f nexus
```

Access Nexus:

```text
http://<NEXUS-IP>:8081
```

---

## Get Initial Admin Password

```bash
docker exec -it nexus cat /nexus-data/admin.password
```

Login:

```text
Username: admin
Password: generated password
```

---

## Create Maven Repositories

Go to:

```text
Settings
→ Repositories
→ Create Repository
```

Create:

### Maven Releases

Repository Type:

```text
maven2 (hosted)
```

Settings:

```text
Name: maven-releases
Version Policy: Release
Deployment Policy: Disable Redeploy
```

---

### Maven Snapshots

Repository Type:

```text
maven2 (hosted)
```

Settings:

```text
Name: maven-snapshots
Version Policy: Snapshot
Deployment Policy: Allow Redeploy
```

---

## Configure Jenkins Credentials

Go to:

```text
Manage Jenkins
→ Credentials
→ Global
→ Add Credentials
```

Add:

```text
ID: nexus-cred
Username: admin
Password: ********
```

---

## Configure Maven settings.xml

Location:

```bash
sudo nano /var/lib/jenkins/.m2/settings.xml
```

Add:

```xml
<settings>
    <servers>
        <server>
            <id>nexus-cred</id>
            <username>admin</username>
            <password>password</password>
        </server>
    </servers>
</settings>
```

Important:

The ID must match `pom.xml`.

---

## Configure pom.xml

```xml
<distributionManagement>

    <repository>
        <id>nexus-cred</id>
        <name>Nexus Releases</name>
        <url>
            http://<NEXUS-IP>:8081/repository/maven-releases/
        </url>
    </repository>

    <snapshotRepository>
        <id>nexus-cred</id>
        <name>Nexus Snapshots</name>
        <url>
            http://<NEXUS-IP>:8081/repository/maven-snapshots/
        </url>
    </snapshotRepository>

</distributionManagement>
```

---

## Jenkins Nexus Upload Stage

```groovy
stage('Upload to Nexus') {
    steps {
        sh './mvnw deploy -DskipTests'
    }
}
```

Manual Test:

```bash
./mvnw deploy -DskipTests
```

Debug Mode:

```bash
./mvnw deploy -X
```

---

## Common Nexus Errors

### 401 Unauthorized

Cause:

* Wrong credentials
* Wrong repository ID

Fix:

Verify:

```xml
<id>nexus-cred</id>
```

matches in both:

* pom.xml
* settings.xml

---

### Redeploy Not Allowed

Error:

```text
asset already exists
redeploy is not allowed
```

Fix:

Use:

```xml
<version>0.0.1-SNAPSHOT</version>
```

instead of releases.

---

# Step 7 — Cypress Setup (Detailed)

## Why Cypress?

Cypress is used for UI testing.

It validates:

* Login page loads
* UI functionality
* Browser-based validation

In this project, Jenkins runs Cypress automatically.

---

## Install Node.js

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install nodejs -y
```

Check:

```bash
node -v
npm -v
```

---

## Install Cypress

Inside project:

```bash
npm install cypress --save-dev
```

Verify:

```bash
npx cypress --version
```

Open UI:

```bash
npx cypress open
```

Headless Mode:

```bash
npx cypress run
```

Used in Jenkins.

---

## Cypress Config

File:

```text
cypress.config.js
```

Example:

```javascript
const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://<APP-IP>:8080'
  }
})
```

---

## Cypress Test Example

File:

```text
cypress/e2e/login.cy.js
```

```javascript
describe('Student Dashboard Test', () => {

  it('Login page should open', () => {
    cy.visit('/')
    cy.contains('Student Dashboard')
  })

})
```

---

## Jenkins Stage

```groovy
stage('Run Cypress Tests') {
    steps {
        sh 'npx cypress run'
    }
}
```

---

## Common Cypress Error — ETIMEDOUT

Cause:

* App server unreachable
* Wrong Elastic IP
* Security Group issue
* App not running

Verification:

From Jenkins EC2:

```bash
curl http://<APP-IP>:8080
```

---

# Step 8 — Selenium Setup (Detailed)

## Why Selenium?

Selenium performs browser automation testing.

Used for:

* Login testing
* Form testing
* UI validation
* End-to-end browser simulation

---

## Selenium Dependencies

pom.xml:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.33.0</version>
</dependency>

<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.9.2</version>
</dependency>
```

---

## Browser Setup

Chrome browser and ChromeDriver are automatically managed by Selenium/WebDriverManager in this project.

No manual Chrome installation is required because Jenkins server already has browser support configured.

Verification:

````bash
./mvnw test
```bash
google-chrome --version
````

---

## Selenium Test Example

Location:

```text
src/test/java/LoginTest.java
```

Example:

```java
package com.aneesh.studentdashboard;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class LoginTest {

    @Test
    public void loginTest() {

        WebDriver driver = new ChromeDriver();

        driver.get("http://<APP-IP>:8080");

        System.out.println(driver.getTitle());

        driver.quit();
    }
}
```

---

## Jenkins Stage

```groovy
stage('Run Selenium Tests') {
    steps {
        sh './mvnw test'
    }
}
```

---

## Common Selenium Error — ERR_CONNECTION_REFUSED

Cause:

* App not running
* Wrong IP
* App server inaccessible

Fix:

```bash
curl http://<APP-IP>:8080
```

Verify:

```bash
sudo lsof -i :8080
```

Increase startup wait:

```groovy
sleep 30
```

---

# Step 9 — Cypress Setup

Install Node.js:

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install nodejs -y
```

Install Cypress:

```bash
npm install cypress --save-dev
```

Run:

```bash
npx cypress run
```

---

# Step 7 — Selenium Setup

Selenium dependencies are managed through Maven.

No manual Chrome installation required.

---

# Jenkins Pipeline Flow

```text
Checkout Code
↓
Build Project
↓
SonarQube Analysis
↓
Quality Gate
↓
Package Application
↓
Upload to Nexus
↓
Deploy to App Server
↓
Start Application
↓
Health Check
↓
Run Cypress Tests
↓
Run Selenium Tests
```

---

# Security Group Configuration

| Service     | Port |
| ----------- | ---- |
| SSH         | 22   |
| Jenkins     | 8080 |
| SonarQube   | 9000 |
| Nexus       | 8081 |
| Spring Boot | 8080 |
| Prometheus  | 9090 |
| Grafana     | 3000 |

---

# Common Errors & Fixes

## Selenium Error — ERR_CONNECTION_REFUSED

Cause:

* App server not running
* Wrong IP
* Port inaccessible

Fix:

```bash
curl http://<APP-IP>:8080
```

Start app and verify port.

---

## Cypress Error — ETIMEDOUT

Cause:

* Jenkins cannot reach app server

Fix:

* Open security group port 8080
* Verify app running
* Verify Elastic IP

---

## Nexus Error — 401 Unauthorized

Cause:

* Wrong credentials
* settings.xml issue

Fix:

Verify:

```xml
<id>nexus-cred</id>
```

matches in both files.

---

## Nexus Error — Redeploy Not Allowed

Cause:

Same release version uploaded again.

Fix:

Use snapshots:

```xml
<version>0.0.1-SNAPSHOT</version>
```

---

## Sonar Error — report-task.txt not found

Cause:

Earlier stage failure.

Fix:

Resolve previous pipeline errors.

---

# Credentials Placeholder

## Jenkins

```text
URL: http://<JENKINS-IP>:8080
Username: <username>
Password: <password>
```

## SonarQube

```text
URL: http://<SONAR-IP>:9000
Token: <SONAR_TOKEN>
```

## Nexus

```text
URL: http://<NEXUS-IP>:8081
Credential ID: nexus-cred
Username: <username>
Password: <password>
```

## App Server

```text
Host: <APP-IP>
User: ubuntu
Key: .pem file
```

---

# Monitoring

## Prometheus

```text
http://<MONITORING-IP>:9090
```

## Grafana

```text
http://<MONITORING-IP>:3000
```

Default:

```text
admin/admin
```


Key achievements:

* Automated build and testing
* Integrated code quality analysis
* Configured artifact repository
* Implemented UI testing
* Automated deployment
* Configured monitoring and observability
* Troubleshot real-world CI/CD failures

---

# Author

Aneesh Ravikumar

GitLab:

[https://gitlab.com/aneeshravikumar2002-group/student.git](https://gitlab.com/aneeshravikumar2002-group/student.git)
