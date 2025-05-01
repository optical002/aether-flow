package engine.os.powershell.scripts

import engine.os.*
import engine.os.powershell.*

import java.nio.file.Path

class DockerComposeUp(yamlFile: Path, name: String) extends PowershellRunScript {
  override def appName: String = "Docker Compose Up"
  override def appCommandName: String = "docker-compose"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |# Run docker-compose up with the provided YAML
         |docker-compose -f "${yamlFile.toAbsolutePath}" -p "$name" up -d
         |
         |# Optionally, verify the services are running
         |docker-compose -f "${yamlFile.toAbsolutePath}" ps
         |""".stripMargin
  }
}
