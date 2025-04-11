
pipeline {
    agent any
    tools {
        maven 'Maven3'
    }
    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service,spring-petclinic-discovery-server"
    }
    stages {
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

              env.BUILD_SERVICES = affectedServices.join(',')
              
              affectedServices.each { service -> 
                  echo "Validating ${service}..."
                  sh "cd ${service} && mvn validate"
              }
            }
          }
        }
      
        stage('Build') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                script {
                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Building ${service}..."
                        sh "./mvnw -pl ${service} -am clean compile"
                    }
                }
            }
        }
      
        stage('Test') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                script {
                    env.BUILD_SERVICES.split(',').each { service ->
                        try {
                            if (service == "spring-petclinic-admin-server" || service == "spring-petclinic-genai-service") {
                                echo "Skipping tests for ${service} (No test cases available)."
                            } else {
                                echo "Running tests for ${service}..."
                                sh "./mvnw -pl ${service} test -Dmaven.repo.local=.maven_cache"

                                // Upload test results
                                junit "**/${service}/target/surefire-reports/*.xml"

                                // Handle JaCoCo coverage
                                def coverageFile = "${service}/target/site/jacoco/jacoco.xml"
                                if (fileExists(coverageFile)) {
                                    jacoco execPattern: "**/${service}/target/jacoco.exec",
                                           classPattern: "**/${service}/target/classes",
                                           sourcePattern: "**/${service}/src/main/java",
                                           minimumInstructionCoverage: '70'
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

        stage('Verify') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                script {
                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Verifying ${service}..."
                        sh "cd ${service} && mvn verify -Dmaven.repo.local=.maven_cache"
                    }
                }
            }
        }
    }
}


