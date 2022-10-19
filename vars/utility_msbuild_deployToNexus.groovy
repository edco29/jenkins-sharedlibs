
def call(opts) {
    def solutionFile = opts.solutionFile
    def msbuildParams = opts.msbuildParams
    def packageToDeployMsbuildTarget =  opts.packageToDeployMsbuildTarget

    def credentialID = opts.nexusCredentialID
    def repositoryName = opts.nexusRepository
    def artifactName = "${APP_NAME}-${APP_VERSION}.zip"

/*
    Running all msbuild initial targets ( clean , build )
    packageToDeployMsbuildTarget => Generate a ${env.APP_NAME}-${env.APP_VERSION}.zip file
                                    in the root workspace directory
*/
    echo("Generating  ${artifactName} file")

    withEnv(["PATH+msbuild=${MSBUILD_PATH}"
            ,"PATH+nuget=${NUGET_PATH}"]) {
            sh([script:"""rm -rf "${env.WORKSPACE}"/target"""])
            sh([script:"""rm -rf "${env.WORKSPACE}"/*.zip"""])
            bat(script:"nuget.exe restore ${solutionFile}")
            bat([script:'MSBuild.exe /t:clean'])
            bat([script:"MSBuild.exe /t:build ${msbuildParams}"])
            bat([script:"MSBuild.exe ${packageToDeployMsbuildTarget}"])
            }

/*
    Deploy  ${env.APP_NAME}-${env.APP_VERSION}.zip file to nexus
*/

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialID, passwordVariable: 'PASS', usernameVariable: 'USERNAME']]) {
        echo("Deploy ${artifactName} file to Nexus")
        sh """curl --fail -u ${env.USERNAME}:${env.PASS} --upload-file "${env.WORKSPACE}"/${artifactName} ${env.NEXUS_URL}/repository/${repositoryName}/${APP_NAME}/${APP_VERSION}/${artifactName}"""
    }
}
