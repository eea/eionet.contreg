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
    stage ('Build, code analysis and SonarQube report') {
      steps {
          script {
                 if (env.BRANCH_NAME == 'master') {
                   tagName = 'latest'
                 } else {
                   tagName = "$BRANCH_NAME"
                 }
                
                checkout scm
                sh './prepare-tmp.sh'

                withSonarQubeEnv('Sonarqube') {
                    sh 'mvn clean -B -V -P docker verify cobertura:cobertura-integration-test pmd:pmd pmd:cpd findbugs:findbugs checkstyle:checkstyle sonar:sonar -Dsonar.sources=src/main/java/ -Dsonar.junit.reportPaths=target/failsafe-reports -Dsonar.cobertura.reportPath=target/site/cobertura/coverage.xml -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.java.binaries=target/classes -Dsonar.java.test.binaries=target/test-classes -Dsonar.projectKey=${GIT_NAME}-${GIT_BRANCH} -Dsonar.projectName=${GIT_NAME}-${GIT_BRANCH}'
                    if (env.CHANGE_ID == '') {
                        try {
                          dockerImage = docker.build("$registry:$tagName", "--no-cache .")
                          docker.withRegistry( '', 'eeajenkins' ) {
                          dockerImage.push()
                           }
                        } finally {
                           sh "docker rmi $registry:$tagName"
                        }
                    }

                    sh '''try=2; while [ \$try -gt 0 ]; do curl -s -XPOST -u "${SONAR_AUTH_TOKEN}:" "${SONAR_HOST_URL}api/project_tags/set?project=${GIT_NAME}-${BRANCH_NAME}&tags=${SONARQUBE_TAGS},${BRANCH_NAME}" > set_tags_result; if [ \$(grep -ic error set_tags_result ) -eq 0 ]; then try=0; else cat set_tags_result; echo "... Will retry"; sleep 60; try=\$(( \$try - 1 )); fi; done'''
                }
        }
      }
      post {
        always {
            sh 'ls -ltr *'
            sh 'ls -ltr target/*'
            junit 'target/failsafe-reports/failsafe-summary.xml'
            cobertura coberturaReportFile: 'target/site/cobertura/coverage.xml'
            sh 'ls -ltr target/site/cobertura/*'
            sh 'rm -rf ./tmp_cr'
            cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenSuccess: true, cleanWhenUnstable: true, deleteDirs: true)
        }
        success {
          archive 'target/*.war'
        }
      }
    }

    stage('Pull Request') {
      when {
        not {
          environment name: 'CHANGE_ID', value: ''
        }
        environment name: 'CHANGE_TARGET', value: 'master'
      }
      steps {
        node(label: 'swarm') {
          script {
            if ( env.CHANGE_BRANCH != "develop" &&  !( env.CHANGE_BRANCH.startsWith("hotfix")) ) {
                error "Pipeline aborted due to PR not made from develop or hotfix branch"
            }
          }
        }
      }
    }
  }

  post {
      changed {
        script {
          def url = "${env.BUILD_URL}/display/redirect"
          def status = currentBuild.currentResult
          def subject = "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
          def summary = "${subject} (${url})"
          def details = """<h1>${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}</h1>
                           <p>Check console output at <a href="${url}">${env.JOB_BASE_NAME} - #${env.BUILD_NUMBER}</a></p>
                        """

          def color = '#FFFF00'
          if (status == 'SUCCESS') {
            color = '#00FF00'
          } else if (status == 'FAILURE') {
            color = '#FF0000'
          }

          emailext (subject: '$DEFAULT_SUBJECT', to: '$DEFAULT_RECIPIENTS', body: details)
        }
      }
    }
}
