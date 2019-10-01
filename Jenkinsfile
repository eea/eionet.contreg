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
    stage ('Build') {
      steps {
        node(label: 'docker') {
          script {
              try {
                checkout scm
                sh './prepare-tmp.sh'
                sh 'mvn -P docker clean -B -V verify'
              } catch (err) {
                throw err
              } finally {
                sh 'rm -r /var/jenkins_home/worker/tmp_cr'
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

    stage('Code analysis') {
      steps {
        node(label: 'docker') {
          sh 'mvn clean -B -V -P docker verify cobertura:cobertura pmd:pmd pmd:cpd findbugs:findbugs checkstyle:checkstyle'
        }
      }
      post {
        always {
            junit '**/target/failsafe-reports/*.xml'
            pmd canComputeNew: false
            dry canComputeNew: false
            checkstyle canComputeNew: false
            findbugs pattern: '**/target/findbugsXml.xml'
            openTasks canComputeNew: false
            cobertura coberturaReportFile: '**/target/site/cobertura/coverage.xml', failNoReports: true
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
