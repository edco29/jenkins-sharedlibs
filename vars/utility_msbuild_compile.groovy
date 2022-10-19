
def call(opts) {
    def solutionFile =  opts.solutionFile
    def msbuildParams = opts.msbuildParams

    withEnv(["PATH+msbuild=${MSBUILD_PATH}"
            ,"PATH+nuget=${NUGET_PATH}"]) {
            bat(script:"nuget.exe restore ${solutionFile}")
            bat([script:'MSBuild.exe /t:clean'])
            bat([script:"MSBuild.exe /t:build ${msbuildParams}"])
            }
}
