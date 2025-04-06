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
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service,spring-petclinic-discovery-server"
        BUILD_SERVICES = ""
    }
    stages {
        // Maven build life cycle:

        // -- Validate stage -- 
        // validate - validate the project is correct and all necessary information is available
      
        // -- Build stage --
        // compile - compile the source code of the project

        // -- Test stage -- 
        // test - test the compiled source code using a suitable unit testing framework. These tests should not require the code be packaged or deployed

        // -- can be safely ignored (not related to current proj) --
        // package - take the compiled code and package it in its distributable format, such as a JAR.
        // verify - run any checks on results of integration tests to ensure quality criteria are met
        // install - install the package into the local repository, for use as a dependency in other projects locally
        // deploy - done in the build environment, copies the final package to the remote repository for sharing with other developers and projects.

        stage('Validate') {
          steps {
            script {
              // detecting changes
              def changed_services = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
              def affectedServices = []

              // extracting services with changes
              SERVICES.split(',').each {
                service ->
                if (changed_services.find { it.startsWith(service + '/') }) {
                  affectedServices.add(service)
                }
              }

              
              if (affectedServices.isEmpty()) {
                  echo "No relevant changes detected. Skipping validation."
                  currentBuild.result = 'SUCCESS'
                  return
              }

              affectedServices.each { service -> 
                  echo "Validating ${service}..."
                  sh "cd ${service} && mvn validate"
              }
            }
          }
        }
      
        stage('Build') {
            steps {
                script {
                  
                    if (!env.BUILD_SERVICES || env.BUILD_SERVICES == "") {
                      echo "Skipping build"
                      return
                    }
                  
                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Building ${service}..."
                        sh "cd ${service} && mvn clean compile"
                    }
                }
            }
        }
      
        stage('Test') {
            steps {
                script {
                   if (!env.BUILD_SERVICES || env.BUILD_SERVICES == "") {
                      echo "Skipping test"
                      return
                    }
                  
                    // SERVICES.split(',').each { service ->
                    // affectedServices.each { service ->
                    env.BUILD_SERVICES.split(',').each { service ->
                        try {
                            if (service == "spring-petclinic-admin-server" || service == "spring-petclinic-genai-service") {
                                echo "Skipping tests for ${service} (No test cases available)."
                            } else {
                                echo "Running tests for ${service}..."
                                // Add -Dmaven.test.failure.ignore=true to continue on test failures
                                sh "cd ${service} && mvn test verify -Dmaven.repo.local=.maven_cache -Dmaven.test.failure.ignore=true"

                                // Debug directory structure
                                sh "pwd && ls -la ${service}/target/ || true"
                                sh "ls -la ${service}/target/site/jacoco/ || echo 'Directory not found'"

                                // Upload test results
                                junit "**/${service}/target/surefire-reports/*.xml"

                                // Handle JaCoCo coverage
                                def coverageFile = "${service}/target/site/jacoco/jacoco.xml"
                                if (fileExists(coverageFile)) {
                                    sh "ls -la ${service}/target/site/jacoco/"
                                    sh "ls -l spring-petclinic-vets-service/target/site/jacoco/jacoco.xml"
                                    jacoco execPattern: "**/${service}/target/jacoco.exec",
                                           classPattern: "**/${service}/target/classes",
                                           sourcePattern: "**/${service}/src/main/java",
                                           minimumInstructionCoverage: '0'
                                } else {
                                    echo "JaCoCo coverage report not found for ${service}, skipping..."
                                    // Debug why file is missing
                                    sh "ls -la ${service}/target/site/jacoco/ || true"
                                }
                            }
                        } catch (Exception e) {
                            echo "Error testing ${service}: ${e.toString()}"
                            // Continue with next service
                        }
                    }
                }
            }
        }


    }
}


