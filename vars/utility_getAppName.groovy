def call(String technology) {
    def appName = ''

    switch (technology.toLowerCase()) {
        case 'netframework' :
            def appInfoPath = "${env.WORKSPACE}/devops/app/info.json"

            sh """
                if [ ! -f "$appInfoPath" ]; then
                    echo "info.json file does not exits"
                    exit 1
                fi
            """
            appName = sh(returnStdout: true , script: """cat "${appInfoPath}" | jq -r '.name' """).trim()
            break
        default:
            error "technology=${technology} is not valid"
    }
    if (!appName) {  error "App Name is not set  - tecnology=${technology}" }
    return appName
}
