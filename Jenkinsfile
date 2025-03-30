// pipeline {
//     agent any
//     environment {
//         SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service"
//     }
//     stages {
//         stage('Checkout') {
//             steps {
//                 git 'https://github.com/truongtamthanh2004/DevOps-CI-spring-petclinic-microservices.git'
//             }
//         }
//         stage('Detect Changes') {
//             steps {
//                 script {
//                     def changedFiles = []
//                     def hasPreviousCommit = sh(script: 'git rev-parse HEAD~1', returnStatus: true) == 0
                    
//                     if (hasPreviousCommit) {
//                         changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
//                     } else {
//                         echo "No previous commit found. Assuming all services changed."
//                         changedFiles = SERVICES.split(',')
//                     }

//                     def servicesToBuild = []
//                     SERVICES.split(',').each { service ->
//                         if (changedFiles.any { it.startsWith(service.trim()) }) {
//                             servicesToBuild.add(service.trim())
//                         }
//                     }

//                     if (servicesToBuild.isEmpty()) {
//                         echo "No relevant changes detected. Skipping build."
//                         currentBuild.result = 'SUCCESS'
//                         return
//                     } else {
//                         env.SERVICES_TO_BUILD = servicesToBuild.join(',')
//                         echo "Services to build: ${env.SERVICES_TO_BUILD}"
//                     }
//                 }
//             }
//         }

//         stage('Test') {
//             steps {
//                 script {
//                     if (env.SERVICES_TO_BUILD?.trim()) {
//                         env.SERVICES_TO_BUILD.split(',').each { service ->
//                             echo "Running tests for ${service}..."
//                             sh "cd ${service} && mvn test"
                            
//                             junit "**/${service}/target/surefire-reports/*.xml"

//                             def coverageFile = "${service}/target/site/cobertura/coverage.xml"
//                             if (fileExists(coverageFile)) {
//                                 cobertura coberturaReportFile: "**/${service}/target/site/cobertura/coverage.xml"
//                             } else {
//                                 echo "Coverage report not found for ${service}, skipping..."
//                             }
//                         }
//                     } else {
//                         echo "No services to build, skipping test stage."
//                     }
//                 }
//             }
//         }
//         stage('Build') {
//             steps {
//                 script {
//                     if (env.SERVICES_TO_BUILD?.trim()) {
//                         env.SERVICES_TO_BUILD.split(',').each { service ->
//                             echo "Building ${service}..."
//                             sh "cd ${service} && mvn clean package"
//                         }
//                     } else {
//                         echo "No services to build, skipping build stage."
//                     }
//                 }
//             }
//         }
//     }
//     post {
//         always {
//             archiveArtifacts artifacts: "**/target/*.jar", fingerprint: true
//         }
//     }
// }

pipeline {
    agent any
    tools {
        maven 'Maven3'
    }
    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service"
    }
    stages {
        stage('Test') {
            steps {
                script {
                    SERVICES.split(',').each { service ->
                        echo "Running tests for ${service}..."
                        sh "cd ${service} && mvn test verify -Dmaven.repo.local=.maven_cache"

                        // Upload test results
                        junit "**/${service}/target/surefire-reports/*.xml"

                        // Upload JaCoCo coverage report
                        def coverageFile = "${service}/target/site/jacoco/jacoco.xml"
                        if (fileExists(coverageFile)) {
                            jacoco execPattern: "**/${service}/target/jacoco.exec",
                                   classPattern: "**/${service}/target/classes",
                                   sourcePattern: "**/${service}/src/main/java",
                                   minimumInstructionCoverage: '80'
                        } else {
                            echo "JaCoCo coverage report not found for ${service}, skipping..."
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    SERVICES.split(',').each { service ->
                        echo "Building ${service}..."
                        sh "cd ${service} && mvn clean package"
                    }
                }
            }
        }
    }
}

