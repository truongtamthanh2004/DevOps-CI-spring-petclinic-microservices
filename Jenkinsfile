pipeline {
    agent any
    environment {
        SERVICES = "vets-service,customers-service,visits-service"
    }
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/truongtamthanh2004/DevOps-CI-spring-petclinic-microservices.git'
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    def servicesToBuild = []
                    
                    SERVICES.split(',').each { service ->
                        if (changedFiles.any { it.startsWith(service.trim()) }) {
                            servicesToBuild.add(service.trim())
                        }
                    }

                    if (servicesToBuild.isEmpty()) {
                        currentBuild.result = 'SUCCESS'
                        error("No relevant changes detected. Skipping build.")
                    } else {
                        env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                        echo "Services to build: ${env.SERVICES_TO_BUILD}"
                    }
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    env.SERVICES_TO_BUILD.split(',').each { service ->
                        echo "Running tests for ${service}..."
                        sh "cd ${service} && mvn test"
                        junit "**/${service}/target/surefire-reports/*.xml"
                        cobertura coberturaReportFile: "**/${service}/target/site/cobertura/coverage.xml"
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    env.SERVICES_TO_BUILD.split(',').each { service ->
                        echo "Building ${service}..."
                        sh "cd ${service} && mvn clean package"
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: "**/target/*.jar", fingerprint: true
        }
    }
}
