def call(body) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipelineParams.each { println(it) }

    pipelineParams.nodeLabel = pipelineParams.nodeLabel ?: 'winNetFramework'
    pipelineParams.msbuildParams = pipelineParams.msbuildParams ?: '/p:Configuration=Release'
    pipelineParams.nexusRepository = pipelineParams.nexusRepository ?: ''
    pipelineParams.nexusCredentialID = pipelineParams.nexusCredentialID ?: 'nexus-credentials'
    pipelineParams.gitCredentialID = pipelineParams.gitCredentialID ?: 'bitbucket-user-pass'

    pipeline {
        agent { label "${pipelineParams.nodeLabel}" }

        environment {
            PROJECT_NAME        = 'XXX'
            NEXUS_URL           = 'http://NEXUS_URL:NEXUS_PORT'
            NUGET_PATH          = 'C:\\jenkins_tools\\nuget\\'
            SONAR_SCANNER_PATH  = 'C:\\jenkins_tools\\sonar-scanner\\'
            MSBUILD_PATH        = 'C:\\Program Files\\Microsoft Visual Studio\\2022\\Professional\\MSBuild\\Current\\Bin\\'
        }
        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            disableConcurrentBuilds()
            timeout(time: 60, unit: 'MINUTES')
            timestamps()
            ansiColor('xterm')
            skipStagesAfterUnstable()
        }

        stages {
            stage('Prepare Workspace') {
                steps {
                    script {
                        echo "\033[1;43mBuild agent label: ${pipelineParams.nodeLabel} (${env.NODE_LABELS})\033[0m"
                        env.APP_NAME =  utility_getAppName('netframework').trim()
                        env.APP_VERSION = utility_getAppVersion('netframework').trim()
                        env.ENVIRONMENT = utility_getEnvName().trim()
                        env.BUILD_TIME_STAMP = sh(script: "date '+%Y%m%d%H%M%S'", returnStdout: true).trim()
                        sh 'env'
                    }
                }
            }

            stage('Compile') {
                when {
                    anyOf {
                        expression { env.GIT_BRANCH ==~ /(develop|feature\/.*|bugfix\/.*)/ }
                        changeRequest()
                    }
                }
                steps {
                    script {
                        utility_msbuild_compile(pipelineParams)
                    }
                }
            }

            stage('Execute SonarScanner') {
                when {
                    anyOf {
                        expression { env.GIT_BRANCH ==~ /(develop|feature\/.*|bugfix\/.*)/ }
                        changeRequest()
                    }
                }
                steps {
                    script {
                        utility_msbuild_sonarScanner(pipelineParams)
                    }
                }
            }

            stage('Deploy Nexus Artifact') {
                when {
                    expression { env.GIT_BRANCH ==~ /(develop)/ }
                }
                steps {
                    script {
                        utility_msbuild_deployToNexus(pipelineParams)
                    }
                }
            }

            stage('Git Tag') {
                when {
                        expression { env.GIT_BRANCH ==~ /(release)/ }
                }
                steps {
                    script {
                        utility_gitTag(pipelineParams)
                    }
                }
            }

            stage('Promote Nexus Artifact') {
                when {
                    anyOf {
                        expression { env.GIT_BRANCH ==~ /(release)/ }
                        tag 'v*'
                    }
                }
                steps {
                    script {
                        utility_rawNexus_promote(pipelineParams)
                    }
                }
            }

            stage('Deploy to IIS') {
                when {
                    anyOf {
                        expression { env.GIT_BRANCH ==~ /(release|develop)/ }
                        tag 'v*'
                    }
                }
                steps {
                    script {
                        utility_deployIIS(pipelineParams)
                    }
                }
            }
        }
    }
}
