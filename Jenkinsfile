pipeline {
    agent any
    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service"
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
                        echo "No relevant changes detected. Skipping build."
                        return
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
                    if (env.SERVICES_TO_BUILD?.trim()) {
                        env.SERVICES_TO_BUILD.split(',').each { service ->
                            echo "Running tests for ${service}..."
                            sh "cd ${service} && mvn test"
                            
                            junit "**/${service}/target/surefire-reports/*.xml"

                            def coverageFile = "${service}/target/site/cobertura/coverage.xml"
                            if (fileExists(coverageFile)) {
                                cobertura coberturaReportFile: "**/${service}/target/site/cobertura/coverage.xml"
                            } else {
                                echo "Coverage report not found for ${service}, skipping..."
                            }
                        }
                    } else {
                        echo "No services to build, skipping test stage."
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    if (env.SERVICES_TO_BUILD?.trim()) {
                        env.SERVICES_TO_BUILD.split(',').each { service ->
                            echo "Building ${service}..."
                            sh "cd ${service} && mvn clean package"
                        }
                    } else {
                        echo "No services to build, skipping build stage."
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
