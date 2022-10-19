def call(opts) {
  def solutionFile        =  opts.solutionFile
  def projectKey          = "${env.APP_NAME}"
  def projectVersion      = "${env.APP_VERSION}"

  def traceabilityParams = "/d:sonar.analysis.commitId=\"${env.GIT_COMMIT}\" /d:sonar.analysis.tek=\"net-framework\" \
                            /d:sonar.analysis.branch=\"${env.GIT_BRANCH}\" /d:sonar.links.scm=\"${env.GIT_URL}\" \
                            /d:sonar.analysis.jobname=\"${env.JOB_NAME}\" "

  echo 'Running SonarQube Analysis'
  withSonarQubeEnv {
        withEnv(["PATH+sonarscanner=${SONAR_SCANNER_PATH}", "PATH+msbuild=${MSBUILD_PATH}"]) {
          bat "SonarScanner.MSBuild.exe  begin /k:${projectKey} /n:${projectKey} /v:${projectVersion} \
                /d:sonar.verbose=true  /d:sonar.host.url=${env.SONAR_HOST_URL} \
                /d:sonar.login=${env.SONAR_AUTH_TOKEN} ${traceabilityParams}"

          bat "MSBuild.exe ${solutionFile} /t:Rebuild"
          bat "SonarScanner.MSBuild.exe end /d:sonar.login=${env.SONAR_AUTH_TOKEN}"
        }
  }
}
