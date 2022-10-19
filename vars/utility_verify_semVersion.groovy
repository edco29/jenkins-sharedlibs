def call(String appVersion) {
  echo 'Verify Semantic Version'
  writeFile file: 'checkSemVer.sh', text: "${libraryResource 'checkSemVer.sh'}"

  sh 'chmod +x checkSemVer.sh'

  sh """
      if [[ `./checkSemVer.sh -v ${appVersion}` != ${appVersion} ]];then
        echo "APP VERSION (${appVersion}) DOES NOT FOLLOW THE SEMANTIC VERSION FORMAT"
        echo "MORE INFO: https://semver.org/"
        exit 1
      fi
    """
}
