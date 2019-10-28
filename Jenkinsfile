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
                withSonarQubeEnv('Sonarqube') {
                    sh "env"
                }
                sh 'mvn clean -B -V -P docker verify cobertura:cobertura'
                sh 'find . -name coverage.xml'
                stash name: "cobertura.xml", includes: "/var/jenkins_home/worker/tmp_cr/target/site/cobertura/coverage.xml"
                stash name: "cobertura.ser", includes: "/var/jenkins_home/worker/tmp_cr/target/cobertura/cobertura.ser"
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
                unstash "cobertura.xml"
                unstash "cobertura.ser"
                withSonarQubeEnv('Sonarqube') {
                    sh "env"
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
