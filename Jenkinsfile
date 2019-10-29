pipeline {
  agent any

  environment {
    GIT_NAME = "eionet.contreg"
    SONARQUBE_TAGS = "cr.eionet.europa.eu"
  }

  tools {
    maven 'maven3'
    jdk 'Java8'
  }

  stages {
    stage ('Build and code analysis') {
      steps {
        node(label: 'docker') {
          script {
              try {
                sh 'rm -rf /var/jenkins_home/worker/tmp_cr'
                checkout scm
                sh './prepare-tmp.sh'
                sh 'mvn clean -B -V -P docker verify cobertura:cobertura pmd:pmd pmd:cpd findbugs:findbugs checkstyle:checkstyle'
                stash name: "coverage.xml", includes: "target/site/cobertura/coverage.xml"
                stash name: "${GIT_NAME}-${GIT_BRANCH}-classes", includes: "target/classes/**"
                stash name: "${GIT_NAME}-${GIT_BRANCH}-test-classes", includes: "target/test-classes/**"
              } catch (err) {
                throw err
              } finally {
                sh 'rm -rf /var/jenkins_home/worker/tmp_cr'
              }
          }
        }
      }
      post {
        success {
          archive 'target/*.war'
        }
      }
    }

    stage ('Report to SonarQube') {
       when {
            allOf {
              environment name: 'CHANGE_ID', value: ''
            }
       }
       steps {
            node(label: 'swarm') {
                checkout scm
                unstash "coverage.xml"
                dir("${GIT_NAME}-${GIT_BRANCH}-classes") {
                  unstash "${GIT_NAME}-${GIT_BRANCH}-classes"
                }
                dir("${GIT_NAME}-${GIT_BRANCH}-test-classes") {
                  unstash "${GIT_NAME}-${GIT_BRANCH}-test-classes"
                }
                withSonarQubeEnv('Sonarqube') {
                    sh "mvn sonar:sonar -Dsonar.cobertura.reportPat=coverage.xml -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.java.binaries=${GIT_NAME}-${GIT_BRANCH}-classes -Dsonar.java.test.binaries=${GIT_NAME}-${GIT_BRANCH}-test-classes -Dsonar.projectKey=${GIT_NAME}-${GIT_BRANCH} -Dsonar.projectName=${GIT_NAME}-${GIT_BRANCH}"
                    sh '''try=2; while [ \$try -gt 0 ]; do curl -s -XPOST -u "${SONAR_AUTH_TOKEN}:" "${SONAR_HOST_URL}api/project_tags/set?project=${GIT_NAME}-${BRANCH_NAME}&tags=${SONARQUBE_TAGS},${BRANCH_NAME}" > set_tags_result; if [ \$(grep -ic error set_tags_result ) -eq 0 ]; then try=0; else cat set_tags_result; echo "... Will retry"; sleep 60; try=\$(( \$try - 1 )); fi; done'''
                }
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

    stage('Release') {
        when {
            allOf {
                environment name: 'CHANGE_ID', value: ''
                branch 'master'
            }
        }
        steps {
            node(label: 'swarm') {
                sh 'echo $BUILDTIME'
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
