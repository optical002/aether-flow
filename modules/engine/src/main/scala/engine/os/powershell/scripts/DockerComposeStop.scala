package engine.os.powershell.scripts

import engine.os.powershell.{PowershellRunScript, PowershellScript}

import java.nio.file.Path

class DockerComposeStop(yamlFile: Path, name: String) extends PowershellRunScript {
  override def appName: String = "Docker Compose Down"
  override def appCommandName: String = "docker-compose"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |# Stop and remove the Docker Compose project
         |docker-compose -f "${yamlFile.toAbsolutePath}" -p "$name" stop
         |""".stripMargin
  }
}
