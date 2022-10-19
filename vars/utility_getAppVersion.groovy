def call(String technology) {
    String appVersion = ''

    switch (technology.toLowerCase()) {
        case 'netframework' :
            def appInfoPath = "${env.WORKSPACE}/devops/app/info.json"

            sh """
                if [ ! -f "$appInfoPath" ]; then
                    echo "info.json file does not exits"
                    exit 1
                fi
            """
            appVersion = sh(returnStdout: true, script: """ cat "${appInfoPath}" | jq -r '.version'  """).trim()
            break
        default:
            error "technology=${technology} is not valid"
    }

    if (!appVersion) {  error "App Version is not set - tecnology=${technology}" }

    utility_verify_semVersion(appVersion)

    return appVersion
}
