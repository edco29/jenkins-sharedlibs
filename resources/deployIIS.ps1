<#
.DESCRIPTION
	Deploy artifact to IIS site
.EXAMPLE
	PS> ./deployIIS.ps1 ${serverName} ${siteName} ${sitePath} ${artifactFolderPath}
.NOTES
	Author: Edwin Contreras
#>
Param (
	[Parameter(Mandatory = $true)]
	[string]$serverName,
	[Parameter(Mandatory = $true)]
	[string]$siteName,
	[Parameter(Mandatory = $true)]
	[string]$sitePath,
	[Parameter(Mandatory = $true)]
	[string]$artifactFolderPath 
)

$ErrorActionPreference = "Stop"

# USERNAME and PASSWORD have been injected as env variables by jenkins(withCredentials)
$serverUserName = $Env:USERNAME 
$serverPassword = $Env:PASSWORD 


$server = New-PSSession -Name AppServer -ComputerName ${serverName} -Credential (New-object System.Management.Automation.PSCredential -ArgumentList @("${serverUserName}" , ( "${serverPassword}" | ConvertTo-secureString -AsPlainText -Force))) 

Write-Host "Stop site ${siteName} in server ${serverName}"
Invoke-Command -Session $server -ArgumentList $siteName -ScriptBlock {
	param($siteName)
	Stop-WebSite -Name "${siteName}" 
}

Write-Host "Copy files from  ${artifactFolderPath} to  ${sitePath}"
Copy-Item –Path "${artifactFolderPath}" –Destination "${sitePath}" -Recurse -Force –ToSession $server  -Verbose 

#Invoke-Command -Session $server -ArgumentList $sitePath -ScriptBlock { 
#	param($sitePath)
#	Expand-Archive -Path "${sitePath}packageToDeploy.zip" -DestinationPath $sitePath -Force -Verbose 
#}

Write-Host "Start site ${siteName} in server ${serverName}"
Invoke-Command -Session $server -ArgumentList $siteName  -ScriptBlock {
	param($siteName)
	Start-WebSite -Name "${siteName}" 
}