package engine.os.powershell.scripts

import engine.os.*
import engine.os.powershell.*

object InstallDocker extends PowershellInstallScript {
  override def appName: String = "Docker"
  override def appCommandName: String = "docker"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      """
        |$ProgressPreference = 'SilentlyContinue'
        |Invoke-WebRequest -UseBasicParsing -OutFile docker-installer.exe https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe
        |Start-Process -Wait -FilePath docker-installer.exe -ArgumentList install
        |Remove-Item docker-installer.exe
        |""".stripMargin
  }
}
