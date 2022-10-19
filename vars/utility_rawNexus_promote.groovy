def call(opts) {
    def credentialID = opts.nexusCredentialID
    def sourceRepository = opts.nexusPromoteRepository.sourceRepository
    def targetRepository = otps.nexusPromoteRepository.targetRepository
    def artifactName = "${APP_NAME}-${APP_VERSION}.zip"

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialID, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME']]) {
        echo("Download ${artifactName} from  ${sourceRepository}")
        sh """curl -o "${env.WORKSPACE}"/${artifactName}" -u ${env.USERNAME}:${env.PASSWORD} ${NEXUS_URL}/repository/${sourceRepository}/${APP_NAME}/${APP_VERSION}/${artifactName} """

        echo("Upload ${artifactName} to ${targetRepository}")
        sh """curl --fail -u ${env.USERNAME}:${env.PASSWORD} --upload-file "${env.WORKSPACE}"/${artifactName} ${NEXUS_URL}/repository/${targetRepository}/${APP_NAME}/${APP_VERSION}/${artifactName} """
        sh """ rm -rf "${env.WORKSPACE}"/${artifactName}  """
    }
}
