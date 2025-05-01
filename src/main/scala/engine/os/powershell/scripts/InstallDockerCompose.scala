package engine.os.powershell.scripts

import engine.os.powershell.*

object InstallDockerCompose extends PowershellInstallScript {
  override def appName: String = "Docker Compose"
  override def appCommandName: String = "docker-compose"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      """
        |$ProgressPreference = 'SilentlyContinue'
        |# Install Docker Compose (latest release version)
        |$dockerComposeVersion = "1.29.2"
        |Invoke-WebRequest -Uri "https://github.com/docker/compose/releases/download/$dockerComposeVersion/docker-compose-Windows-x86_64.exe" -OutFile "C:\Program Files\Docker\docker-compose.exe"
        |# Set proper permissions (not always necessary on Windows)
        |Set-ItemProperty -Path "C:\Program Files\Docker\docker-compose.exe" -Name "IsReadOnly" -Value $false
        |
        |# Verify the installation of Docker Compose
        |docker-compose --version
        |""".stripMargin
  }
}
