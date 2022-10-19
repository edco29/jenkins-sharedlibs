def call(opts) {
  def gitTagName = "v${APP_VERSION}"
  def tagComment = "DeploymentDate:${BUILD_TIME_STAMP} JenkinsPipeline:${env.BUILD_URL}"
  def gitCredentials = opts.gitCredentialID

  withCredentials([gitUsernamePassword(credentialsId: gitCredentials )]) {
    sh("""
        git tag -a "${gitTagName}" -m "${tagComment}"
        git push origin "${gitTagName}"
    """)
  }
}
