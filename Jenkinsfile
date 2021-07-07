pipeline {
  agent {
            node { label "docker-host" }
  }

  environment {
    GIT_NAME = "eionet.contreg"
    SONARQUBE_TAGS = "cr.eionet.europa.eu"
    registry = "eeacms/contreg"
    availableport = sh(script: 'echo $(python3 -c \'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1], end = ""); s.close()\');', returnStdout: true).trim();
    availableport2 = sh(script: 'echo $(python3 -c \'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1], end = ""); s.close()\');', returnStdout: true).trim();
    availableport3 = sh(script: 'echo $(python3 -c \'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1], end = ""); s.close()\');', returnStdout: true).trim();

  }

  tools {
        maven 'maven3'
  }
  
  stages {

    stage ('Unit Tests and Sonarqube') {
      when {
        not { buildingTag() }
      }     
      steps {
                sh './prepare-tmp.sh'
                withSonarQubeEnv('Sonarqube') {
                    sh '''mvn clean -B -V -P docker verify cobertura:cobertura-integration-test pmd:pmd pmd:cpd findbugs:findbugs checkstyle:checkstyle surefire-report:report sonar:sonar -Dsonar.sources=src/main/java/ -Dsonar.junit.reportPaths=target/failsafe-reports -Dsonar.cobertura.reportPath=target/site/cobertura/coverage.xml -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.java.binaries=target/classes -Dsonar.java.test.binaries=target/test-classes -Dsonar.projectKey=${GIT_NAME}-${GIT_BRANCH} -Dsonar.projectName=${GIT_NAME}-${GIT_BRANCH}'''
                    sh '''try=2; while [ \$try -gt 0 ]; do curl -s -XPOST -u "${SONAR_AUTH_TOKEN}:" "${SONAR_HOST_URL}api/project_tags/set?project=${GIT_NAME}-${BRANCH_NAME}&tags=${SONARQUBE_TAGS},${BRANCH_NAME}" > set_tags_result; if [ \$(grep -ic error set_tags_result ) -eq 0 ]; then try=0; else cat set_tags_result; echo "... Will retry"; sleep 60; try=\$(( \$try - 1 )); fi; done'''
                }
      }
      post {
        always {
            junit 'target/failsafe-reports/*.xml'
            cobertura coberturaReportFile: 'target/site/cobertura/coverage.xml'
        }
      }
    }

    stage ('Build war') {
      when {
          environment name: 'CHANGE_ID', value: ''
      }
      steps {
        script {
                sh '''mvn -P docker clean package -Dmaven.test.skip=true'''
            }
       }
       post {
        success {
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
      }
    }
    
    stage ('Docker build and push') {
      when {
          environment name: 'CHANGE_ID', value: ''
      }
      steps {
        script{
                 
                 if (env.BRANCH_NAME == 'master') {
                         tagName = 'latest'
                 } else {
                         tagName = "$BRANCH_NAME"
                 }
                 dockerImage = docker.build("$registry:$tagName", "--no-cache .")
                 docker.withRegistry( '', 'eeajenkins' ) {
                          dockerImage.push()
                 }
            }
      }
      post {
        always {
                           sh "docker rmi $registry:$tagName | docker images $registry:$tagName"
        }
        
        }
    }   
    
  }

  post {
    always {
      cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenSuccess: true, cleanWhenUnstable: true, deleteDirs: true)
      script {
        def details = """<h1>${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${currentBuild.currentResult}</h1>
                         <p>Check console output at <a href="${env.BUILD_URL}/display/redirect">${env.JOB_BASE_NAME} - #${env.BUILD_NUMBER}</a></p>
                      """
        emailext(
        subject: '$DEFAULT_SUBJECT',
        body: details,
        attachLog: true,
        compressLog: true,
        recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'CulpritsRecipientProvider']]
        )
      }
    }
  }
}
