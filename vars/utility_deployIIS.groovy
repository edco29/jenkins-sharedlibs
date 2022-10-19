
def call(opts) {
    /*
        Download artifact from Nexus
    */

    def nexusCredentialID = opts.nexusCredentialID
    def enableGitConfigFile = (opts.enableGitConfigFile).toBoolean()
    def artifactName = "${APP_NAME}-${APP_VERSION}.zip"
    def artifactFolderName = 'packageToDeploy'
    def repositoryName

    if ( env.ENVIRONMENT == 'dev') {
        repositoryName = opts.nexusRepository
    }else {
        repositoryName = opts.nexusRepository.targetRepository
    }

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: nexusCredentialID, passwordVariable: 'PASS', usernameVariable: 'USERNAME']]) {
        echo("Download ${artifactName} from Nexus")
        sh """ rm -rf "${env.WORKSPACE}"/${artifactFolderName}/ && mkdir "${env.WORKSPACE}"/${artifactFolderName}/ """
        sh """curl  -o "${env.WORKSPACE}"/${artifactFolderName}/${artifactName} --fail -u ${env.USERNAME}:${env.PASS} ${NEXUS_URL}/repository/${repositoryName}/${APP_NAME}/${APP_VERSION}/${artifactName}"""
        unzip zipFile: "${env.WORKSPACE}/${artifactFolderName}/${artifactName}", dir: "${env.WORKSPACE}/${artifactFolderName}/"
        sh """ rm -rf "${env.WORKSPACE}"/${artifactFolderName}/${artifactName}"""
    }

    /*
        Download Config Files from Bitbucket
    */

    if (enableGitConfigFile) {
        def branchName = "${APP_NAME}"
        def configFilesGitUrl = 'CHANGE_THIS_TO_GIT_REPO_NAME'
        def gitCredentials = opts.gitCredentialID
        def configFileDirectory = "${APP_NAME}-${BUILD_TIME_STAMP}"

        dir(configFileDirectory) {
            checkout([$class: 'GitSCM',
            branches: [[name: branchName]],
            userRemoteConfigs: [[credentialsId: gitCredentials,
                                url: configFilesGitUrl]]])}
    /*
        Copy Config Files to  ArtifactPath
    */
        sh """ cp -r "${env.WORKSPACE}"/${configFileDirectory}/${ENVIRONMENT}/** "${env.WORKSPACE}"/${artifactFolderName}/ """
    }

    /*
        Deploy artifact to IIS
    */

    def iisCredentialID = opts.IISjenkinsCredentialID
    def serverName = opts.IISserverName
    def siteName = opts.IISsiteName
    def sitePath = opts.IISsitePath
    def artifactFolderPath = "${env.WORKSPACE}\\${artifactFolderName}\\**"

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: iisCredentialID, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME']]) {
        writeFile file: 'deployIIS.ps1', text: "${libraryResource 'deployIIS.ps1'}"
        bat([script:"powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command . './deployIIS.ps1' ${serverName} ${siteName} ${sitePath} ${artifactFolderPath}"])
    }
}
