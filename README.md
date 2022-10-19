# Jenkins Shared Library

# Shared Library Structure
The directory structure of a Shared Library repository is as follows:

    (root)
    +- vars
    |   +- pipeline_netframework.groovy             # main pipeline
    |   +- utility_deployIIS.goovy                  # utility pipeline
    |   +- utility_*.goovy                          # utility pipeline
    +- resources                                    # resource files
    |   +- deployIIS.ps1                            # static helper data for main pipeline

# Git Branch Modelling 
## GitFlow
the environment, is taken according to the branch in which it is located.

    dev environment => develop branch
    qa environment => release branch
    prd environment => tag

# How to use it?
The pipeline is executed, according to the branch you are on.
this structure is located in the devops folder

    (root)
    +- app                                      # folder    
    |   +- info.json                            # define version in project
    +- jenkins                                  # folder                                 
    |   +- dev.jenkinsfile                      # jenkinsfile for dev environment
    |   +- qa.jenkinsfile                       # jenkinsfile for qa environment
    |   +- prd.jenkinsfile                      # jenkinsfile for prd environment

- jenkins: this folder is located in the application repository
- dev.jenkinsfile:  file that executes the pipeline flow in develop branch
- qa.jenkinsfile:  file that executes the pipeline flow in release branch
- prd.jenkinsfile:  file that executes the pipeline flow in tag vx.x.x

# Example:

## NET FRAMEWORK

This is how the 'dev.jenkinsfile' file is used. It runs when I am on the 'dev branch'. The same thing happens in the different branches. Also, don't forget to change the project version in the info.json file.

Details of the variables inside the dev.jenkins file:

    @Library('pipeline-library') _
    pipeline_netframework {
    nodeLabel = 'winNetFramework'                                           # Jenkins Nodo name

    solutionFile = 'xxxx.sln'                                               # .sln file name
    msbuildParams = '/p:Configuration=Release'                              # Additional parameters to msbuild
    packageToDeployMsbuildTarget = '/t:xxx:packageToDeploy'                 # Package to deploy target

    IISjenkinsCredentialID = 'xx-xx-iis-dev'                                # Jenkins credential ID for IIS
    IISsiteName = 'xxxxxx'                                                  # IIS site Name
    IISsitePath = "C:\\inetpub\\wwwroot\\xxxxxxx"                           # IIS site name Path
    IISserverName = 'xxx'                                                   # Windows server name
    enableGitConfigFile = 'true'                                            # Enable git external configuration repository

    nexusRepository = 'xxxxx'                                               # nexus repository name
    }

# Sematic version
The app version should follow the semantic version standar , more info : https://semver.org/

    (root)
    +- app
    |   +- info.json                            # app info

# Example
In the info.json file the name and version of the project are required.

    {
    "name": "xxxxx",                  #  name project
    "version": "1.0.0"                          #  version project
    }

    # Note 
    Do not forget to change the app version on each deployment also remember that the environment is taken by the branches defined in the git flow strategy.

