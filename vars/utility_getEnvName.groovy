def call() {
    def branchName = "${env.GIT_BRANCH}"
    def environmentName

    if (branchName ==~ /(master)|(v\d+\.\d+\.\d+)/) {
        environmentName = 'prd'
    }else if (branchName ==~ /(release)/) {
        environmentName = 'qa'
    }else if (branchName ==~ /(develop)|(feature\/.*)|(bugfix\/.*)|(PR-\d+)/ ) {
        environmentName = 'dev'
    }else {
        error("You are running the jenkins pipeline from a not standar branch (${branchName})")
    }
    return environmentName
}
